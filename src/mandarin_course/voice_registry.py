"""ElevenLabs voice registry: synced once, cached locally, used for deterministic per-lesson voice selection."""

from __future__ import annotations

import hashlib
import json
from dataclasses import asdict, dataclass
from pathlib import Path


@dataclass(frozen=True)
class VoiceProfile:
    voice_id: str
    name: str
    language: str
    accent: str | None = None
    gender: str | None = None


def short_language(language: str) -> str:
    return language.split("-")[0].lower()


def sync_registry(api_key: str, destination: Path) -> list[VoiceProfile]:
    """Fetch every voice already in the account's library and save it locally.

    This lists voices; it does not synthesize audio and is not a paid TTS request.
    Run manually (`mandarin-course voices sync`) when the account's voice library
    changes — not on every `generate` run.
    """
    from elevenlabs.client import ElevenLabs

    client = ElevenLabs(api_key=api_key)
    profiles: list[VoiceProfile] = []
    next_page_token: str | None = None
    while True:
        response = client.voices.search(page_size=100, next_page_token=next_page_token)
        for voice in response.voices:
            labels = voice.labels or {}
            profiles.append(
                VoiceProfile(
                    voice_id=voice.voice_id,
                    name=voice.name,
                    language=labels.get("language", "unknown"),
                    accent=labels.get("accent"),
                    gender=labels.get("gender"),
                )
            )
        if not response.has_more or not response.next_page_token:
            break
        next_page_token = response.next_page_token
    profiles.sort(key=lambda profile: profile.voice_id)
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(
        json.dumps([asdict(profile) for profile in profiles], ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    return profiles


def load_registry(path: Path) -> list[VoiceProfile]:
    if not path.is_file():
        return []
    data = json.loads(path.read_text(encoding="utf-8"))
    return [VoiceProfile(**entry) for entry in data]


def preferred_accent_for_level(level: str, language: str) -> str | None:
    """Lessons 1–2 (absolute_beginner) give top priority to a Beijing Mandarin voice
    while the learner is still establishing speaker identity. Later lessons broaden to
    whatever accents the registry has, per `lesson_authoring_guidelines.md`."""
    if language == "zh-CN" and level == "absolute_beginner":
        return "beijing"
    return None


# Known character genders, used as a priority tier when picking a voice for that
# role. This is a stopgap for the current fixed Speaker names; it should be
# replaced by the planned named-person registry (lesson_authoring_guidelines.md),
# where gender comes from the registry entry itself instead of being hardcoded here.
KNOWN_ROLE_GENDER: dict[str, str] = {
    "anna": "female",
    "li_ming": "male",
}


def _role_seed(lesson_id: str, version: int, role: str) -> str:
    return hashlib.sha256(f"{lesson_id}:{version}:{role}".encode("utf-8")).hexdigest()


def pick_voices_for_roles(
    profiles: list[VoiceProfile],
    *,
    language: str,
    lesson_id: str,
    version: int,
    roles: list[str],
    prefer_accent: str | None = None,
) -> dict[str, VoiceProfile]:
    """Assign one voice per role for a single segment/turns list.

    Voices are ranked, not hard-filtered: a known-gender match for the role (e.g.
    `anna` is female, `li_ming` is male) is the top-priority tier, a `prefer_accent`
    match (e.g. Beijing for lessons 1-2) is the second tier, but every voice stays a
    usable fallback. Roles claim a voice one at a time in a deterministic order (by
    hash of lesson/version/role); each claim re-ranks whatever is left in the pool
    for that role's own preference, then removes the choice from the pool, so
    distinct roles get distinct voices whenever enough exist rather than every role
    collapsing onto the single top-priority voice.
    """
    short = short_language(language)
    candidates = [profile for profile in profiles if short_language(profile.language) == short]
    if not candidates:
        return {}

    claim_order = sorted(dict.fromkeys(roles), key=lambda role: _role_seed(lesson_id, version, role))

    def tier_for(role: str):
        expected_gender = KNOWN_ROLE_GENDER.get(role)

        def tier(profile: VoiceProfile) -> tuple[int, int, str]:
            gender_ok = expected_gender is None or profile.gender == expected_gender
            accent_ok = bool(prefer_accent and profile.accent and prefer_accent in profile.accent.lower())
            return (0 if gender_ok else 1, 0 if accent_ok else 1, profile.voice_id)

        return tier

    assignment: dict[str, VoiceProfile] = {}
    pool = list(candidates)
    for role in claim_order:
        if not pool:
            pool = list(candidates)
        chosen = min(pool, key=tier_for(role))
        assignment[role] = chosen
        pool.remove(chosen)
    return assignment


def load_pinned_cast(path: Path) -> dict[str, str]:
    if not path.is_file():
        return {}
    return json.loads(path.read_text(encoding="utf-8"))


def save_pinned_cast(path: Path, cast: dict[str, str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(cast, ensure_ascii=False, indent=2, sort_keys=True), encoding="utf-8")


def resolve_roles_with_pinning(
    profiles: list[VoiceProfile],
    *,
    language: str,
    lesson_id: str,
    version: int,
    roles: list[str],
    prefer_accent: str | None,
    cast_path: Path,
) -> dict[str, VoiceProfile | None]:
    """Like `pick_voices_for_roles`, but once a role's voice has been picked for this
    lesson, it is pinned to `cast_path` and reused forever after — even if the
    registry grows or shrinks later. Growing the account's voice library must not
    retroactively change the cast (and invalidate the TTS cache) of an already
    generated lesson.
    """
    pinned = load_pinned_cast(cast_path)
    by_id = {profile.voice_id: profile for profile in profiles}
    resolved: dict[str, VoiceProfile | None] = {}
    unresolved_roles: list[str] = []
    for role in roles:
        key = f"{language}:{role}"
        if key in pinned and pinned[key] in by_id:
            resolved[role] = by_id[pinned[key]]
        else:
            unresolved_roles.append(role)

    if unresolved_roles:
        # Avoid reusing a voice already pinned to a different role for this lesson+
        # language — whether pinned just now (a sibling in this same call) or in an
        # earlier call for a different segment. Without this, two roles that never
        # share a single segment/turns list (e.g. a narrator role and a character
        # role that never speak in the same `turns`) could each independently and
        # deterministically land on the same single top-priority voice.
        already_pinned_ids = {
            voice_id for key, voice_id in pinned.items() if key.startswith(f"{language}:")
        } | {profile.voice_id for profile in resolved.values() if profile}
        available = [p for p in profiles if p.voice_id not in already_pinned_ids] or profiles
        fresh = pick_voices_for_roles(
            available,
            language=language,
            lesson_id=lesson_id,
            version=version,
            roles=unresolved_roles,
            prefer_accent=prefer_accent,
        )
        changed = False
        for role in unresolved_roles:
            profile = fresh.get(role)
            resolved[role] = profile
            if profile:
                pinned[f"{language}:{role}"] = profile.voice_id
                changed = True
        if changed:
            save_pinned_cast(cast_path, pinned)
    return resolved


def pick_voice(
    profiles: list[VoiceProfile],
    *,
    language: str,
    lesson_id: str,
    version: int,
    role: str,
    prefer_accent: str | None = None,
) -> VoiceProfile | None:
    """Single-role convenience wrapper around `pick_voices_for_roles`."""
    assignment = pick_voices_for_roles(
        profiles,
        language=language,
        lesson_id=lesson_id,
        version=version,
        roles=[role],
        prefer_accent=prefer_accent,
    )
    return assignment.get(role)
