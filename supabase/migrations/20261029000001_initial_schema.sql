-- =============================================================================
-- Migration: 20261029000001_initial_schema.sql
-- Proyek: Mari Partner Fuel
-- Deskripsi: Skema basis data 13 tabel relasional, constraint, index,
--            trigger otomatis, dan data awal (seed) paket langganan.
-- =============================================================================

-- Pastikan extension pgcrypto aktif untuk gen_random_uuid()
create extension if not exists "pgcrypto";

-- -----------------------------------------------------------------------------
-- 0. Fungsi Trigger Konsisten untuk updated_at
-- -----------------------------------------------------------------------------
create or replace function public.handle_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

-- -----------------------------------------------------------------------------
-- 1. Tabel: profiles
-- -----------------------------------------------------------------------------
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  timezone text not null default 'Asia/Jakarta',
  theme_preference text not null default 'system'
    check (theme_preference in ('light', 'dark', 'system')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create trigger trg_profiles_updated_at
  before update on public.profiles
  for each row execute function public.handle_updated_at();

-- -----------------------------------------------------------------------------
-- 2. Tabel: vehicles
-- -----------------------------------------------------------------------------
create table if not exists public.vehicles (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  vehicle_type text not null
    check (vehicle_type in ('motorcycle', 'car', 'other')),
  make text,
  model text,
  license_plate text,
  default_fuel_type text,
  tank_capacity_liters numeric(8,2)
    check (tank_capacity_liters is null or tank_capacity_liters > 0),
  last_odometer_km numeric(12,1)
    check (last_odometer_km is null or last_odometer_km >= 0),
  is_archived boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (id, user_id)
);

create index if not exists idx_vehicles_user_active
  on public.vehicles(user_id, is_archived);

create trigger trg_vehicles_updated_at
  before update on public.vehicles
  for each row execute function public.handle_updated_at();

-- -----------------------------------------------------------------------------
-- 3. Tabel: receipt_files (Metadata foto struk privat)
-- -----------------------------------------------------------------------------
create table if not exists public.receipt_files (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  storage_path text not null unique,
  mime_type text not null,
  size_bytes bigint not null check (size_bytes > 0),
  sha256 text,
  created_at timestamptz not null default now(),
  unique (id, user_id)
);

create index if not exists idx_receipt_files_user
  on public.receipt_files(user_id);

-- -----------------------------------------------------------------------------
-- 4. Tabel: fuel_transactions
-- -----------------------------------------------------------------------------
create table if not exists public.fuel_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  vehicle_id uuid not null,
  receipt_file_id uuid,
  transaction_at timestamptz not null,
  station_name text,
  fuel_type text,
  volume_liters numeric(10,3) not null check (volume_liters > 0),
  total_amount_idr bigint not null check (total_amount_idr >= 0),
  price_per_liter_idr numeric(12,2)
    check (price_per_liter_idr is null or price_per_liter_idr >= 0),
  odometer_km numeric(12,1)
    check (odometer_km is null or odometer_km >= 0),
  scan_status text not null default 'manual'
    check (scan_status in ('manual', 'ocr_review', 'ai_review', 'confirmed')),
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  foreign key (vehicle_id, user_id)
    references public.vehicles(id, user_id) on delete restrict,
  foreign key (receipt_file_id, user_id)
    references public.receipt_files(id, user_id) on delete set null
);

create index if not exists idx_fuel_transactions_user_date
  on public.fuel_transactions(user_id, transaction_at desc);

create index if not exists idx_fuel_transactions_vehicle_date
  on public.fuel_transactions(vehicle_id, transaction_at desc);

create trigger trg_fuel_transactions_updated_at
  before update on public.fuel_transactions
  for each row execute function public.handle_updated_at();

-- -----------------------------------------------------------------------------
-- 5. Tabel: receipt_scan_jobs
-- -----------------------------------------------------------------------------
create table if not exists public.receipt_scan_jobs (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  receipt_file_id uuid references public.receipt_files(id) on delete set null,
  provider text not null
    check (provider in ('mlkit_local', 'gemini_cloud', 'openai_cloud')),
  status text not null default 'pending'
    check (status in ('pending', 'processing', 'completed', 'failed')),
  raw_extracted_text text,
  structured_result jsonb,
  error_message text,
  duration_ms integer check (duration_ms is null or duration_ms >= 0),
  created_at timestamptz not null default now()
);

create index if not exists idx_receipt_scan_jobs_user
  on public.receipt_scan_jobs(user_id, created_at desc);

