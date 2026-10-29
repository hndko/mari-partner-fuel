-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION 006: SUBSCRIPTION PLANS, ENTITLEMENTS & AI QUOTA
-- Tahap 6: Server-Authoritative Entitlements, Atomic AI Quota, and Vehicle Limit
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Penyesuaian Tabel subscription_plans
-- -----------------------------------------------------------------------------
alter table public.subscription_plans
  drop constraint if exists subscription_plans_billing_period_check;

alter table public.subscription_plans
  add constraint subscription_plans_billing_period_check
  check (billing_period in ('free', 'monthly', 'yearly', 'lifetime'));

alter table public.subscription_plans
  add column if not exists features jsonb not null default '[]'::jsonb;

alter table public.subscription_plans
  add column if not exists description text;

-- Seed / Update Katalog Paket Resmi (Server-Authoritative Pricing)
insert into public.subscription_plans (
  code, name, billing_period, price_idr, ai_scan_quota, max_vehicles, is_active, features, description
)
values
  (
    'free',
    'Mari Free',
    'free',
    0,
    5,
    2,
    true,
    '["manual_transactions", "local_ocr", "export_csv", "vehicles_limit_2", "ai_scans_5"]'::jsonb,
    'Paket esensial gratis untuk 2 kendaraan aktif dan 5 pemindaian struk AI per bulan.'
  ),
  (
    'pro_monthly',
    'Mari Pro Bulanan',
    'monthly',
    29000,
    100,
    null,
    true,
    '["manual_transactions", "local_ocr", "cloud_ai_extraction", "unlimited_vehicles", "ai_scans_100", "advanced_analytics", "export_csv", "priority_support"]'::jsonb,
    'Paket Pro fleksibel bulanan dengan kuota 100 pemindaian AI dan jumlah kendaraan tanpa batas.'
  ),
  (
    'pro_yearly',
    'Mari Pro Tahunan',
    'yearly',
    290000,
    100,
    null,
    true,
    '["manual_transactions", "local_ocr", "cloud_ai_extraction", "unlimited_vehicles", "ai_scans_100", "advanced_analytics", "export_csv", "priority_support", "discount_2_months"]'::jsonb,
    'Hemat 17% dengan paket tahunan. Nikmati seluruh fitur Pro tanpa batas kendaraan.'
  )
on conflict (code) do update set
  name = excluded.name,
  billing_period = excluded.billing_period,
  price_idr = excluded.price_idr,
  ai_scan_quota = excluded.ai_scan_quota,
  max_vehicles = excluded.max_vehicles,
  is_active = excluded.is_active,
  features = excluded.features,
  description = excluded.description;

-- -----------------------------------------------------------------------------
-- 2. Penyesuaian Tabel subscriptions
-- -----------------------------------------------------------------------------
alter table public.subscriptions
  drop constraint if exists subscriptions_status_check;

alter table public.subscriptions
  add constraint subscriptions_status_check
  check (status in ('pending', 'active', 'grace_period', 'cancelled', 'expired', 'on_hold', 'paused', 'revoked', 'past_due'));

alter table public.subscriptions
  add column if not exists auto_renew boolean not null default false;

alter table public.subscriptions
  add column if not exists cancel_at_period_end boolean not null default false;

alter table public.subscriptions
  add column if not exists last_verified_at timestamptz not null default now();

-- -----------------------------------------------------------------------------
-- 3. Penyesuaian Tabel ai_usage_logs (Atomic Ledger & Idempotency)
-- -----------------------------------------------------------------------------
alter table public.ai_usage_logs
  add column if not exists idempotency_key text;

alter table public.ai_usage_logs
  add column if not exists status text not null default 'confirmed'
  check (status in ('reserved', 'confirmed', 'released', 'failed'));

alter table public.ai_usage_logs
  add column if not exists provider text not null default 'gemini_cloud';

alter table public.ai_usage_logs
  add column if not exists updated_at timestamptz not null default now();

create unique index if not exists idx_ai_usage_logs_user_idempotency
  on public.ai_usage_logs(user_id, idempotency_key)
  where idempotency_key is not null;

create index if not exists idx_ai_usage_logs_user_month_status
  on public.ai_usage_logs(user_id, billing_month, status);

