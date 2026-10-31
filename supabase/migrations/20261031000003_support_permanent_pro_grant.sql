-- =============================================================================
-- Migration: 20261031000003_support_permanent_pro_grant.sql
-- Keterangan: Mendukung opsi pemberian status Pro Permanen (Seumur Hidup / Lifetime)
--             secara manual dari konsol SaaS Pemilik (Owner Console).
-- =============================================================================

create or replace function public.admin_grant_user_pro(
  p_target_user_id uuid,
  p_duration_days integer,
  p_notes text default null
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_caller_id uuid;
  v_plan_id uuid;
  v_new_expires timestamptz;
  v_existing_sub record;
  v_is_extended boolean := false;
  v_is_permanent boolean := false;
begin
  v_caller_id := auth.uid();
  if v_caller_id is null or not public.is_admin_or_owner(v_caller_id) then
    raise exception 'Akses ditolak: Hanya Owner/Admin yang diizinkan memberikan status Pro'
      using errcode = '42501';
  end if;

  if p_duration_days is not null and p_duration_days < 0 then
    raise exception 'Durasi hari tidak valid' using errcode = '22003';
  end if;

  -- Jika p_duration_days is null atau 0, berarti aktivasi PRO PERMANEN (Seumur Hidup)
  if p_duration_days is null or p_duration_days = 0 then
    v_is_permanent := true;
    v_new_expires := null;
  end if;

  -- Pilih plan_id: jika permanen atau >= 365, gunakan pro_yearly (tier fitur terlengkap)
  if v_is_permanent or (p_duration_days is not null and p_duration_days >= 365) then
    select id into v_plan_id from public.subscription_plans where code = 'pro_yearly';
  else
    select id into v_plan_id from public.subscription_plans where code = 'pro_monthly';
  end if;

  if v_plan_id is null then
    select id into v_plan_id from public.subscription_plans where code != 'free' limit 1;
  end if;

  -- Cek subscription aktif pengguna
  select id, expires_at into v_existing_sub
  from public.subscriptions
  where user_id = p_target_user_id and status = 'active'
  order by created_at desc
  limit 1;

  if v_is_permanent then
    -- Status Pro Permanen: expires_at diset NULL
    if v_existing_sub.id is not null then
      update public.subscriptions
      set expires_at = null,
          plan_id = coalesce(v_plan_id, plan_id),
          provider = 'manual_owner_grant',
          provider_subscription_ref = coalesce(p_notes, 'Manual Owner Grant Permanen by ' || v_caller_id::text),
          cancel_at_period_end = false,
          auto_renew = false,
          updated_at = now()
      where id = v_existing_sub.id;
      v_is_extended := true;
    else
      insert into public.subscriptions (
        user_id,
        plan_id,
        status,
        starts_at,
        expires_at,
        provider,
        provider_subscription_ref
      ) values (
        p_target_user_id,
        v_plan_id,
        'active',
        now(),
        null,
        'manual_owner_grant',
        coalesce(p_notes, 'Manual Owner Grant Permanen by ' || v_caller_id::text)
      );
      v_is_extended := false;
    end if;
  else
    -- Status Pro Berdurasi Tertentu (30, 90, 365 hari dsb)
    if v_existing_sub.id is not null and v_existing_sub.expires_at is null then
      -- Jika akun saat ini sudah permanen, tetap biarkan permanen
      v_new_expires := null;
      v_is_extended := true;
      v_is_permanent := true;
    elsif v_existing_sub.id is not null and v_existing_sub.expires_at is not null and v_existing_sub.expires_at > now() then
      v_new_expires := v_existing_sub.expires_at + (p_duration_days || ' days')::interval;
      v_is_extended := true;

      update public.subscriptions
      set expires_at = v_new_expires,
          provider = 'manual_owner_grant',
          provider_subscription_ref = coalesce(p_notes, 'Manual Owner Grant by ' || v_caller_id::text),
          cancel_at_period_end = false,
          updated_at = now()
      where id = v_existing_sub.id;
    else
      v_new_expires := now() + (p_duration_days || ' days')::interval;
      v_is_extended := false;

      insert into public.subscriptions (
        user_id,
        plan_id,
        status,
        starts_at,
        expires_at,
        provider,
        provider_subscription_ref
      ) values (
        p_target_user_id,
        v_plan_id,
        'active',
        now(),
        v_new_expires,
        'manual_owner_grant',
        coalesce(p_notes, 'Manual Owner Grant by ' || v_caller_id::text)
      );
    end if;
  end if;

  return jsonb_build_object(
    'success', true,
    'is_extended', v_is_extended,
    'is_permanent', v_is_permanent,
    'target_user_id', p_target_user_id,
    'new_expires_at', v_new_expires,
    'duration_days_added', case when v_is_permanent then 0 else p_duration_days end,
    'message', case
      when v_is_permanent then 'Status Pro Permanen (Seumur Hidup) berhasil diaktifkan.'
      when v_is_extended then 'Masa aktif Pro berhasil diperpanjang.'
      else 'Status Pro aktif baru berhasil diberikan.'
    end
  );
end;
$$;

grant execute on function public.admin_grant_user_pro(uuid, integer, text) to authenticated;