-- -----------------------------------------------------------------------------
-- 6. Tabel: maintenance_reminders
-- -----------------------------------------------------------------------------
create table if not exists public.maintenance_reminders (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  vehicle_id uuid not null references public.vehicles(id) on delete cascade,
  title text not null,
  reminder_type text not null
    check (reminder_type in ('odometer', 'date', 'both')),
  target_odometer_km numeric(12,1)
    check (target_odometer_km is null or target_odometer_km >= 0),
  target_date date,
  is_completed boolean not null default false,
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_maintenance_reminders_user_active
  on public.maintenance_reminders(user_id, is_completed);

create trigger trg_maintenance_reminders_updated_at
  before update on public.maintenance_reminders
  for each row execute function public.handle_updated_at();

-- -----------------------------------------------------------------------------
-- 7. Tabel: notification_preferences
-- -----------------------------------------------------------------------------
create table if not exists public.notification_preferences (
  id uuid primary key references auth.users(id) on delete cascade,
  fuel_reminders_enabled boolean not null default true,
  maintenance_alerts_enabled boolean not null default true,
  promo_alerts_enabled boolean not null default false,
  updated_at timestamptz not null default now()
);

create trigger trg_notification_preferences_updated_at
  before update on public.notification_preferences
  for each row execute function public.handle_updated_at();

-- -----------------------------------------------------------------------------
-- 8. Tabel: subscription_plans (Server-Authoritative Pricing)
-- -----------------------------------------------------------------------------
create table if not exists public.subscription_plans (
  id uuid primary key default gen_random_uuid(),
  code text not null unique,
  name text not null,
  billing_period text not null
    check (billing_period in ('monthly', 'yearly')),
  price_idr bigint not null check (price_idr >= 0),
  ai_scan_quota integer not null check (ai_scan_quota >= 0),
  max_vehicles integer check (max_vehicles is null or max_vehicles > 0),
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

-- -----------------------------------------------------------------------------
-- 9. Tabel: subscriptions
-- -----------------------------------------------------------------------------
create table if not exists public.subscriptions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  plan_id uuid not null references public.subscription_plans(id),
  status text not null default 'pending'
    check (status in ('pending', 'active', 'expired', 'cancelled', 'past_due')),
  starts_at timestamptz,
  expires_at timestamptz,
  provider text,
  provider_subscription_ref text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_subscriptions_user_status
  on public.subscriptions(user_id, status);

create trigger trg_subscriptions_updated_at
  before update on public.subscriptions
  for each row execute function public.handle_updated_at();

-- -----------------------------------------------------------------------------
-- 10. Tabel: payment_transactions
-- -----------------------------------------------------------------------------
create table if not exists public.payment_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  subscription_id uuid references public.subscriptions(id) on delete set null,
  provider text not null
    check (provider in ('qrispy', 'midtrans', 'xendit')),
  payment_reference text not null unique,
  provider_payment_id text,
  amount_idr bigint not null check (amount_idr >= 0),
  currency text not null default 'IDR' check (currency = 'IDR'),
  status text not null default 'created'
    check (status in ('created', 'pending', 'paid', 'expired', 'cancelled', 'failed', 'refunded')),
  checkout_url text,
  qris_image_url text,
  expires_at timestamptz,
  paid_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (provider, provider_payment_id)
);

create index if not exists idx_payment_transactions_user
  on public.payment_transactions(user_id);

create index if not exists idx_payment_transactions_reference
  on public.payment_transactions(payment_reference);

create trigger trg_payment_transactions_updated_at
  before update on public.payment_transactions
  for each row execute function public.handle_updated_at();

-- -----------------------------------------------------------------------------
-- 11. Tabel: payment_webhook_events (Idempotency Protection)
-- -----------------------------------------------------------------------------
create table if not exists public.payment_webhook_events (
  id uuid primary key default gen_random_uuid(),
  provider text not null,
  event_id text not null,
  payment_reference text not null,
  payload jsonb not null,
  processed_at timestamptz not null default now(),
  unique (provider, event_id)
);

create index if not exists idx_payment_webhook_events_reference
  on public.payment_webhook_events(payment_reference);

-- -----------------------------------------------------------------------------
-- 12. Tabel: ai_usage_logs
-- -----------------------------------------------------------------------------
create table if not exists public.ai_usage_logs (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  action text not null
    check (action in ('receipt_scan_fallback', 'query')),
  tokens_or_units integer not null default 1 check (tokens_or_units > 0),
  billing_month text not null, -- Format YYYY-MM
  created_at timestamptz not null default now()
);

create index if not exists idx_ai_usage_logs_user_month
  on public.ai_usage_logs(user_id, billing_month);

-- -----------------------------------------------------------------------------
-- 13. Tabel: user_feedback
-- -----------------------------------------------------------------------------
create table if not exists public.user_feedback (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  category text not null
    check (category in ('ocr_accuracy', 'app_bug', 'feature_request', 'general')),
  message text not null,
  rating integer check (rating is null or (rating >= 1 and rating <= 5)),
  app_version text,
  created_at timestamptz not null default now()
);

create index if not exists idx_user_feedback_user
  on public.user_feedback(user_id);

-- -----------------------------------------------------------------------------
-- 14. Trigger Otomatis Pembuatan Profil saat User Mendaftar (auth.users)
-- -----------------------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.profiles (id, display_name, timezone, theme_preference)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'full_name', new.raw_user_meta_data->>'name', split_part(new.email, '@', 1)),
    'Asia/Jakarta',
    'system'
  )
  on conflict (id) do nothing;

  insert into public.notification_preferences (id)
  values (new.id)
  on conflict (id) do nothing;

  return new;
end;
$$ language plpgsql security definer;

-- Trigger pada schema auth.users
drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- -----------------------------------------------------------------------------
-- 15. Seed Awal Paket Langganan Pro (Server-Authoritative Pricing)
-- -----------------------------------------------------------------------------
insert into public.subscription_plans (code, name, billing_period, price_idr, ai_scan_quota, max_vehicles, is_active)
values
  ('pro_monthly', 'Mari Pro Bulanan', 'monthly', 15000, 50, null, true),
  ('pro_yearly', 'Mari Pro Tahunan', 'yearly', 120000, 600, null, true)
on conflict (code) do update set
  name = excluded.name,
  billing_period = excluded.billing_period,
  price_idr = excluded.price_idr,
  ai_scan_quota = excluded.ai_scan_quota,
  max_vehicles = excluded.max_vehicles,
  is_active = excluded.is_active;
