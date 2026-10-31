-- =============================================================================
-- Migration: 20261031000004_subscription_auto_expire_and_reminder_rules.sql
-- Keterangan: 
-- 1. Logika pemrosesan otomatis langganan kedaluwarsa (auto-expire & graceful fallback ke Free).
-- 2. Pengarsipan aman armada aktif di atas batas Free Tier (2 unit) dengan garansi Zero Data Loss.
-- 3. Deteksi pengingat masa aktif Pro mendekati habis (H-7) dan notifikasi transisi kedaluwarsa.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Prosedur: process_expired_subscriptions
-- Memproses langganan Pro yang telah melewati masa aktif secara otomatis
-- -----------------------------------------------------------------------------
create or replace function public.process_expired_subscriptions(p_target_user_id uuid default null)
returns integer
language plpgsql
security definer
set search_path = public
as $$
declare
  v_expired_count integer := 0;
  v_user_record record;
  v_active_pro_exists boolean;
  v_excess_vehicle record;
begin
  -- 1. Tandai seluruh subscription yang melewati expires_at sebagai 'expired'
  with updated as (
    update public.subscriptions
    set status = 'expired',
        auto_renew = false,
        updated_at = now()
    where status = 'active'
      and expires_at is not null
      and expires_at <= now()
      and (p_target_user_id is null or user_id = p_target_user_id)
    returning user_id
  )
  select count(*) into v_expired_count from updated;

  -- 2. Untuk setiap user yang mengalami kedaluwarsa, pastikan invarian Free Tier terpenuhi
  for v_user_record in
    select distinct user_id
    from public.subscriptions
    where status = 'expired'
      and (p_target_user_id is null or user_id = p_target_user_id)
  loop
    -- Periksa apakah user memiliki subscription Pro lain yang masih aktif
    select exists (
      select 1
      from public.subscriptions
      where user_id = v_user_record.user_id
        and status = 'active'
        and (expires_at is null or expires_at > now())
    ) into v_active_pro_exists;

    -- Jika user tidak lagi memiliki Pro aktif (turun ke Free Tier)
    if not v_active_pro_exists then
      -- Jika kendaraan aktif melebihi batas Free Tier (2 unit):
      -- Arsipkan kendaraan ke-3 dan seterusnya secara aman tanpa menghapus data
      update public.vehicles
      set is_archived = true,
          updated_at = now()
      where user_id = v_user_record.user_id
        and is_archived = false
        and id not in (
          select id from public.vehicles
          where user_id = v_user_record.user_id
            and is_archived = false
          order by created_at asc
          limit 2
        );
    end if;
  end loop;

  return v_expired_count;
end;
$$;

grant execute on function public.process_expired_subscriptions(uuid) to authenticated, service_role;

-- -----------------------------------------------------------------------------
-- 2. Pemutakhiran get_user_effective_entitlement
-- Mengekspos is_expiring_soon, days_until_expiry, dan is_recently_expired
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
  v_days_left integer := null;
  v_is_expiring_soon boolean := false;
  v_recently_expired boolean := false;
begin
  -- Otorisasi: hanya pemilik data atau service_role yang diizinkan
  if auth.uid() is not null and auth.uid() != p_user_id then
    raise exception 'Akses ditolak: User ID tidak cocok' using errcode = '42501';
  end if;

  -- Jalankan pembersihan subscription kedaluwarsa secara otomatis
  perform public.process_expired_subscriptions(p_user_id);

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
    if v_sub.expires_at is not null then
      v_days_left := greatest(0, ceil(extract(epoch from (v_sub.expires_at - now())) / 86400)::integer);
      v_is_expiring_soon := (v_sub.expires_at <= now() + interval '7 days');
    else
      v_days_left := null;
      v_is_expiring_soon := false;
    end if;

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
      'features', v_sub.features,
      'is_expiring_soon', v_is_expiring_soon,
      'days_until_expiry', v_days_left,
      'is_recently_expired', false
    );
  else
    -- Pengguna berstatus Free (Default)
    -- Periksa apakah user baru saja kedaluwarsa dalam 7 hari terakhir
    select exists (
      select 1
      from public.subscriptions
      where user_id = p_user_id
        and status = 'expired'
        and expires_at is not null
        and expires_at >= now() - interval '7 days'
    ) into v_recently_expired;

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
      'features', coalesce(v_plan.features, '["manual_transactions", "local_ocr", "export_csv", "vehicles_limit_2", "ai_scans_5"]'::jsonb),
      'is_expiring_soon', false,
      'days_until_expiry', null,
      'is_recently_expired', coalesce(v_recently_expired, false)
    );
  end if;

  return v_result;
end;
$$;

grant execute on function public.get_user_effective_entitlement(uuid) to authenticated, service_role;
