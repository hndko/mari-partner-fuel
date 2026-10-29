-- =============================================================================
-- Migration: 20261029000002_rls_and_storage.sql
-- Proyek: Mari Partner Fuel
-- Deskripsi: Penegakan Row Level Security (RLS) pada 13 tabel publik,
--            isolasi data per-pengguna, proteksi mutasi client pada tabel
--            sensitif, serta konfigurasi bucket privat dan storage policies.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Aktivasi Row Level Security (RLS) pada Seluruh Tabel
-- -----------------------------------------------------------------------------
alter table public.profiles enable row level security;
alter table public.vehicles enable row level security;
alter table public.receipt_files enable row level security;
alter table public.fuel_transactions enable row level security;
alter table public.receipt_scan_jobs enable row level security;
alter table public.maintenance_reminders enable row level security;
alter table public.notification_preferences enable row level security;
alter table public.subscription_plans enable row level security;
alter table public.subscriptions enable row level security;
alter table public.payment_transactions enable row level security;
alter table public.payment_webhook_events enable row level security;
alter table public.ai_usage_logs enable row level security;
alter table public.user_feedback enable row level security;

-- -----------------------------------------------------------------------------
-- 2. Kebijakan RLS: profiles
-- -----------------------------------------------------------------------------
create policy "profiles_select_own"
  on public.profiles for select
  to authenticated
  using (auth.uid() = id);

create policy "profiles_update_own"
  on public.profiles for update
  to authenticated
  using (auth.uid() = id)
  with check (auth.uid() = id);

create policy "profiles_insert_own"
  on public.profiles for insert
  to authenticated
  with check (auth.uid() = id);

-- -----------------------------------------------------------------------------
-- 3. Kebijakan RLS: vehicles
-- -----------------------------------------------------------------------------
create policy "vehicles_select_own"
  on public.vehicles for select
  to authenticated
  using (auth.uid() = user_id);

create policy "vehicles_insert_own"
  on public.vehicles for insert
  to authenticated
  with check (auth.uid() = user_id);

