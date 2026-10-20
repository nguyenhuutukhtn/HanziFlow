-- Progress tables for the Mandarin course app (see docs/app_architecture.md "Supabase").
-- Clients write only their own rows; no row may be deleted through the API.

create table if not exists public.lesson_progress (
  user_id    uuid        not null references auth.users on delete cascade,
  lesson_id  text        not null,
  status     text        not null check (status in ('started', 'listened', 'passed')),
  passed_at  timestamptz,
  updated_at timestamptz not null default now(),
  primary key (user_id, lesson_id)
);

create table if not exists public.test_attempts (
  id                uuid        primary key, -- generated on the client
  user_id           uuid        not null references auth.users on delete cascade,
  lesson_id         text        not null,
  lesson_version    int         not null,
  generator_version int         not null,
  seed              bigint      not null,
  question_count    int         not null,
  mistakes          int         not null,
  passed            boolean     not null,
  started_at        timestamptz not null,
  finished_at       timestamptz not null,
  client            text        not null check (client in ('android', 'web')),
  device_id         text        not null,
  updated_at        timestamptz not null default now()
);

alter table public.lesson_progress enable row level security;
alter table public.test_attempts   enable row level security;

-- lesson_progress: the owner may read, insert and update; there is no delete policy.
create policy "lesson_progress_select_own"
  on public.lesson_progress for select
  to authenticated
  using (user_id = auth.uid());

create policy "lesson_progress_insert_own"
  on public.lesson_progress for insert
  to authenticated
  with check (user_id = auth.uid());

create policy "lesson_progress_update_own"
  on public.lesson_progress for update
  to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

-- test_attempts: attempts are immutable, so no delete policy exists at all.
create policy "test_attempts_select_own"
  on public.test_attempts for select
  to authenticated
  using (user_id = auth.uid());

create policy "test_attempts_insert_own"
  on public.test_attempts for insert
  to authenticated
  with check (user_id = auth.uid());

create policy "test_attempts_update_own"
  on public.test_attempts for update
  to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

-- updated_at is server-controlled: clients cannot backdate a row, and
-- last-write-wins sync compares this value.
create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

create trigger lesson_progress_set_updated_at
  before insert or update on public.lesson_progress
  for each row execute function public.set_updated_at();

create trigger test_attempts_set_updated_at
  before insert or update on public.test_attempts
  for each row execute function public.set_updated_at();

-- Sync pulls rows newer than the last cursor, per user.
create index lesson_progress_user_updated_idx
  on public.lesson_progress (user_id, updated_at);

create index test_attempts_user_updated_idx
  on public.test_attempts (user_id, updated_at);
