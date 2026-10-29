-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION 008: SECURITY HARDENING & DATA INTEGRITY
-- Tahap 8: Audit Keamanan, Pencabutan Hak Akses Publik pada RPC Otoritatif,
-- Proteksi Anti-Bypass Service Role, & Penegakan Invarian Transaksi Finansial
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Pengetatan Hak Akses (REVOKE) Fungsi Otoritatif Pembayaran & Kuota AI
-- Menutup celah default PostgreSQL di mana PUBLIC/AUTHENTICATED memiliki izin EXECUTE
-- -----------------------------------------------------------------------------

-- A. Fungsi Aktivasi Pembayaran
revoke execute on function public.activate_subscription_for_payment(uuid, text, text, text, text, integer, boolean) from public, anon, authenticated;
grant execute on function public.activate_subscription_for_payment(uuid, text, text, text, text, integer, boolean) to service_role;

-- B. Fungsi Lifecycle Langganan
revoke execute on function public.handle_subscription_lifecycle_event(uuid, text, text, text, text) from public, anon, authenticated;
grant execute on function public.handle_subscription_lifecycle_event(uuid, text, text, text, text) to service_role;

-- C. Fungsi Log Idempotensi Webhook
revoke execute on function public.record_payment_webhook_event(text, text, text, jsonb) from public, anon, authenticated;
grant execute on function public.record_payment_webhook_event(text, text, text, jsonb) to service_role;

-- D. Fungsi Reservasi & Finalisasi Kuota AI
revoke execute on function public.reserve_ai_quota(uuid, text, text) from public, anon, authenticated;
grant execute on function public.reserve_ai_quota(uuid, text, text) to service_role;

revoke execute on function public.finalize_ai_quota(uuid, text, boolean) from public, anon, authenticated;
grant execute on function public.finalize_ai_quota(uuid, text, boolean) to service_role;

-- E. Helper Testing
revoke execute on function public.test_set_user_subscription(uuid, text, integer, text) from public, anon, authenticated;
grant execute on function public.test_set_user_subscription(uuid, text, integer, text) to service_role;

