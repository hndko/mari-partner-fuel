-- =============================================================================
-- Migration: 20261031000007_add_updated_at_to_subscription_plans.sql
-- Keterangan:
-- 1. Menambahkan kolom updated_at pada tabel subscription_plans jika belum ada.
-- 2. Memperbarui RPC admin_update_subscription_plan agar aman memperbarui data paket.
-- =============================================================================

alter table public.subscription_plans
  add column if not exists updated_at timestamptz default now();

create or replace function public.admin_update_subscription_plan(
  p_plan_id uuid,
  p_name text,
  p_price_idr bigint,
  p_ai_scan_quota integer,
  p_max_vehicles integer default null,
  p_is_active boolean default true,
  p_description text default null,
  p_is_promo_active boolean default false,
  p_promo_price_idr bigint default null,
  p_promo_max_per_user integer default null,
  p_promo_label text default 'PROMO SPESIAL'
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_caller_id uuid := auth.uid();
  v_updated record;
begin
  -- 1. Validasi Akses Owner / Admin
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

  if p_promo_price_idr is not null and p_promo_price_idr < 0 then
    raise exception 'Harga promo tidak boleh negatif' using errcode = '22003';
  end if;

  if p_promo_max_per_user is not null and p_promo_max_per_user < 0 then
    raise exception 'Batas pembelian per user tidak boleh negatif' using errcode = '22003';
  end if;

  update public.subscription_plans
  set name = coalesce(p_name, name),
      price_idr = p_price_idr,
      ai_scan_quota = p_ai_scan_quota,
      max_vehicles = p_max_vehicles,
      is_active = p_is_active,
      description = coalesce(p_description, description),
      is_promo_active = coalesce(p_is_promo_active, false),
      promo_price_idr = p_promo_price_idr,
      promo_max_per_user = p_promo_max_per_user,
      promo_label = coalesce(nullif(trim(p_promo_label), ''), 'PROMO SPESIAL'),
      updated_at = now()
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
    'price_idr', v_updated.price_idr,
    'is_promo_active', v_updated.is_promo_active,
    'promo_price_idr', v_updated.promo_price_idr,
    'promo_max_per_user', v_updated.promo_max_per_user,
    'promo_label', v_updated.promo_label
  );
end;
$$;
