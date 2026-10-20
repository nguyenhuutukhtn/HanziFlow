"""Upload published lesson packages to the public Supabase Storage bucket.

``publish/`` is the source of truth; this module mirrors it into the ``course``
bucket using the Storage REST API and the service-role key.

Immutability rules (see docs/app_architecture.md, "Lesson package contract"):

* everything except ``catalog.json`` is a versioned artifact and is never
  overwritten — an existing object with a different size or content fails the
  upload;
* ``catalog.json`` is uploaded last, with ``no-cache``, and may be replaced.

Versioning rule (see docs/app_architecture.md, "Versioning rule"): only the
version referenced by the current ``catalog.json`` is kept. After uploading and
verifying the new files from the public endpoint, every other local version
directory and every other remote ``lessons/<id>/v*/`` prefix for a cataloged
lesson is deleted.

The service-role key is read from the environment and never printed: errors
name the file and the HTTP status only.
"""

from __future__ import annotations

import hashlib
import json
import os
import re
import shutil
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
_VERSION_DIR = re.compile(r"^v(\d+)$")


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
    verified: bool
    pruned_remote: int
    pruned_local: int


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


def _public_object_url(config: StorageConfig, relative: str) -> str:
    return f"{config.url}/storage/v1/object/public/{config.bucket}/{quote(relative, safe='/')}"


def _list_url(config: StorageConfig) -> str:
    return f"{config.url}/storage/v1/object/list/{config.bucket}"


def _delete_url(config: StorageConfig) -> str:
    return f"{config.url}/storage/v1/object/{config.bucket}"


def _sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _md5(path: Path) -> str:
    digest = hashlib.md5(usedforsecurity=False)
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _version_prefix(relative: str) -> str | None:
    """``lessons/<id>/v<N>/...`` -> ``lessons/<id>/v<N>/``, or ``None`` if not versioned that way."""
    parts = relative.split("/")
    if len(parts) >= 3 and parts[0] == "lessons" and _VERSION_DIR.match(parts[2]):
        return "/".join(parts[:3]) + "/"
    return None


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


def _verify_from_public_endpoint(client: httpx.Client, config: StorageConfig, item: PackageFile) -> None:
    response = client.get(_public_object_url(config, item.relative))
    if response.status_code != 200:
        raise UploadError(
            f"Verification failed for {item.relative}: public endpoint returned HTTP {response.status_code}"
        )
    body = response.content
    if len(body) != item.size:
        raise UploadError(
            f"Verification failed for {item.relative}: public size {len(body)} B != local {item.size} B"
        )
    if hashlib.sha256(body).hexdigest() != _sha256(item.path):
        raise UploadError(f"Verification failed for {item.relative}: public content differs from local file")


def _list_remote(client: httpx.Client, config: StorageConfig, prefix: str) -> list[str]:
    """Every object path under ``prefix`` (folders are listed one level at a time)."""
    paths: list[str] = []
    offset = 0
    while True:
        response = client.post(
            _list_url(config),
            headers=_headers(config),
            json={"prefix": prefix, "limit": 1000, "offset": offset},
        )
        if response.status_code != 200:
            raise UploadError(
                f"Could not list {prefix} in bucket {config.bucket}: HTTP {response.status_code}"
            )
        batch = response.json()
        if not batch:
            break
        for entry in batch:
            name = entry.get("name", "")
            full = prefix + name
            if entry.get("id") is None and entry.get("metadata") is None:
                paths.extend(_list_remote(client, config, full + "/"))
            else:
                paths.append(full)
        offset += len(batch)
        if len(batch) < 1000:
            break
    return paths


def _delete_remote(client: httpx.Client, config: StorageConfig, paths: list[str]) -> None:
    if not paths:
        return
    response = client.request(
        "DELETE", _delete_url(config), headers=_headers(config), json={"prefixes": paths}
    )
    if response.status_code != 200:
        raise UploadError(
            f"Could not delete {len(paths)} object(s) in bucket {config.bucket}: HTTP {response.status_code}"
        )


def _read_local_catalog(root: Path) -> dict:
    return json.loads((root / CATALOG_NAME).read_text(encoding="utf-8"))