create policy "vehicles_update_own"
  on public.vehicles for update
  to authenticated
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "vehicles_delete_own"
  on public.vehicles for delete
  to authenticated
  using (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 4. Kebijakan RLS: receipt_files
-- -----------------------------------------------------------------------------
create policy "receipt_files_select_own"
  on public.receipt_files for select
  to authenticated
  using (auth.uid() = user_id);

create policy "receipt_files_insert_own"
  on public.receipt_files for insert
  to authenticated
  with check (auth.uid() = user_id);

create policy "receipt_files_delete_own"
  on public.receipt_files for delete
  to authenticated
  using (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 5. Kebijakan RLS: fuel_transactions
-- -----------------------------------------------------------------------------
create policy "fuel_transactions_select_own"
  on public.fuel_transactions for select
  to authenticated
  using (auth.uid() = user_id);

create policy "fuel_transactions_insert_own"
  on public.fuel_transactions for insert
  to authenticated
  with check (auth.uid() = user_id);

create policy "fuel_transactions_update_own"
  on public.fuel_transactions for update
  to authenticated
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "fuel_transactions_delete_own"
  on public.fuel_transactions for delete
  to authenticated
  using (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 6. Kebijakan RLS: receipt_scan_jobs
-- -----------------------------------------------------------------------------
create policy "receipt_scan_jobs_select_own"
  on public.receipt_scan_jobs for select
  to authenticated
  using (auth.uid() = user_id);

create policy "receipt_scan_jobs_insert_own"
  on public.receipt_scan_jobs for insert
  to authenticated
  with check (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 7. Kebijakan RLS: maintenance_reminders
-- -----------------------------------------------------------------------------
create policy "maintenance_reminders_all_own"
  on public.maintenance_reminders for all
  to authenticated
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 8. Kebijakan RLS: notification_preferences
-- -----------------------------------------------------------------------------
create policy "notification_preferences_select_own"
  on public.notification_preferences for select
  to authenticated
  using (auth.uid() = id);

create policy "notification_preferences_update_own"
  on public.notification_preferences for update
  to authenticated
  using (auth.uid() = id)
  with check (auth.uid() = id);

-- -----------------------------------------------------------------------------
-- 9. Kebijakan RLS: subscription_plans (Public Read, Server-Only Write)
-- -----------------------------------------------------------------------------
create policy "subscription_plans_read_active"
  on public.subscription_plans for select
  to anon, authenticated
  using (is_active = true);

-- Dilarang INSERT/UPDATE/DELETE dari klien Android
-- Mutasi hanya diizinkan via backend service-role / migrations

-- -----------------------------------------------------------------------------
-- 10. Kebijakan RLS: subscriptions (User Read-Only, Server-Only Mutation)
-- -----------------------------------------------------------------------------
create policy "subscriptions_select_own"
  on public.subscriptions for select
  to authenticated
  using (auth.uid() = user_id);

-- Mutasi status subscription (active, expired, dll) DILARANG keras dari klien Android.
-- Hanya Supabase Edge Function berotentikasi service-role yang boleh mengubah.

-- -----------------------------------------------------------------------------
-- 11. Kebijakan RLS: payment_transactions (User Read-Only, Server-Only Mutation)
-- -----------------------------------------------------------------------------
create policy "payment_transactions_select_own"
  on public.payment_transactions for select
  to authenticated
  using (auth.uid() = user_id);

-- Klien Android dilarang INSERT/UPDATE status pembayaran secara langsung.
-- Order & perubahan status pembayaran hanya diproses via Edge Functions.

-- -----------------------------------------------------------------------------
-- 12. Kebijakan RLS: payment_webhook_events (Strictly Private to Server)
-- -----------------------------------------------------------------------------
-- Tidak ada policy untuk 'anon' maupun 'authenticated'.
-- Tabel ini hanya dapat diakses oleh Edge Functions dengan service_role.

-- -----------------------------------------------------------------------------
-- 13. Kebijakan RLS: ai_usage_logs (User Read, Server-Only Insert)
-- -----------------------------------------------------------------------------
create policy "ai_usage_logs_select_own"
  on public.ai_usage_logs for select
  to authenticated
  using (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 14. Kebijakan RLS: user_feedback
-- -----------------------------------------------------------------------------
create policy "user_feedback_insert_own"
  on public.user_feedback for insert
  to authenticated
  with check (auth.uid() = user_id);

create policy "user_feedback_select_own"
  on public.user_feedback for select
  to authenticated
  using (auth.uid() = user_id);

-- =============================================================================
-- 15. Konfigurasi Private Storage Bucket untuk Foto Struk
-- =============================================================================

-- Buat bucket 'receipts' berstatus privat (public = false) dengan batasan ukuran & MIME type
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'receipts',
  'receipts',
  false, -- Wajib privat; foto struk mengandung data finansial sensitif
  5242880, -- Maksimal 5 MB per berkas foto struk
  array['image/jpeg', 'image/png', 'image/webp']
)
on conflict (id) do update set
  public = false,
  file_size_limit = 5242880,
  allowed_mime_types = array['image/jpeg', 'image/png', 'image/webp'];

-- -----------------------------------------------------------------------------
-- 16. Kebijakan Akses Objek Storage (Row Level Security pada storage.objects)
-- Format Path: receipts/{user_id}/{filename}
-- -----------------------------------------------------------------------------

-- Pengguna hanya dapat mengunggah file ke direktori miliknya sendiri (receipts/{auth.uid()}/*)
create policy "receipts_storage_upload_own"
  on storage.objects for insert
  to authenticated
  with check (
    bucket_id = 'receipts' and
    (storage.foldername(name))[1] = auth.uid()::text
  );

-- Pengguna hanya dapat membaca foto struk di direktorinya sendiri
create policy "receipts_storage_select_own"
  on storage.objects for select
  to authenticated
  using (
    bucket_id = 'receipts' and
    (storage.foldername(name))[1] = auth.uid()::text
  );

-- Pengguna hanya dapat memperbarui foto struk di direktorinya sendiri
create policy "receipts_storage_update_own"
  on storage.objects for update
  to authenticated
  using (
    bucket_id = 'receipts' and
    (storage.foldername(name))[1] = auth.uid()::text
  );

-- Pengguna hanya dapat menghapus foto struk di direktorinya sendiri
create policy "receipts_storage_delete_own"
  on storage.objects for delete
  to authenticated
  using (
    bucket_id = 'receipts' and
    (storage.foldername(name))[1] = auth.uid()::text
  );
