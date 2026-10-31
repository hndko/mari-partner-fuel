-- =============================================================================
-- Migration: 20261031000005_subscription_plans_promotional_pricing.sql
-- Keterangan:
-- 1. Penambahan fitur harga promo pada tabel subscription_plans (is_promo_active, promo_price_idr, promo_max_per_user, promo_label).
-- 2. Dukungan tracking pembelian promo pada payment_transactions (is_promo).
-- 3. Pemutakhiran RPC admin_update_subscription_plan untuk mengelola promo dari Owner SaaS Console.
-- 4. Fungsi otoritatif get_available_subscription_plans untuk menghitung eligibilitas promo per pengguna.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Penambahan Kolom Promo pada subscription_plans
-- -----------------------------------------------------------------------------
alter table public.subscription_plans
  add column if not exists is_promo_active boolean not null default false,
  add column if not exists promo_price_idr bigint default null check (promo_price_idr is null or promo_price_idr >= 0),
  add column if not exists promo_max_per_user integer default null check (promo_max_per_user is null or promo_max_per_user >= 0),
  add column if not exists promo_label text default 'PROMO SPESIAL';

-- -----------------------------------------------------------------------------
-- 2. Penambahan Kolom is_promo pada payment_transactions
-- -----------------------------------------------------------------------------
alter table public.payment_transactions
  add column if not exists is_promo boolean not null default false;

-- -----------------------------------------------------------------------------
-- 3. Pemutakhiran admin_update_subscription_plan
-- Mendukung pembaruan parameter promo oleh Administrator / SaaS Owner
-- -----------------------------------------------------------------------------
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
    'name', v_updated.name,
    'price_idr', v_updated.price_idr,
    'ai_scan_quota', v_updated.ai_scan_quota,
    'max_vehicles', v_updated.max_vehicles,
    'is_active', v_updated.is_active,
    'is_promo_active', v_updated.is_promo_active,
    'promo_price_idr', v_updated.promo_price_idr,
    'promo_max_per_user', v_updated.promo_max_per_user,
    'promo_label', v_updated.promo_label
  );
end;
$$;

grant execute on function public.admin_update_subscription_plan(uuid, text, bigint, integer, integer, boolean, text, boolean, bigint, integer, text) to authenticated, service_role;

-- -----------------------------------------------------------------------------
-- 4. Fungsi Otoritatif: get_available_subscription_plans
-- Mengambil katalog paket beserta kalkulasi eligibilitas promo per pengguna
-- -----------------------------------------------------------------------------
create or replace function public.get_available_subscription_plans(p_user_id uuid default null)
returns table (
  id uuid,
  code text,
  name text,
  billing_period text,
  price_idr bigint,
  ai_scan_quota integer,
  max_vehicles integer,
  is_active boolean,
  features jsonb,
  description text,
  is_promo_active boolean,
  promo_price_idr bigint,
  promo_max_per_user integer,
  promo_label text,
  user_promo_purchases_count integer,
  is_user_eligible_for_promo boolean,
  effective_price_idr bigint
)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_target_user uuid := coalesce(p_user_id, auth.uid());
begin
  return query
  select 
    sp.id,
    sp.code,
    sp.name,
    sp.billing_period,
    sp.price_idr,
    sp.ai_scan_quota,
    sp.max_vehicles,
    sp.is_active,
    sp.features,
    sp.description,
    sp.is_promo_active,
    sp.promo_price_idr,
    sp.promo_max_per_user,
    sp.promo_label,
    coalesce(user_promo.purchases_count, 0)::integer as user_promo_purchases_count,
    (
      case
        when not sp.is_promo_active or sp.promo_price_idr is null then false
        when sp.promo_max_per_user is null or sp.promo_max_per_user <= 0 then true
        when coalesce(user_promo.purchases_count, 0) < sp.promo_max_per_user then true
        else false
      end
    )::boolean as is_user_eligible_for_promo,
    (
      case
        when sp.is_promo_active and sp.promo_price_idr is null then sp.price_idr
        when sp.is_promo_active and (sp.promo_max_per_user is null or sp.promo_max_per_user <= 0 or coalesce(user_promo.purchases_count, 0) < sp.promo_max_per_user)
          then sp.promo_price_idr
        else sp.price_idr
      end
    )::bigint as effective_price_idr
  from public.subscription_plans sp
  left join lateral (
    select count(*)::integer as purchases_count
    from public.payment_transactions pt
    where pt.user_id = v_target_user
      and pt.status = 'paid'
      and pt.is_promo = true
      and (
        pt.metadata->>'plan_code' = sp.code
        or pt.product_id = sp.code
      )
  ) user_promo on true
  where sp.is_active = true
  order by sp.price_idr asc;
end;
$$;

grant execute on function public.get_available_subscription_plans(uuid) to authenticated, anon, service_role;
