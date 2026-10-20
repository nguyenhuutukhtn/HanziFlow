# App Architecture (index)

Supersedes the former `android_app_handoff.md`. Decisions were made on 2026-10-20.

Course content and teacher behavior follow [Lesson Audio Design Principles](lesson_design_principles.md). The apps play, test, and track that content; they do not replace the teacher script with generic UI exercises.

The architecture is split by topic so a task reads only what it needs. Older references of the form `app_architecture.md "<section>"` resolve through this table.

| Section | File |
|---|---|
| Product decisions, System overview, Repository layout, Web app, Phases, Out of scope, Open questions, References | [architecture/overview.md](architecture/overview.md) |
| Lesson package contract (catalog.json, reference.json, items.json), Versioning rule, Timeline contract (cues, sections) | [architecture/content_package.md](architecture/content_package.md) |
| Test generation, Pass rule | [architecture/testgen.md](architecture/testgen.md) |
| Android app: Download behavior, Playback, Headset and Bluetooth controls, Karaoke transcript, Chinese-only listening, Section replay, Progress sync, Navigation and MVP screens, Word lookup and copy, MVP acceptance criteria | [architecture/android.md](architecture/android.md) |
| Code architecture (Android) | [architecture/android_code.md](architecture/android_code.md) |
| Character learning (planned: decisions, draft design, data research) | [architecture/characters.md](architecture/characters.md) |
| Supabase (tables, RLS, Storage, free tier) | [architecture/supabase.md](architecture/supabase.md) |

UI design: [ui/README.md](ui/README.md). Process: [workflow.md](workflow.md). Current state: [STATUS.md](STATUS.md).
