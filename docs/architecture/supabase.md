# Architecture — Supabase

> Part of the architecture docs; index: [../app_architecture.md](../app_architecture.md).

## Supabase

```sql
create table lesson_progress (
  user_id      uuid not null references auth.users on delete cascade,
  lesson_id    text not null,
  status       text not null check (status in ('started', 'listened', 'passed')),
  passed_at    timestamptz,
  updated_at   timestamptz not null default now(),
  primary key (user_id, lesson_id)
);

create table test_attempts (
  id                 uuid primary key,          -- generated on the client
  user_id            uuid not null references auth.users on delete cascade,
  lesson_id          text not null,
  lesson_version     int  not null,
  generator_version  int  not null,
  seed               bigint not null,
  question_count     int  not null,
  mistakes           int  not null,
  passed             boolean not null,
  started_at         timestamptz not null,
  finished_at        timestamptz not null,
  client             text not null check (client in ('android', 'web')),
  device_id          text not null,
  updated_at         timestamptz not null default now()
);

alter table lesson_progress enable row level security;
alter table test_attempts  enable row level security;
-- each table: select/insert/update only where user_id = auth.uid(); no delete policy on test_attempts
```

Migrations live in `supabase/migrations/`. Lesson packages go in a public Storage bucket `course`; only the local upload step (service-role key from `.env`) writes to it.

Free-tier notes: projects pause after 7 days without activity (data is kept; resume from the dashboard). Android keeps working offline while paused. The Storage free tier (1 GB, 5 GB egress/month) is enough for personal use; if it stops being enough, move packages to Cloudflare R2 by changing the base URL.
