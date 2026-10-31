-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION: SUBSCRIPTION DOWNGRADE & RENEWAL RULES
-- Menjamin graceful downgrade (cancel_at_period_end), no immediate revocation,
-- dan server-authoritative renewal lifecycle
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Penambahan / Pemutakhiran Kolom cancel_at_period_end di subscriptions
-- -----------------------------------------------------------------------------
alter table public.subscriptions
  add column if not exists cancel_at_period_end boolean not null default false;

-- -----------------------------------------------------------------------------
-- 2. Fungsi: request_subscription_downgrade
-- Mencatat permohonan downgrade pengguna ke Free tanpa memutus hak akses Pro
-- -----------------------------------------------------------------------------
create or replace function public.request_subscription_downgrade(p_user_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_sub record;
  v_plan_name text;
begin
  -- Verifikasi otorisasi: hanya pemilik data atau service_role
  if auth.uid() is not null and auth.uid() != p_user_id then
    raise exception 'Akses ditolak: User ID tidak cocok' using errcode = '42501';
  end if;

  -- Cari langganan aktif saat ini
  select s.id, s.status, s.expires_at, s.cancel_at_period_end, p.name as plan_name
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

  if v_sub.id is null then
    return jsonb_build_object(
      'success', false,
      'error', 'Tidak ada langganan Pro aktif yang dapat di-downgrade. Akun Anda sudah berada pada paket Free.'
    );
  end if;

  -- Tandai langganan untuk tidak diperpanjang otomatis setelah masa aktif habis
  update public.subscriptions
  set cancel_at_period_end = true,
      auto_renew = false,
      updated_at = now()
  where id = v_sub.id;

  return jsonb_build_object(
    'success', true,
    'subscription_id', v_sub.id,
    'plan_name', v_sub.plan_name,
    'cancel_at_period_end', true,
    'expires_at', v_sub.expires_at,
    'message', 'Permintaan downgrade ke paket Free berhasil dicatat. Status ' || v_sub.plan_name || ' Anda tetap aktif sepenuhnya hingga masa berlaku berakhir.'
  );
end;
$$;

grant execute on function public.request_subscription_downgrade(uuid) to authenticated, service_role;

-- -----------------------------------------------------------------------------
-- 3. Fungsi: reactivate_subscription_auto_renew
-- Membatalkan permohonan downgrade sebelum masa aktif berakhir
-- -----------------------------------------------------------------------------
create or replace function public.reactivate_subscription_auto_renew(p_user_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_sub record;
begin
  -- Verifikasi otorisasi
  if auth.uid() is not null and auth.uid() != p_user_id then
    raise exception 'Akses ditolak: User ID tidak cocok' using errcode = '42501';
  end if;

  select s.id, s.status, s.expires_at, p.name as plan_name
  into v_sub
  from public.subscriptions s
  join public.subscription_plans p on p.id = s.plan_id
  where s.user_id = p_user_id
    and s.status = 'active'
    and (s.expires_at is null or s.expires_at > now())
  order by s.created_at desc
  limit 1;

  if v_sub.id is null then
    return jsonb_build_object(
      'success', false,
      'error', 'Tidak ada langganan Pro aktif yang ditemukan.'
    );
  end if;

  update public.subscriptions
  set cancel_at_period_end = false,
      auto_renew = true,
      updated_at = now()
  where id = v_sub.id;

  return jsonb_build_object(
    'success', true,
    'subscription_id', v_sub.id,
    'plan_name', v_sub.plan_name,
    'cancel_at_period_end', false,
    'expires_at', v_sub.expires_at,
    'message', 'Status perpanjangan otomatis untuk ' || v_sub.plan_name || ' berhasil diaktifkan kembali.'
  );
end;
$$;

grant execute on function public.reactivate_subscription_auto_renew(uuid) to authenticated, service_role;

-- -----------------------------------------------------------------------------
-- 4. Pemutakhiran get_user_effective_entitlement untuk mengekspos cancel_at_period_end
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
  select s.id, s.status, s.expires_at, s.auto_renew, s.cancel_at_period_end, s.plan_id,
         p.code as plan_code, p.name as plan_name, p.max_vehicles, p.ai_scan_quota, p.features
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
      'cancel_at_period_end', coalesce(v_sub.cancel_at_period_end, false),
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
      'cancel_at_period_end', false,
      'max_vehicles', coalesce(v_plan.max_vehicles, 2),
      'ai_scan_quota', coalesce(v_plan.ai_scan_quota, 5),
      'features', coalesce(v_plan.features, '["manual_transactions", "local_ocr", "export_csv", "vehicles_limit_2", "ai_scans_5"]'::jsonb)
    );
  end if;

  return v_result;
end;
$$;

grant execute on function public.get_user_effective_entitlement(uuid) to authenticated, service_role;
