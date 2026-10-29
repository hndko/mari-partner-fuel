-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION 007: PAYMENT INTEGRATION & SUBSCRIPTION ACTIVATION
-- Tahap 7: Dual-Track Payment Gateways (Google Play Billing + QRISpy),
-- Server-Authoritative Verification, Idempotency & Lifecycle Event State Machine
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Penyesuaian Tabel payment_transactions
-- -----------------------------------------------------------------------------
alter table public.payment_transactions
  drop constraint if exists payment_transactions_provider_check;

alter table public.payment_transactions
  add constraint payment_transactions_provider_check
  check (provider in ('google_play', 'qrispy', 'midtrans', 'xendit', 'sandbox'));

alter table public.payment_transactions
  add column if not exists product_id text;

alter table public.payment_transactions
  add column if not exists base_plan_id text;

alter table public.payment_transactions
  add column if not exists billing_period text;

alter table public.payment_transactions
  add column if not exists metadata jsonb not null default '{}'::jsonb;

create index if not exists idx_payment_transactions_user_created
  on public.payment_transactions(user_id, created_at desc);

-- -----------------------------------------------------------------------------
-- 2. Penyesuaian Tabel subscriptions (Relasi ke Transaksi Pembayaran Terakhir)
-- -----------------------------------------------------------------------------
alter table public.subscriptions
  add column if not exists last_payment_transaction_id uuid
  references public.payment_transactions(id) on delete set null;

-- -----------------------------------------------------------------------------
-- 3. Fungsi Atomik: activate_subscription_for_payment
-- Mengaktifkan atau memperpanjang langganan Pro secara transaksional
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
  -- 1. Ambil plan id resmi dari subscription_plans
  select id, code, name, billing_period into v_plan
  from public.subscription_plans
  where code = p_plan_code and is_active = true;

  if v_plan.id is null then
    raise exception 'Plan code % tidak valid atau tidak aktif', p_plan_code
      using errcode = 'P0002';
  end if;

  -- 2. Ambil atau buat record transaksi pembayaran
  select id, status, amount_idr, subscription_id into v_tx
  from public.payment_transactions
  where payment_reference = p_payment_ref and user_id = p_user_id;

  if v_tx.id is not null then
    -- Perbarui transaksi menjadi 'paid'
    update public.payment_transactions
    set status = 'paid',
        paid_at = coalesce(paid_at, now()),
        provider_payment_id = coalesce(p_provider_payment_id, provider_payment_id),
        updated_at = now()
    where id = v_tx.id;
  end if;

  -- 3. Cek apakah pengguna sudah memiliki langganan aktif untuk plan ini
  select id, status, expires_at into v_sub
  from public.subscriptions
  where user_id = p_user_id and plan_id = v_plan.id and status = 'active'
  order by created_at desc
  limit 1;

  if v_sub.id is not null and v_sub.expires_at is not null and v_sub.expires_at > now() then
    -- Perpanjang dari tanggal kedaluwarsa yang sedang berjalan
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
    -- Buat masa aktif baru dari waktu sekarang (now())
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

  -- 4. Hubungkan transaksi dengan subscription jika ada record transaksi
  if v_tx.id is not null then
    update public.payment_transactions
    set subscription_id = v_sub_id
    where id = v_tx.id;
  end if;

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

grant execute on function public.activate_subscription_for_payment(uuid, text, text, text, text, integer, boolean) to service_role;

-- -----------------------------------------------------------------------------
-- 4. Fungsi: handle_subscription_lifecycle_event
-- Menangani state machine renewal, cancel, grace_period, expire, revoke & refund
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
      -- Pembatalan perpanjangan otomatis: hak akses tetap aktif sampai akhir periode (graceful expiry)
      update public.subscriptions
      set auto_renew = false,
          cancel_at_period_end = true,
          updated_at = now()
      where id = v_sub.id;
      v_new_status := v_sub.status;

    when 'grace_period' then
      -- Masuk masa tenggang penagihan
      update public.subscriptions
      set status = 'grace_period',
          updated_at = now()
      where id = v_sub.id;
      v_new_status := 'grace_period';

    when 'revoke', 'refund' then
      -- Refund atau pencabutan akses segera oleh provider (Google Play/Merchant)
      update public.subscriptions
      set status = 'revoked',
          auto_renew = false,
          updated_at = now()
      where id = v_sub.id;
      v_new_status := 'revoked';

    when 'expire' then
      -- Kedaluwarsa
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

grant execute on function public.handle_subscription_lifecycle_event(uuid, text, text, text, text) to service_role;

-- -----------------------------------------------------------------------------
-- 5. Fungsi Atomik Idempotensi Webhook: record_payment_webhook_event
-- -----------------------------------------------------------------------------
create or replace function public.record_payment_webhook_event(
  p_provider text,
  p_event_id text,
  p_payment_reference text,
  p_payload jsonb
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_inserted_id uuid;
begin
  insert into public.payment_webhook_events (
    provider,
    event_id,
    payment_reference,
    payload,
    processed_at
  )
  values (
    p_provider,
    p_event_id,
    p_payment_reference,
    p_payload,
    now()
  )
  on conflict (provider, event_id) do nothing
  returning id into v_inserted_id;

  if v_inserted_id is null then
    -- Event sudah pernah diproses sebelumnya (Idempotent Hit)
    return jsonb_build_object(
      'is_duplicate', true,
      'processed', false,
      'message', 'Event duplikat diabaikan secara aman.'
    );
  else
    return jsonb_build_object(
      'is_duplicate', false,
      'processed', true,
      'event_id', v_inserted_id
    );
  end if;
end;
$$;

grant execute on function public.record_payment_webhook_event(text, text, text, jsonb) to service_role;
