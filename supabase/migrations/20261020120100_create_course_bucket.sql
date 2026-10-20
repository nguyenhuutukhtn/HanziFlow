-- Public read-only Storage bucket for published lesson packages.
-- Only the local upload command writes here, using the service-role key
-- (service_role bypasses RLS). No insert/update/delete policy exists for
-- anon or authenticated, so clients cannot modify published content.

insert into storage.buckets (id, name, public)
values ('course', 'course', true)
on conflict (id) do update set public = true;

create policy "course_public_read"
  on storage.objects for select
  to anon, authenticated
  using (bucket_id = 'course');
