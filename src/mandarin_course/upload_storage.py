"""HTTP and Storage REST helpers for the lesson uploader."""

from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Callable
from urllib.parse import quote

import httpx

CATALOG_NAME = "catalog.json"
VERSIONED_CACHE_CONTROL = "public, max-age=31536000, immutable"
CATALOG_CACHE_CONTROL = "no-cache"
CONTENT_TYPES = {".json": "application/json", ".mp3": "audio/mpeg"}
DEFAULT_CONTENT_TYPE = "application/octet-stream"
_VERSION_DIR = re.compile(r"^v(\d+)$")


class UploadError(Exception):
    """A failed upload that must be shown as one line and exit non-zero."""


@dataclass(frozen=True)
class StorageConfig:
    url: str
    service_key: str
    bucket: str = "course"


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


def client(transport: httpx.BaseTransport | None = None) -> httpx.Client:
    return httpx.Client(timeout=httpx.Timeout(120.0, connect=15.0), transport=transport)


def headers(config: StorageConfig) -> dict[str, str]:
    return {"apikey": config.service_key, "authorization": f"Bearer {config.service_key}"}


def object_url(config: StorageConfig, relative: str) -> str:
    return f"{config.url}/storage/v1/object/{config.bucket}/{quote(relative, safe='/')}"


def public_object_url(config: StorageConfig, relative: str) -> str:
    return f"{config.url}/storage/v1/object/public/{config.bucket}/{quote(relative, safe='/')}"


def list_url(config: StorageConfig) -> str:
    return f"{config.url}/storage/v1/object/list/{config.bucket}"


def delete_url(config: StorageConfig) -> str:
    return f"{config.url}/storage/v1/object/{config.bucket}"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def md5(path: Path) -> str:
    digest = hashlib.md5(usedforsecurity=False)
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def version_prefix(relative: str) -> str | None:
    parts = relative.split("/")
    if len(parts) >= 3 and parts[0] == "lessons" and _VERSION_DIR.match(parts[2]):
        return "/".join(parts[:3]) + "/"
    return None


def version_number(path: str) -> int | None:
    prefix = version_prefix(path)
    if prefix is None:
        return None
    return int(prefix.rstrip("/").rsplit("/", 1)[1][1:])


def remote_object(client: httpx.Client, config: StorageConfig, item: PackageFile) -> RemoteObject | None:
    response = client.head(object_url(config, item.relative), headers=headers(config))
    if response.status_code in (400, 404):
        if "bucket not found" in response.text.lower():
            raise UploadError(
                f"Bucket `{config.bucket}` does not exist; apply supabase/migrations/ "
                "with `supabase db push` against the project in SUPABASE_URL"
            )
        return None
    if response.status_code != 200:
        raise UploadError(f"Could not check {item.relative} in bucket {config.bucket}: HTTP {response.status_code}")
    length = response.headers.get("content-length")
    size = int(length) if length and length.isdigit() else None
    etag = response.headers.get("etag", "").strip('"')
    valid_md5 = len(etag) == 32 and all(char in "0123456789abcdef" for char in etag.lower())
    return RemoteObject(size=size, md5=etag if valid_md5 else None)


def ensure_unchanged(item: PackageFile, remote: RemoteObject) -> None:
    if remote.size is not None and remote.size != item.size:
        raise UploadError(f"Refusing to overwrite {item.relative}: published versions are immutable (local {item.size} B, remote {remote.size} B); publish a new version instead")
    if remote.md5 is not None and remote.md5 != md5(item.path):
        raise UploadError(f"Refusing to overwrite {item.relative}: published versions are immutable (remote content differs); publish a new version instead")


def upload(client: httpx.Client, config: StorageConfig, item: PackageFile) -> None:
    request_headers = {
        **headers(config), "content-type": item.content_type, "cache-control": item.cache_control,
        "x-upsert": "false" if item.versioned else "true",
    }
    with item.path.open("rb") as handle:
        response = client.post(object_url(config, item.relative), headers=request_headers, content=handle)
    if response.status_code not in (200, 201):
        try:
            payload = response.json()
        except ValueError:
            payload = {}
        message = str(payload.get("message") or payload.get("error") or "") if isinstance(payload, dict) else ""
        detail = f": {message}" if message else ""
        raise UploadError(f"Upload failed for {item.relative}: HTTP {response.status_code}{detail}")


def public_body(client: httpx.Client, config: StorageConfig, relative: str, query: str = "") -> bytes:
    response = client.get(public_object_url(config, relative) + query)
    if response.status_code != 200:
        raise UploadError(f"Verification failed for {relative}: public endpoint returned HTTP {response.status_code}")
    return response.content


def verify(client: httpx.Client, config: StorageConfig, item: PackageFile) -> None:
    body = public_body(client, config, item.relative)
    if len(body) != item.size:
        raise UploadError(f"Verification failed for {item.relative}: public size {len(body)} B != local {item.size} B")
    if hashlib.sha256(body).hexdigest() != sha256(item.path):
        raise UploadError(f"Verification failed for {item.relative}: public content differs from local file")


def remote_paths(client: httpx.Client, config: StorageConfig, prefix: str) -> list[str]:
    paths: list[str] = []
    offset = 0
    while True:
        response = client.post(list_url(config), headers=headers(config), json={"prefix": prefix, "limit": 1000, "offset": offset})
        if response.status_code != 200:
            raise UploadError(f"Could not list {prefix} in bucket {config.bucket}: HTTP {response.status_code}")
        batch = response.json()
        if not batch:
            break
        for entry in batch:
            name = entry.get("name", "")
            full = prefix + name
            if entry.get("id") is None and entry.get("metadata") is None:
                paths.extend(remote_paths(client, config, full + "/"))
            else:
                paths.append(full)
        offset += len(batch)
        if len(batch) < 1000:
            break
    return paths


def delete_remote(client: httpx.Client, config: StorageConfig, paths: list[str]) -> None:
    if not paths:
        return
    response = client.request("DELETE", delete_url(config), headers=headers(config), json={"prefixes": paths})
    if response.status_code != 200:
        raise UploadError(f"Could not delete {len(paths)} object(s) in bucket {config.bucket}: HTTP {response.status_code}")
