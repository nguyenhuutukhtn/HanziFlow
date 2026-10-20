"""Upload published lesson packages to the public Supabase Storage bucket.

``publish/`` is the source of truth; this module mirrors it into the ``course``
bucket using the Storage REST API and the service-role key.

Immutability rules (see docs/app_architecture.md, "Lesson package contract"):

* everything except ``catalog.json`` is a versioned artifact and is never
  overwritten — an existing object with a different size or content fails the
  upload;
* ``catalog.json`` is uploaded last, with ``no-cache``, and may be replaced.

The service-role key is read from the environment and never printed: errors
name the file and the HTTP status only.
"""

from __future__ import annotations

import hashlib
import os
from dataclasses import dataclass
from pathlib import Path
from typing import Callable
from urllib.parse import quote

import httpx

CATALOG_NAME = "catalog.json"
VERSIONED_CACHE_CONTROL = "public, max-age=31536000, immutable"
CATALOG_CACHE_CONTROL = "no-cache"
CONTENT_TYPES = {
    ".json": "application/json",
    ".mp3": "audio/mpeg",
}
DEFAULT_CONTENT_TYPE = "application/octet-stream"


class UploadError(Exception):
    """A failed upload that must be shown as one line and exit non-zero."""


@dataclass(frozen=True)
class StorageConfig:
    url: str
    service_key: str
    bucket: str = "course"

    @classmethod
    def from_env(cls) -> "StorageConfig":
        url = (os.getenv("SUPABASE_URL") or "").strip().rstrip("/")
        key = (os.getenv("SUPABASE_SERVICE_ROLE_KEY") or "").strip()
        missing = [
            name
            for name, value in (("SUPABASE_URL", url), ("SUPABASE_SERVICE_ROLE_KEY", key))
            if not value
        ]
        if missing:
            raise UploadError(
                "Missing Supabase configuration: " + ", ".join(missing) + " (set them in .env)"
            )
        return cls(url=url, service_key=key)


@dataclass(frozen=True)
class PackageFile:
    relative: str
    path: Path
    size: int

    @property
    def versioned(self) -> bool:
        return self.relative != CATALOG_NAME

    @property
    def content_type(self) -> str:
        return CONTENT_TYPES.get(self.path.suffix.lower(), DEFAULT_CONTENT_TYPE)

    @property
    def cache_control(self) -> str:
        return VERSIONED_CACHE_CONTROL if self.versioned else CATALOG_CACHE_CONTROL


@dataclass(frozen=True)
class RemoteObject:
    size: int | None
    md5: str | None


@dataclass(frozen=True)
class UploadReport:
    total: int
    uploaded: int
    skipped: int
    dry_run: bool


def package_files(root: Path) -> list[PackageFile]:
    """Every file under ``root``, versioned files first and ``catalog.json`` last."""
    if not root.is_dir():
        raise UploadError(f"Publish directory not found: {root}")
    files: list[PackageFile] = []
    for path in root.rglob("*"):
        if path.is_file():
            relative = path.relative_to(root).as_posix()
            files.append(PackageFile(relative=relative, path=path, size=path.stat().st_size))
    if not any(item.relative == CATALOG_NAME for item in files):
        raise UploadError(f"{root / CATALOG_NAME} is missing; run `mandarin-course publish` first")
    files.sort(key=lambda item: (not item.versioned, item.relative))
    return files


def _client(transport: httpx.BaseTransport | None = None) -> httpx.Client:
    return httpx.Client(timeout=httpx.Timeout(120.0, connect=15.0), transport=transport)


def _headers(config: StorageConfig) -> dict[str, str]:
    return {"apikey": config.service_key, "authorization": f"Bearer {config.service_key}"}


def _object_url(config: StorageConfig, relative: str) -> str:
    return f"{config.url}/storage/v1/object/{config.bucket}/{quote(relative, safe='/')}"


def _md5(path: Path) -> str:
    digest = hashlib.md5(usedforsecurity=False)
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _remote_object(client: httpx.Client, config: StorageConfig, item: PackageFile) -> RemoteObject | None:
    """Metadata for an existing object, or ``None`` when it does not exist."""
    response = client.head(_object_url(config, item.relative), headers=_headers(config))
    if response.status_code in (400, 404):
        if "bucket not found" in response.text.lower():
            raise UploadError(
                f"Bucket `{config.bucket}` does not exist; apply supabase/migrations/ "
                "with `supabase db push` against the project in SUPABASE_URL"
            )
        return None
    if response.status_code != 200:
        raise UploadError(
            f"Could not check {item.relative} in bucket {config.bucket}: HTTP {response.status_code}"
        )
    length = response.headers.get("content-length")
    size = int(length) if length and length.isdigit() else None
    etag = response.headers.get("etag", "").strip('"')
    md5 = etag if len(etag) == 32 and all(char in "0123456789abcdef" for char in etag.lower()) else None
    return RemoteObject(size=size, md5=md5)


def _ensure_unchanged(item: PackageFile, remote: RemoteObject) -> None:
    if remote.size is not None and remote.size != item.size:
        raise UploadError(
            f"Refusing to overwrite {item.relative}: published versions are immutable "
            f"(local {item.size} B, remote {remote.size} B); publish a new version instead"
        )
    if remote.md5 is not None and remote.md5 != _md5(item.path):
        raise UploadError(
            f"Refusing to overwrite {item.relative}: published versions are immutable "
            "(remote content differs); publish a new version instead"
        )


def _upload(client: httpx.Client, config: StorageConfig, item: PackageFile) -> None:
    headers = {
        **_headers(config),
        "content-type": item.content_type,
        "cache-control": item.cache_control,
        # Versioned objects must never replace an existing one; the catalog may.
        "x-upsert": "false" if item.versioned else "true",
    }
    with item.path.open("rb") as handle:
        response = client.post(_object_url(config, item.relative), headers=headers, content=handle)
    if response.status_code not in (200, 201):
        message = ""
        try:
            payload = response.json()
        except ValueError:
            payload = {}
        if isinstance(payload, dict):
            message = str(payload.get("message") or payload.get("error") or "")
        detail = f": {message}" if message else ""
        raise UploadError(
            f"Upload failed for {item.relative}: HTTP {response.status_code}{detail}"
        )


def upload_package(
    root: Path,
    config: StorageConfig,
    *,
    dry_run: bool = False,
    echo: Callable[[str], None] = print,
    transport: httpx.BaseTransport | None = None,
) -> UploadReport:
    """Mirror ``root`` into the Storage bucket; ``catalog.json`` goes last."""
    files = package_files(root)
    uploaded = 0
    skipped = 0
    echo(f"{'Dry run' if dry_run else 'Upload'} to bucket `{config.bucket}` from {root} ({len(files)} file(s)):")
    with _client(transport) as client:
        for item in files:
            remote = _remote_object(client, config, item)
            if remote is not None and item.versioned:
                _ensure_unchanged(item, remote)
                skipped += 1
                echo(f"  skip    {item.relative} ({item.size:,} B, unchanged)")
                continue
            if dry_run:
                uploaded += 1
                note = "catalog, no-cache, uploaded last" if not item.versioned else "new version"
                echo(f"  upload  {item.relative} ({item.size:,} B, {note})")
                continue
            _upload(client, config, item)
            uploaded += 1
            note = "catalog, no-cache, uploaded last" if not item.versioned else "new version"
            echo(f"  upload  {item.relative} ({item.size:,} B, {note})")
    return UploadReport(total=len(files), uploaded=uploaded, skipped=skipped, dry_run=dry_run)