def _kept_versions(catalog: dict) -> dict[str, int]:
    return {entry["id"]: int(entry["version"]) for entry in catalog.get("lessons", [])}


def _remote_prune_candidates(
    client: httpx.Client, config: StorageConfig, lesson_id: str, keep_version: int
) -> list[str]:
    keep = f"v{keep_version}"
    objects = _list_remote(client, config, f"lessons/{lesson_id}/")
    return [path for path in objects if _version_prefix(path) != f"lessons/{lesson_id}/{keep}/"]


def _local_prune_candidates(root: Path, lesson_id: str, keep_version: int) -> list[Path]:
    lesson_dir = root / "lessons" / lesson_id
    if not lesson_dir.is_dir():
        return []
    keep = f"v{keep_version}"
    return sorted(
        path
        for path in lesson_dir.iterdir()
        if path.is_dir() and _VERSION_DIR.match(path.name) and path.name != keep
    )


def upload_package(
    root: Path,
    config: StorageConfig,
    *,
    dry_run: bool = False,
    prune: bool = True,
    echo: Callable[[str], None] = print,
    transport: httpx.BaseTransport | None = None,
) -> UploadReport:
    """Mirror ``root`` into the Storage bucket, verify what's new, then prune.

    Default output is a short summary (counts only). ``dry_run`` instead lists
    every file that would be uploaded or deleted and makes no write or delete
    calls (including no verification, since nothing new would exist to check).
    """
    files = package_files(root)
    uploaded = 0
    skipped = 0
    new_version_prefixes: set[str] = set()
    verified = False
    pruned_remote = 0
    pruned_local = 0

    with _client(transport) as client:
        if dry_run:
            echo(f"Dry run to bucket `{config.bucket}` from {root} ({len(files)} file(s)):")
        for item in files:
            remote = _remote_object(client, config, item)
            if remote is not None and item.versioned:
                _ensure_unchanged(item, remote)
                skipped += 1
                if dry_run:
                    echo(f"  skip    {item.relative} ({item.size:,} B, unchanged)")
                continue
            note = "catalog, no-cache, uploaded last" if not item.versioned else "new version"
            if dry_run:
                uploaded += 1
                echo(f"  upload  {item.relative} ({item.size:,} B, {note})")
                continue
            _upload(client, config, item)
            uploaded += 1
            if item.versioned:
                prefix = _version_prefix(item.relative)
                if prefix:
                    new_version_prefixes.add(prefix)

        catalog = _read_local_catalog(root)
        kept_versions = _kept_versions(catalog)

        if not dry_run:
            to_verify = [CATALOG_NAME] + [
                item.relative
                for item in files
                if item.versioned and _version_prefix(item.relative) in new_version_prefixes
            ]
            for relative in to_verify:
                item = next(candidate for candidate in files if candidate.relative == relative)
                _verify_from_public_endpoint(client, config, item)
            verified = True

        if prune and (dry_run or verified):
            for lesson_id, keep_version in kept_versions.items():
                remote_targets = _remote_prune_candidates(client, config, lesson_id, keep_version)
                local_targets = _local_prune_candidates(root, lesson_id, keep_version)
                if dry_run:
                    for path in remote_targets:
                        echo(f"  delete  (remote) {path}")
                    for path in local_targets:
                        echo(f"  delete  (local)  {path.relative_to(root)}/")
                else:
                    _delete_remote(client, config, remote_targets)
                    for path in local_targets:
                        shutil.rmtree(path)
                pruned_remote += len(remote_targets)
                pruned_local += len(local_targets)

    if dry_run:
        echo("Dry run only: no files were uploaded or deleted.")
    else:
        echo(f"Uploaded {uploaded} file(s); skipped {skipped} unchanged file(s).")
        checked = len(new_version_prefixes)
        echo(f"Verified: ok ({checked} new version(s) checked against the public endpoint)")
        if prune:
            echo(f"Pruned: {pruned_remote} remote object(s), {pruned_local} local version dir(s)")
        else:
            echo("Pruned: skipped (--no-prune)")

    return UploadReport(
        total=len(files), uploaded=uploaded, skipped=skipped, dry_run=dry_run,
        verified=verified, pruned_remote=pruned_remote, pruned_local=pruned_local,
    )
