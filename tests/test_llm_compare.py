"""Tests for scripts/llm_compare.py. No network: HTTP is monkeypatched."""

from __future__ import annotations

import importlib.util
import json
import re
import sys
from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parent.parent
SCRIPT = ROOT / "scripts" / "llm_compare.py"


@pytest.fixture(scope="module")
def lc():
    spec = importlib.util.spec_from_file_location("llm_compare", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module  # dataclasses needs the module registered
    spec.loader.exec_module(module)
    return module


def _reference(lc_module):
    return json.loads(lc_module.REFERENCE.read_text(encoding="utf-8"))


def test_prompt_renders_every_placeholder(lc):
    system, template = lc.load_prompt()
    values = {k: f"<{k}>" for k in ["hanzi", "pinyin", "meaning_vi", "context_before", "context_after"]}
    rendered = lc.render_prompt(template, values)
    assert "{{" not in rendered
    assert system and "{{" not in system
    for k in values:
        assert f"<{k}>" in rendered


def test_prompt_template_has_placeholders(lc):
    _, template = lc.load_prompt()
    assert len(re.findall(r"\{\{\w+\}\}", template)) >= 5


def test_env_parse_and_process_override(lc, tmp_path, monkeypatch):
    env_file = tmp_path / ".env"
    env_file.write_text("# comment\nGROQ_API_KEY=\"abc123\"\nGEMINI_MODEL=gemini-x\n\nEMPTY\n", encoding="utf-8")
    monkeypatch.setenv("GEMINI_MODEL", "from-process")
    values = lc.load_env(env_file)
    assert values["GROQ_API_KEY"] == "abc123"
    assert values["GEMINI_MODEL"] == "from-process"
    assert "EMPTY" not in values


def test_provider_without_key_is_skipped(lc):
    env = {"GROQ_API_KEY": "k-groq", "GROQ_MODEL": "m1"}
    enabled = lc.enabled_providers(env)
    assert [p.name for p, _, _ in enabled] == ["groq"]
    assert enabled[0][2] == "m1"


def test_gemini_without_model_reports_missing_model(lc):
    enabled = lc.enabled_providers({"GEMINI_API_KEY": "k-gem"})
    assert enabled[0][0].name == "gemini"
    assert enabled[0][2] is None  # main() prints "set GEMINI_MODEL; see --list-models gemini"


def test_pick_lines_includes_hall_line(lc):
    lines = lc.pick_lines(_reference(lc))
    assert len(lines) == 5
    assert any("大厅" in line["hanzi"] for line in lines)
    assert len({line["hanzi"] for line in lines}) == 5


def test_one_provider_error_does_not_stop_others(lc, tmp_path, monkeypatch):
    secret = "SECRET-KEY-VALUE-XYZ"

    def fake_http(method, url, headers, body=None, timeout=60):
        if "generativelanguage" in url:
            raise RuntimeError(f"HTTP 500 for Authorization: Bearer {secret}")
        return {"choices": [{"message": {"content": "ok answer"}}]}

    monkeypatch.setattr(lc, "http_json", fake_http)
    lines = lc.pick_lines(_reference(lc))[:2]
    providers = [
        (lc.PROVIDERS[0], secret, "gem-model"),
        (lc.PROVIDERS[1], "other-key", "groq-model"),
    ]
    report = lc.run_compare(lines, providers, "sys", "user {{hanzi}}")
    assert "### gemini · gem-model" in report
    assert "### groq · groq-model" in report
    assert "ok answer" in report
    assert "ERROR:" in report
    assert secret not in report


def test_output_contains_each_provider_heading_and_no_key(lc, monkeypatch):
    secrets = ["KEY-AAA-111", "KEY-BBB-222", "KEY-CCC-333"]

    def fake_http(method, url, headers, body=None, timeout=60):
        return {"choices": [{"message": {"content": f"answer from {url}"}}]}

    monkeypatch.setattr(lc, "http_json", fake_http)
    lines = lc.pick_lines(_reference(lc))
    providers = [(p, k, f"{p.name}-model") for p, k in zip(lc.PROVIDERS, secrets)]
    report = lc.run_compare(lines, providers, "sys", "user {{hanzi}}")
    for p in lc.PROVIDERS:
        assert f"### {p.name} · {p.name}-model" in report
    assert report.count("## ") >= 5
    for s in secrets:
        assert s not in report


def test_dry_run_makes_no_network_call(lc, monkeypatch, capsys):
    def boom(*args, **kwargs):
        raise AssertionError("network call during dry-run")

    monkeypatch.setattr(lc, "http_json", boom)
    assert lc.main(["--dry-run"]) == 0
    out = capsys.readouterr().out
    assert "大厅" in out