-- -----------------------------------------------------------------------------
-- 2. Hardening RPC: activate_subscription_for_payment
-- Menambahkan Guard Service Role & Pencegahan Re-Aktivasi Transaksi yang Sudah 'paid'
-- -----------------------------------------------------------------------------
create or replace function public.activate_subscription_for_payment(
  p_user_id uuid,
  p_payment_ref text,
  p_provider text,
  p_provider_payment_id text,
  p_plan_code text,
  p_duration_days integer,
  p_auto_renew boolean default false
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_plan record;
  v_tx record;
  v_sub record;
  v_new_expires timestamptz;
  v_is_extended boolean := false;
  v_sub_id uuid;
begin
  -- Guard 1: Hanya service_role yang berhak mengeksekusi aktivasi langganan
  if auth.role() is not null and auth.role() != 'service_role' then
    raise exception 'Akses ditolak: Hanya backend service_role yang diizinkan mengaktifkan langganan'
      using errcode = '42501';
  end if;

  -- 1. Ambil plan id resmi dari subscription_plans
  select id, code, name, billing_period into v_plan
  from public.subscription_plans
  where code = p_plan_code and is_active = true;

  if v_plan.id is null then
    raise exception 'Plan code % tidak valid atau tidak aktif', p_plan_code
      using errcode = 'P0002';
  end if;

  -- 2. Ambil record transaksi pembayaran
  select id, status, amount_idr, subscription_id into v_tx
  from public.payment_transactions
  where payment_reference = p_payment_ref and user_id = p_user_id;

  if v_tx.id is null then
    raise exception 'Transaksi pembayaran dengan referensi % tidak ditemukan', p_payment_ref
      using errcode = 'P0002';
  end if;

  -- Guard 2: Cegah eksekusi ulang pada transaksi yang statusnya sudah 'paid'
  if v_tx.status = 'paid' and v_tx.subscription_id is not null then
    return jsonb_build_object(
      'success', true,
      'is_duplicate', true,
      'message', 'Transaksi sudah berstatus paid dan telah diaktifkan sebelumnya.',
      'subscription_id', v_tx.subscription_id
    );
  end if;

  -- Perbarui status transaksi menjadi 'paid'
  update public.payment_transactions
  set status = 'paid',
      paid_at = coalesce(paid_at, now()),
      provider_payment_id = coalesce(p_provider_payment_id, provider_payment_id),
      updated_at = now()
  where id = v_tx.id;

  -- 3. Cek apakah pengguna sudah memiliki langganan aktif untuk plan ini
  select id, status, expires_at into v_sub
  from public.subscriptions
  where user_id = p_user_id and plan_id = v_plan.id and status = 'active'
  order by created_at desc
  limit 1;

  if v_sub.id is not null and v_sub.expires_at is not null and v_sub.expires_at > now() then
    -- Perpanjang masa aktif secara kumulatif
    v_new_expires := v_sub.expires_at + (p_duration_days || ' days')::interval;
    v_is_extended := true;
    v_sub_id := v_sub.id;

    update public.subscriptions
    set expires_at = v_new_expires,
        provider = p_provider,
        provider_subscription_ref = coalesce(p_provider_payment_id, provider_subscription_ref),
        auto_renew = p_auto_renew,
        cancel_at_period_end = false,
        last_payment_transaction_id = v_tx.id,
        last_verified_at = now(),
        updated_at = now()
  where id = v_sub.id;
  else
    -- Buat masa aktif baru dari now()
    v_new_expires := now() + (p_duration_days || ' days')::interval;
    v_is_extended := false;

    insert into public.subscriptions (
      user_id,
      plan_id,
      status,
      starts_at,
      expires_at,
      provider,
      provider_subscription_ref,
      auto_renew,
      cancel_at_period_end,
      last_payment_transaction_id,
      last_verified_at,
      created_at,
      updated_at
    )
    values (
      p_user_id,
      v_plan.id,
      'active',
      now(),
      v_new_expires,
      p_provider,
      p_provider_payment_id,
      p_auto_renew,
      false,
      v_tx.id,
      now(),
      now(),
      now()
    )
    returning id into v_sub_id;
  end if;

  -- 4. Hubungkan transaksi dengan subscription id
  update public.payment_transactions
  set subscription_id = v_sub_id
  where id = v_tx.id;

  return jsonb_build_object(
    'success', true,
    'subscription_id', v_sub_id,
    'plan_code', v_plan.code,
    'plan_name', v_plan.name,
    'status', 'active',
    'expires_at', v_new_expires,
    'is_extended', v_is_extended,
    'activated_at', now()
  );
end;
$$;

revoke execute on function public.activate_subscription_for_payment(uuid, text, text, text, text, integer, boolean) from public, anon, authenticated;
grant execute on function public.activate_subscription_for_payment(uuid, text, text, text, text, integer, boolean) to service_role;

-- -----------------------------------------------------------------------------
-- 3. Hardening RPC: handle_subscription_lifecycle_event
-- -----------------------------------------------------------------------------
create or replace function public.handle_subscription_lifecycle_event(
  p_user_id uuid,
  p_provider text,
  p_provider_ref text,
  p_event_type text,
  p_reason text default null
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_sub record;
  v_new_status text;
begin
  -- Guard: Hanya service_role yang berhak mengubah siklus langganan
  if auth.role() is not null and auth.role() != 'service_role' then
    raise exception 'Akses ditolak: Hanya backend service_role yang diizinkan mengelola siklus langganan'
      using errcode = '42501';
  end if;

  select id, status, expires_at into v_sub
  from public.subscriptions
  where user_id = p_user_id
    and (provider_subscription_ref = p_provider_ref or provider = p_provider)
  order by updated_at desc
  limit 1;

  if v_sub.id is null then
    return jsonb_build_object('success', false, 'error', 'Subscription tidak ditemukan');
  end if;

  case p_event_type
    when 'cancel' then
      update public.subscriptions
      set auto_renew = false,
          cancel_at_period_end = true,
          updated_at = now()
      where id = v_sub.id;
      v_new_status := v_sub.status;

    when 'grace_period' then
      update public.subscriptions
      set status = 'grace_period',
          updated_at = now()
      where id = v_sub.id;
      v_new_status := 'grace_period';

    when 'revoke', 'refund' then
      update public.subscriptions
      set status = 'revoked',
          auto_renew = false,
          updated_at = now()
      where id = v_sub.id;
      v_new_status := 'revoked';

    when 'expire' then
      update public.subscriptions
      set status = 'expired',
          auto_renew = false,
          updated_at = now()
      where id = v_sub.id;
      v_new_status := 'expired';

    else
      return jsonb_build_object('success', false, 'error', 'Event type tidak dikenali: ' || p_event_type);
  end case;

  return jsonb_build_object(
    'success', true,
    'subscription_id', v_sub.id,
    'event_type', p_event_type,
    'status', v_new_status,
    'updated_at', now()
  );
end;
$$;

revoke execute on function public.handle_subscription_lifecycle_event(uuid, text, text, text, text) from public, anon, authenticated;
grant execute on function public.handle_subscription_lifecycle_event(uuid, text, text, text, text) to service_role;
