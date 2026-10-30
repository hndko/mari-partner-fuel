-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION 011: SAAS OWNER PLAN & FINANCIAL TX MANAGEMENT
-- Tahap 11: Manajemen Paket Langganan (Pricing, Kuota AI, Batas Kendaraan),
-- dan Log Pemantauan Transaksi Pembayaran Riil untuk Pemilik SaaS
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. RPC Otoritatif: admin_update_subscription_plan
-- Mengizinkan Pemilik SaaS mengubah harga paket, kuota AI, batas armada, & status
-- -----------------------------------------------------------------------------
create or replace function public.admin_update_subscription_plan(
  p_plan_id uuid,
  p_name text,
  p_price_idr bigint,
  p_ai_scan_quota integer,
  p_max_vehicles integer,
  p_is_active boolean,
  p_description text default null
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_caller_id uuid;
  v_updated record;
begin
  v_caller_id := auth.uid();
  if v_caller_id is null or not public.is_admin_or_owner(v_caller_id) then
    raise exception 'Akses ditolak: Hanya Owner/Admin yang diizinkan memperbarui paket langganan'
      using errcode = '42501';
  end if;

  if p_price_idr < 0 then
    raise exception 'Harga tidak boleh negatif' using errcode = '22003';
  end if;

  if p_ai_scan_quota < 0 then
    raise exception 'Kuota scan AI tidak boleh negatif' using errcode = '22003';
  end if;

  if p_max_vehicles is not null and p_max_vehicles <= 0 then
    raise exception 'Batas kendaraan harus lebih dari 0 atau null (unlimited)' using errcode = '22003';
  end if;

  update public.subscription_plans
  set name = coalesce(p_name, name),
      price_idr = p_price_idr,
      ai_scan_quota = p_ai_scan_quota,
      max_vehicles = p_max_vehicles,
      is_active = p_is_active,
      description = coalesce(p_description, description)
  where id = p_plan_id
  returning * into v_updated;

  if v_updated.id is null then
    raise exception 'Paket langganan dengan ID % tidak ditemukan', p_plan_id
      using errcode = 'P0002';
  end if;

  return jsonb_build_object(
    'success', true,
    'message', 'Paket ' || v_updated.name || ' berhasil diperbarui.',
    'plan_id', v_updated.id,
    'code', v_updated.code,
    'name', v_updated.name,
    'price_idr', v_updated.price_idr,
    'ai_scan_quota', v_updated.ai_scan_quota,
    'max_vehicles', v_updated.max_vehicles,
    'is_active', v_updated.is_active
  );
end;
$$;

-- -----------------------------------------------------------------------------
-- 2. RPC Otoritatif: admin_get_payment_transactions
-- Mengambil riwayat transaksi pembayaran masuk dari semua pengguna untuk audit
-- -----------------------------------------------------------------------------
create or replace function public.admin_get_payment_transactions(
  p_limit integer default 50
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_caller_id uuid;
  v_tx_list jsonb;
begin
  v_caller_id := auth.uid();
  if v_caller_id is null or not public.is_admin_or_owner(v_caller_id) then
    raise exception 'Akses ditolak: Hanya Owner/Admin yang diizinkan mengakses log transaksi keuangan'
      using errcode = '42501';
  end if;

  select coalesce(
    jsonb_agg(
      jsonb_build_object(
        'id', pt.id,
        'user_id', pt.user_id,
        'user_display_name', coalesce(p.display_name, 'Pengguna Mari'),
        'provider', pt.provider,
        'payment_reference', pt.payment_reference,
        'amount_idr', pt.amount_idr,
        'status', pt.status,
        'created_at', pt.created_at,
        'paid_at', pt.paid_at,
        'plan_name', coalesce(sp.name, pt.product_id, 'Paket Pro')
      ) order by pt.created_at desc
    ), '[]'::jsonb
  ) into v_tx_list
  from public.payment_transactions pt
  left join public.profiles p on pt.user_id = p.id
  left join public.subscriptions sub on pt.subscription_id = sub.id
  left join public.subscription_plans sp on sub.plan_id = sp.id
  limit coalesce(p_limit, 50);

  return jsonb_build_object(
    'success', true,
    'transactions', v_tx_list
  );
end;
$$;

grant execute on function public.admin_update_subscription_plan(uuid, text, bigint, integer, integer, boolean, text) to authenticated;
grant execute on function public.admin_get_payment_transactions(integer) to authenticated;