-- -----------------------------------------------------------------------------
-- 4. Fungsi: get_user_effective_entitlement
-- Menentukan hak akses pengguna secara server-authoritative
-- -----------------------------------------------------------------------------
create or replace function public.get_user_effective_entitlement(p_user_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_sub record;
  v_plan record;
  v_result jsonb;
begin
  -- Otorisasi: hanya pemilik data atau service_role yang diizinkan
  if auth.uid() is not null and auth.uid() != p_user_id then
    raise exception 'Akses ditolak: User ID tidak cocok' using errcode = '42501';
  end if;

  -- Cari subscription aktif atau dalam grace period
  select s.id, s.status, s.expires_at, s.auto_renew, s.plan_id, p.code as plan_code, p.name as plan_name,
         p.max_vehicles, p.ai_scan_quota, p.features
  into v_sub
  from public.subscriptions s
  join public.subscription_plans p on p.id = s.plan_id
  where s.user_id = p_user_id
    and (
      (s.status = 'active' and (s.expires_at is null or s.expires_at > now()))
      or
      (s.status = 'grace_period' and s.expires_at > now() - interval '3 days')
    )
  order by s.created_at desc
  limit 1;

  if v_sub.id is not null then
    -- Pengguna berstatus Pro aktif
    v_result := jsonb_build_object(
      'user_id', p_user_id,
      'is_pro', true,
      'plan_code', v_sub.plan_code,
      'plan_name', v_sub.plan_name,
      'status', v_sub.status,
      'expires_at', v_sub.expires_at,
      'auto_renew', v_sub.auto_renew,
      'max_vehicles', v_sub.max_vehicles,
      'ai_scan_quota', v_sub.ai_scan_quota,
      'features', v_sub.features
    );
  else
    -- Pengguna berstatus Free (Default)
    select code, name, max_vehicles, ai_scan_quota, features
    into v_plan
    from public.subscription_plans
    where code = 'free'
    limit 1;

    v_result := jsonb_build_object(
      'user_id', p_user_id,
      'is_pro', false,
      'plan_code', coalesce(v_plan.code, 'free'),
      'plan_name', coalesce(v_plan.name, 'Mari Free'),
      'status', 'active',
      'expires_at', null,
      'auto_renew', false,
      'max_vehicles', coalesce(v_plan.max_vehicles, 2),
      'ai_scan_quota', coalesce(v_plan.ai_scan_quota, 5),
      'features', coalesce(v_plan.features, '["manual_transactions", "local_ocr", "export_csv", "vehicles_limit_2", "ai_scans_5"]'::jsonb)
    );
  end if;

  return v_result;
end;
$$;

grant execute on function public.get_user_effective_entitlement(uuid) to authenticated, service_role;

-- -----------------------------------------------------------------------------
-- 5. Fungsi Atomik: reserve_ai_quota
-- Mencegah race condition dan pemakaian berlebih kuota AI cloud
-- -----------------------------------------------------------------------------
create or replace function public.reserve_ai_quota(
  p_user_id uuid,
  p_idempotency_key text,
  p_action text default 'receipt_scan_fallback'
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_month text;
  v_entitlement jsonb;
  v_quota_limit integer;
  v_used integer;
  v_existing record;
begin
  -- Otorisasi
  if auth.uid() is not null and auth.uid() != p_user_id then
    raise exception 'Akses ditolak: User ID tidak cocok' using errcode = '42501';
  end if;

  v_month := to_char(now(), 'YYYY-MM');

  -- Cek idempotensi: Jika key sudah ada dan berhasil direservasi/dikonfirmasi sebelumnya
  if p_idempotency_key is not null and length(trim(p_idempotency_key)) > 0 then
    select status, tokens_or_units into v_existing
    from public.ai_usage_logs
    where user_id = p_user_id and idempotency_key = p_idempotency_key;

    if v_existing.status in ('reserved', 'confirmed') then
      return jsonb_build_object(
        'allowed', true,
        'code', 'IDEMPOTENT_SUCCESS',
        'message', 'Request sudah diproses sebelumnya.'
      );
    end if;
  end if;

  -- Dapatkan batas kuota pengguna
  v_entitlement := public.get_user_effective_entitlement(p_user_id);
  v_quota_limit := (v_entitlement->>'ai_scan_quota')::integer;

  -- Kunci baris / hitung pemakaian bulan berjalan secara atomik
  select coalesce(sum(tokens_or_units), 0)::integer into v_used
  from public.ai_usage_logs
  where user_id = p_user_id
    and billing_month = v_month
    and status in ('reserved', 'confirmed');

  if v_used >= v_quota_limit then
    return jsonb_build_object(
      'allowed', false,
      'code', 'AI_QUOTA_EXCEEDED',
      'error', 'Kuota pemindaian AI bulanan Anda telah habis.',
      'limit', v_quota_limit,
      'used', v_used,
      'remaining', 0
    );
  end if;

  -- Catat reservasi kuota
  insert into public.ai_usage_logs (
    user_id,
    action,
    tokens_or_units,
    billing_month,
    idempotency_key,
    status,
    provider,
    created_at,
    updated_at
  )
  values (
    p_user_id,
    p_action,
    1,
    v_month,
    p_idempotency_key,
    'reserved',
    'gemini_cloud',
    now(),
    now()
  );

  return jsonb_build_object(
    'allowed', true,
    'code', 'OK',
    'limit', v_quota_limit,
    'used', v_used + 1,
    'remaining', v_quota_limit - (v_used + 1)
  );
end;
$$;

grant execute on function public.reserve_ai_quota(uuid, text, text) to authenticated, service_role;

-- -----------------------------------------------------------------------------
-- 6. Fungsi Atomik: finalize_ai_quota
-- Mengonfirmasi atau membebaskan reservasi jika provider AI gagal
-- -----------------------------------------------------------------------------
create or replace function public.finalize_ai_quota(
  p_user_id uuid,
  p_idempotency_key text,
  p_success boolean
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_new_status text;
  v_updated integer;
begin
  if auth.uid() is not null and auth.uid() != p_user_id then
    raise exception 'Akses ditolak: User ID tidak cocok' using errcode = '42501';
  end if;

  v_new_status := case when p_success then 'confirmed' else 'released' end;

  update public.ai_usage_logs
  set status = v_new_status,
      updated_at = now()
  where user_id = p_user_id
    and idempotency_key = p_idempotency_key
    and status = 'reserved';

  get diagnostics v_updated = row_count;

  return jsonb_build_object(
    'finalized', true,
    'status', v_new_status,
    'rows_affected', v_updated
  );
end;
$$;

grant execute on function public.finalize_ai_quota(uuid, text, boolean) to authenticated, service_role;

-- -----------------------------------------------------------------------------
-- 7. Trigger Enforce Vehicle Limit (Server-Side Invariant)
-- Mencegah penambahan kendaraan aktif melampaui batas paket Free
-- -----------------------------------------------------------------------------
create or replace function public.check_vehicle_limit()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_entitlement jsonb;
  v_max integer;
  v_active_count integer;
  v_plan_name text;
begin
  -- Hanya periksa jika kendaraan tidak diarsipkan
  if NEW.is_archived = false then
    v_entitlement := public.get_user_effective_entitlement(NEW.user_id);
    v_max := (v_entitlement->>'max_vehicles')::integer;
    v_plan_name := v_entitlement->>'plan_name';

    if v_max is not null then
      select count(*) into v_active_count
      from public.vehicles
      where user_id = NEW.user_id
        and is_archived = false
        and (TG_OP = 'INSERT' or id != NEW.id);

      if v_active_count >= v_max then
        raise exception 'VEHICLE_LIMIT_EXCEEDED: Paket % membatasi maksimal % kendaraan aktif.', v_plan_name, v_max
          using errcode = 'P0001';
      end if;
    end if;
  end if;

  return NEW;
end;
$$;

drop trigger if exists trg_enforce_vehicle_limit on public.vehicles;
create trigger trg_enforce_vehicle_limit
  before insert or update of is_archived on public.vehicles
  for each row execute function public.check_vehicle_limit();

-- -----------------------------------------------------------------------------
-- 8. Test Fixture Helper (Development Only - Strictly service_role)
-- -----------------------------------------------------------------------------
create or replace function public.test_set_user_subscription(
  p_user_id uuid,
  p_plan_code text,
  p_duration_days integer,
  p_status text default 'active'
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_plan_id uuid;
  v_sub_id uuid;
  v_expires timestamptz;
begin
  select id into v_plan_id from public.subscription_plans where code = p_plan_code;
  if v_plan_id is null then
    raise exception 'Plan code % tidak ditemukan', p_plan_code;
  end if;

  v_expires := case when p_duration_days is not null then now() + (p_duration_days || ' days')::interval else null end;

  insert into public.subscriptions (
    user_id,
    plan_id,
    status,
    starts_at,
    expires_at,
    provider,
    provider_subscription_ref,
    auto_renew
  )
  values (
    p_user_id,
    v_plan_id,
    p_status,
    now(),
    v_expires,
    'test_fixture',
    'dev_test_' || gen_random_uuid(),
    false
  )
  returning id into v_sub_id;

  return jsonb_build_object(
    'success', true,
    'subscription_id', v_sub_id,
    'plan_code', p_plan_code,
    'status', p_status,
    'expires_at', v_expires
  );
end;
$$;

-- Izin fungsi test_fixture: CABUT dari publik/klien biasa, HANYA izinkan service_role
revoke execute on function public.test_set_user_subscription(uuid, text, integer, text) from public, anon, authenticated;
grant execute on function public.test_set_user_subscription(uuid, text, integer, text) to service_role;
