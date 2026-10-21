"""Upload published lesson packages to Supabase Storage."""

from __future__ import annotations

import hashlib
import json
import os
import shutil
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Callable

from .upload_storage import *

_client = client


@classmethod
def _from_env(cls: type[StorageConfig]) -> StorageConfig:
    url = (os.getenv("SUPABASE_URL") or "").strip().rstrip("/")
    key = (os.getenv("SUPABASE_SERVICE_ROLE_KEY") or "").strip()
    missing = [name for name, value in (("SUPABASE_URL", url), ("SUPABASE_SERVICE_ROLE_KEY", key)) if not value]
    if missing:
        raise UploadError("Missing Supabase configuration: " + ", ".join(missing) + " (set them in .env)")
    return cls(url=url, service_key=key)


StorageConfig.from_env = _from_env


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
    if not root.is_dir():
        raise UploadError(f"Publish directory not found: {root}")
    files = [PackageFile(path.relative_to(root).as_posix(), path, path.stat().st_size) for path in root.rglob("*") if path.is_file()]
    if not any(item.relative == CATALOG_NAME for item in files):
        raise UploadError(f"{root / CATALOG_NAME} is missing; run `mandarin-course publish` first")
    files.sort(key=lambda item: (not item.versioned, item.relative))
    return files


def _versions(catalog: dict) -> dict[str, int]:
    return {entry["id"]: int(entry["version"]) for entry in catalog.get("lessons", [])}


def _number(path: str) -> int | None:
    prefix = version_prefix(path)
    return int(prefix.rstrip("/").rsplit("/", 1)[1][1:]) if prefix else None


def _keep_candidates(versions: list[int], current: int) -> set[int]:
    older = [version for version in versions if version < current]
    return {current, max(older)} if older else {current}


def _remote_prune_candidates(http, config: StorageConfig, lesson_id: str, current: int) -> list[str]:
    paths = remote_paths(http, config, f"lessons/{lesson_id}/")
    keep = _keep_candidates(sorted({number for path in paths if (number := _number(path)) is not None}), current)
    # Only versioned prefixes are prunable; anything else under the lesson stays.
    return [path for path in paths if (number := _number(path)) is not None and number not in keep]


def _local_prune_candidates(root: Path, lesson_id: str, current: int) -> list[Path]:
    directory = root / "lessons" / lesson_id
    if not directory.is_dir():
        return []
    paths = [path for path in directory.iterdir() if path.is_dir() and path.name.startswith("v") and path.name[1:].isdigit()]
    keep = _keep_candidates(sorted(int(path.name[1:]) for path in paths), current)
    return [path for path in paths if int(path.name[1:]) not in keep]


def _catalog_matches(http, config: StorageConfig, item: PackageFile, clock: Callable[[], float]) -> bool:
    try:
        body = public_body(http, config, item.relative, f"?t={int(clock() * 1000)}")
    except UploadError:
        return False
    return len(body) == item.size and sha256_bytes(body) == sha256(item.path)


def _verify_catalog(http, config: StorageConfig, item: PackageFile, sleep: Callable[[float], None], clock: Callable[[], float]) -> None:
    deadline = clock() + 120
    while True:
        try:
            verify(http, config, item)
            return
        except UploadError as error:
            if clock() >= deadline:
                raise UploadError(f"Catalog verification timed out after 120 seconds: {error}")
        sleep(min(10, max(0, deadline - clock())))


def upload_package(root: Path, config: StorageConfig, *, dry_run: bool = False, prune: bool = True,
                   echo: Callable[[str], None] = print, transport=None,
                   sleep: Callable[[float], None] = time.sleep,
                   clock: Callable[[], float] = time.time) -> UploadReport:
    files = package_files(root)
    uploaded = skipped = 0
    new_prefixes: set[str] = set()
    catalog_skipped = False
    verified = False
    pruned_remote = pruned_local = 0
    with _client(transport) as http:
        if dry_run:
            echo(f"Dry run to bucket `{config.bucket}` from {root} ({len(files)} file(s)):")
        for item in files:
            remote = remote_object(http, config, item)
            if item.versioned and remote is not None:
                ensure_unchanged(item, remote)
                skipped += 1
                if dry_run:
                    echo(f"  skip    {item.relative} ({item.size:,} B, unchanged)")
                continue
            if not dry_run and not item.versioned and _catalog_matches(http, config, item, clock):
                catalog_skipped = True
                skipped += 1
                continue
            note = "catalog, no-cache, uploaded last" if not item.versioned else "new version"
            if dry_run:
                uploaded += 1
                echo(f"  upload  {item.relative} ({item.size:,} B, {note})")
                continue
            upload(http, config, item)
            uploaded += 1
            if item.versioned and version_prefix(item.relative):
                new_prefixes.add(version_prefix(item.relative))
        catalog = json.loads((root / CATALOG_NAME).read_text(encoding="utf-8"))
        if not dry_run:
            catalog_item = next(item for item in files if item.relative == CATALOG_NAME)
            if not catalog_skipped:
                _verify_catalog(http, config, catalog_item, sleep, clock)
            for item in files:
                if item.versioned and version_prefix(item.relative) in new_prefixes:
                    verify(http, config, item)
            verified = True
        if prune and (dry_run or verified):
            for lesson_id, current in _versions(catalog).items():
                remote_targets = _remote_prune_candidates(http, config, lesson_id, current)
                local_targets = _local_prune_candidates(root, lesson_id, current)
                if dry_run:
                    for path in remote_targets:
                        echo(f"  delete  (remote) {path}")
                    for path in local_targets:
                        echo(f"  delete  (local)  {path.relative_to(root)}/")
                else:
                    delete_remote(http, config, remote_targets)
                    for path in local_targets:
                        shutil.rmtree(path)
                pruned_remote += len(remote_targets)
                pruned_local += len(local_targets)
    if dry_run:
        echo("Dry run only: no files were uploaded or deleted.")
    else:
        echo(f"Uploaded {uploaded} file(s); skipped {skipped} unchanged file(s).")
        echo(f"Verified: ok ({len(new_prefixes)} new version(s) checked against the public endpoint)")
        echo(f"Pruned: {pruned_remote} remote object(s), {pruned_local} local version dir(s)" if prune else "Pruned: skipped (--no-prune)")
    return UploadReport(len(files), uploaded, skipped, dry_run, verified, pruned_remote, pruned_local)


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()
