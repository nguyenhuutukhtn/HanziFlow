"""Upload tests use httpx.MockTransport: no network, no real Supabase project."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from urllib.parse import unquote

import httpx
import pytest

from mandarin_course import upload
from mandarin_course.cli import main
from mandarin_course.upload import (
    CATALOG_CACHE_CONTROL,
    VERSIONED_CACHE_CONTROL,
    StorageConfig,
    UploadError,
    package_files,
    upload_package,
)

URL = "https://example.supabase.co"
SERVICE_KEY = "test-service-role-key-0123456789abcdef"
OBJECT_PREFIX = "/storage/v1/object/course/"
PUBLIC_PREFIX = "/storage/v1/object/public/course/"
LIST_PATH = "/storage/v1/object/list/course"
DELETE_PATH = "/storage/v1/object/course"

CATALOG_V1 = json.dumps({
    "schema_version": 1, "catalog_version": 1,
    "lessons": [{"id": "lesson_001", "version": 1, "order": 1}],
}).encode()

FILES = {
    "lessons/lesson_001/v1/lesson.json": b'{"lesson_id": "lesson_001"}',
    "lessons/lesson_001/v1/guided.mp3": b"ID3\x04\x00\x00fake-audio",
    "catalog.json": CATALOG_V1,
}


class FakeStorage:
    """Minimal stand-in for the Storage REST endpoints used by upload.py."""

    def __init__(self, objects: dict[str, bytes] | None = None) -> None:
        self.objects = dict(objects or {})
        self.requests: list[httpx.Request] = []

    @staticmethod
    def _relative(request: httpx.Request) -> str:
        path = unquote(request.url.path)
        return path.split(OBJECT_PREFIX, 1)[1]

    def handler(self, request: httpx.Request) -> httpx.Response:
        self.requests.append(request)
        path = unquote(request.url.path)
        if path == LIST_PATH:
            return self._list(request)
        if path == DELETE_PATH and request.method == "DELETE":
            return self._delete(request)
        if path.startswith(PUBLIC_PREFIX):
            return self._get(path[len(PUBLIC_PREFIX):])
        if path.startswith(OBJECT_PREFIX):
            relative = path[len(OBJECT_PREFIX):]
            if request.method == "HEAD":
                return self._head(relative)
            if request.method == "POST":
                return self._post(relative, request)
        return httpx.Response(404)

    def _head(self, relative: str) -> httpx.Response:
        data = self.objects.get(relative)
        if data is None:
            return httpx.Response(
                400,
                json={"statusCode": "404", "error": "not_found", "message": "Object not found"},
            )
        return httpx.Response(
            200,
            headers={
                "content-length": str(len(data)),
                "etag": hashlib.md5(data, usedforsecurity=False).hexdigest(),
            },
        )

    def _post(self, relative: str, request: httpx.Request) -> httpx.Response:
        self.objects[relative] = request.read()
        return httpx.Response(200, json={"Key": relative})

    def _get(self, relative: str) -> httpx.Response:
        data = self.objects.get(relative)
        if data is None:
            return httpx.Response(400, json={"message": "Object not found"})
        return httpx.Response(200, content=data)

    def _list(self, request: httpx.Request) -> httpx.Response:
        body = json.loads(request.content)
        prefix = body.get("prefix", "")
        entries: dict[str, dict] = {}
        for key, data in self.objects.items():
            if not key.startswith(prefix):
                continue
            remainder = key[len(prefix):]
            if not remainder:
                continue
            name = remainder.split("/", 1)[0]
            if "/" in remainder:
                entries.setdefault(name, {"name": name, "id": None, "metadata": None})
            else:
                entries[name] = {"name": name, "id": "fake-id", "metadata": {"size": len(data)}}
        return httpx.Response(200, json=list(entries.values()))

    def _delete(self, request: httpx.Request) -> httpx.Response:
        body = json.loads(request.content)
        removed = []
        for prefix in body.get("prefixes", []):
            if prefix in self.objects:
                del self.objects[prefix]
                removed.append(prefix)
        return httpx.Response(200, json=removed)

    def transport(self) -> httpx.MockTransport:
        return httpx.MockTransport(self.handler)

    def uploads(self) -> list[str]:
        return [self._relative(request) for request in self.requests if request.method == "POST" and request.url.path.startswith(OBJECT_PREFIX)]

    def request_for(self, method: str, relative: str) -> httpx.Request:
        return next(
            request
            for request in self.requests
            if request.method == method and request.url.path.startswith(OBJECT_PREFIX) and self._relative(request) == relative
        )


def write_package(tmp_path: Path, files: dict[str, bytes] = FILES) -> Path:
    root = tmp_path / "publish"
    for relative, content in files.items():
        path = root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(content)
    return root


def config() -> StorageConfig:
    return StorageConfig(url=URL, service_key=SERVICE_KEY)


def run(root: Path, storage: FakeStorage, *, dry_run: bool = False, prune: bool = True):
    lines: list[str] = []
    report = upload_package(
        root, config(), dry_run=dry_run, prune=prune, echo=lines.append, transport=storage.transport()
    )
    return report, lines


def test_catalog_is_uploaded_last(tmp_path):
    root = write_package(tmp_path)
    storage = FakeStorage()

    report, _ = run(root, storage)

    assert report.uploaded == len(FILES)
    assert storage.uploads()[-1] == "catalog.json"
    assert storage.uploads()[:-1] == sorted(name for name in FILES if name != "catalog.json")


def test_versioned_files_have_immutable_cache_headers_and_catalog_is_no_cache(tmp_path):
    root = write_package(tmp_path)
    storage = FakeStorage()

    run(root, storage)

    clip = storage.request_for("POST", "lessons/lesson_001/v1/guided.mp3")
    assert clip.headers["cache-control"] == VERSIONED_CACHE_CONTROL
    assert clip.headers["content-type"] == "audio/mpeg"
    assert clip.headers["x-upsert"] == "false"
    assert clip.headers["authorization"] == f"Bearer {SERVICE_KEY}"

    lesson = storage.request_for("POST", "lessons/lesson_001/v1/lesson.json")
    assert lesson.headers["cache-control"] == VERSIONED_CACHE_CONTROL
    assert lesson.headers["content-type"] == "application/json"

    catalog = storage.request_for("POST", "catalog.json")
    assert catalog.headers["cache-control"] == CATALOG_CACHE_CONTROL
    assert catalog.headers["content-type"] == "application/json"
    assert catalog.headers["x-upsert"] == "true"


def test_unchanged_versioned_file_is_skipped(tmp_path):
    root = write_package(tmp_path)
    storage = FakeStorage({"lessons/lesson_001/v1/guided.mp3": FILES["lessons/lesson_001/v1/guided.mp3"]})

    report, _ = run(root, storage)

    assert "lessons/lesson_001/v1/guided.mp3" not in storage.uploads()
    assert report.skipped == 1
    assert report.uploaded == 2
    assert "catalog.json" in storage.uploads()


@pytest.mark.parametrize(
    "remote",
    [
        b"short",
        b"ID3\x04\x00\x00fake-audit",  # same length as the local file, different bytes
    ],
)
def test_changed_versioned_file_is_never_overwritten(tmp_path, remote):
    root = write_package(tmp_path)
    storage = FakeStorage({"lessons/lesson_001/v1/guided.mp3": remote})

    with pytest.raises(UploadError, match="immutable"):
        run(root, storage)

    assert storage.uploads() == []
    assert storage.objects.get("catalog.json") != FILES["catalog.json"]


def test_verification_checks_every_new_version_file_from_the_public_endpoint(tmp_path):
    root = write_package(tmp_path)
    storage = FakeStorage()

    run(root, storage)

    get_requests = [request for request in storage.requests if request.method == "GET"]
    fetched = {unquote(request.url.path).split(PUBLIC_PREFIX, 1)[1] for request in get_requests}
    assert fetched == set(FILES)


def test_verification_failure_raises_and_deletes_nothing(tmp_path, monkeypatch):
    root = write_package(tmp_path)
    storage = FakeStorage()
    real_get = storage._get

    def tampered_get(relative: str) -> httpx.Response:
        if relative == "lessons/lesson_001/v1/guided.mp3":
            return httpx.Response(200, content=b"not-what-was-uploaded")
        return real_get(relative)

    monkeypatch.setattr(storage, "_get", tampered_get)

    with pytest.raises(UploadError, match="Verification failed"):
        run(root, storage)

    # Nothing was pruned: every uploaded object is still present.
    assert set(FILES).issubset(storage.objects)


def test_dry_run_reports_size_for_every_file(tmp_path):
    root = write_package(tmp_path)
    storage = FakeStorage()

    _, lines = run(root, storage, dry_run=True)

    for relative, content in FILES.items():
        line = next(line for line in lines if relative in line)
        assert f"{len(content):,} B" in line


def test_missing_catalog_fails_before_any_request(tmp_path):
    root = write_package(tmp_path)
    (root / "catalog.json").unlink()

    with pytest.raises(UploadError, match="catalog.json is missing"):
        package_files(root)


def test_missing_bucket_points_at_migrations(tmp_path):
    root = write_package(tmp_path)

    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(400, json={"statusCode": "404", "message": "Bucket not found"})

    with pytest.raises(UploadError, match="apply supabase/migrations"):
        upload_package(root, config(), transport=httpx.MockTransport(handler))


def test_service_key_never_appears_in_cli_output(tmp_path, monkeypatch, capsys):
    root = write_package(tmp_path)
    monkeypatch.setenv("SUPABASE_URL", URL)
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", SERVICE_KEY)

    def failing(request: httpx.Request) -> httpx.Response:
        return httpx.Response(500, json={"message": "internal error"})

    monkeypatch.setattr(
        upload, "_client", lambda transport=None: httpx.Client(transport=httpx.MockTransport(failing))
    )

    code = main(["upload", "--out", str(root)])
    captured = capsys.readouterr()

    assert code == 1
    assert "Error:" in captured.err
    assert SERVICE_KEY not in captured.out
    assert SERVICE_KEY not in captured.err
