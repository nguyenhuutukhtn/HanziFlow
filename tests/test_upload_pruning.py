"""Upload retry and retention tests."""

from __future__ import annotations

import json

from mandarin_course.upload import upload_package
from test_upload import FILES, FakeStorage, config, write_package


class StaleCatalogStorage(FakeStorage):
    def __init__(self, objects, stale_body, stale_reads):
        super().__init__(objects)
        self.stale_body = stale_body
        self.stale_reads = stale_reads

    def handler(self, request):
        if request.method == "GET" and request.url.path.endswith("/catalog.json") and not request.url.query:
            if self.stale_reads:
                self.stale_reads -= 1
                self.requests.append(request)
                return __import__("httpx").Response(200, content=self.stale_body)
        return super().handler(request)


def test_prune_keeps_current_and_previous_version(tmp_path):
    files = {
        "lessons/lesson_001/v1/lesson.json": b"v1",
        "lessons/lesson_001/v2/lesson.json": b"v2",
        "lessons/lesson_001/v3/lesson.json": b"v3",
        "catalog.json": json.dumps({"lessons": [{"id": "lesson_001", "version": 3}]}).encode(),
    }
    root = write_package(tmp_path, files)
    storage = FakeStorage({key: value for key, value in files.items() if "/v1/" in key or "/v2/" in key})

    report, _ = run(root, storage)

    assert report.pruned_remote == 1
    assert report.pruned_local == 1
    assert (root / "lessons/lesson_001/v2").exists()
    assert not (root / "lessons/lesson_001/v1").exists()
    assert not any("/v1/" in key for key in storage.objects)


def test_dry_run_uses_same_previous_version_retention(tmp_path):
    files = {
        "lessons/lesson_001/v1/lesson.json": b"v1",
        "lessons/lesson_001/v2/lesson.json": b"v2",
        "lessons/lesson_001/v3/lesson.json": b"v3",
        "catalog.json": json.dumps({"lessons": [{"id": "lesson_001", "version": 3}]}).encode(),
    }
    root = write_package(tmp_path, files)
    storage = FakeStorage({"lessons/lesson_001/v1/lesson.json": b"v1"})

    report, lines = run(root, storage, dry_run=True)

    assert report.pruned_local == 1
    assert any("lessons/lesson_001/v1/" in line for line in lines)
    assert not any("lessons/lesson_001/v2/" in line and "delete" in line for line in lines)


def test_no_prune_skips_deletion(tmp_path):
    root = write_package(tmp_path)
    storage = FakeStorage()
    report, lines = run(root, storage, prune=False)
    assert report.pruned_remote == report.pruned_local == 0
    assert any("--no-prune" in line for line in lines)


def run(root, storage, *, dry_run=False, prune=True):
    lines = []
    report = upload_package(root, config(), dry_run=dry_run, prune=prune, echo=lines.append, transport=storage.transport())
    return report, lines


def test_default_output_is_short_and_reports_counts(tmp_path):
    root = write_package(tmp_path)
    report, lines = run(root, FakeStorage())
    assert len(lines) <= 20
    assert any(line.startswith("Uploaded 3 file(s)") for line in lines)
    assert any(line.startswith("Verified: ok") for line in lines)
    assert report.verified is True


def test_unchanged_catalog_is_skipped_after_cache_busted_get(tmp_path):
    root = write_package(tmp_path)
    storage = FakeStorage(dict(FILES))
    report, _ = run(root, storage)
    assert report.uploaded == 0
    assert report.skipped == len(FILES)
    assert "catalog.json" not in storage.uploads()
    catalog_get = next(request for request in storage.requests if request.method == "GET")
    assert catalog_get.url.params["t"].isdigit()


def test_catalog_verification_retries_stale_public_responses(tmp_path):
    files = {
        "lessons/lesson_001/v2/lesson.json": b"v2",
        "catalog.json": json.dumps({"lessons": [{"id": "lesson_001", "version": 2}]}).encode(),
    }
    old_catalog = json.dumps({"lessons": [{"id": "lesson_001", "version": 1}]}).encode()
    root = write_package(tmp_path, files)
    storage = StaleCatalogStorage({"catalog.json": old_catalog}, old_catalog, 2)
    now = [0]
    report, _ = upload_package(root, config(), transport=storage.transport(), sleep=lambda seconds: now.__setitem__(0, now[0] + seconds), clock=lambda: now[0]) , []
    assert report.verified is True
    assert storage.stale_reads == 0


def test_catalog_verification_timeout_does_not_prune(tmp_path):
    files = {
        "lessons/lesson_001/v1/lesson.json": b"v1",
        "lessons/lesson_001/v2/lesson.json": b"v2",
        "catalog.json": json.dumps({"lessons": [{"id": "lesson_001", "version": 2}]}).encode(),
    }
    old_catalog = json.dumps({"lessons": [{"id": "lesson_001", "version": 1}]}).encode()
    root = write_package(tmp_path, files)
    storage = StaleCatalogStorage({"lessons/lesson_001/v1/lesson.json": b"v1", "catalog.json": old_catalog}, old_catalog, 99)
    now = [0]
    from mandarin_course.upload import UploadError
    import pytest
    with pytest.raises(UploadError, match="timed out"):
        upload_package(root, config(), transport=storage.transport(), sleep=lambda seconds: now.__setitem__(0, now[0] + seconds), clock=lambda: now[0])
    assert "lessons/lesson_001/v1/lesson.json" in storage.objects
    assert (root / "lessons/lesson_001/v1").exists()
