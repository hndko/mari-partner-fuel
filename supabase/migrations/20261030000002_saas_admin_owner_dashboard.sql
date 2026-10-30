-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION 010: SAAS OWNER & ADMIN METRICS DASHBOARD
-- Tahap 10: Server-Authoritative SaaS Analytics, Owner Access Control,
-- KPI Metrics Aggregation, Conversion Stats & Manual Pro Subscription Grant
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Penambahan Kolom role pada Tabel profiles
-- -----------------------------------------------------------------------------
alter table public.profiles
  add column if not exists role text not null default 'user';

-- Tambahkan constraint check bila belum ada
do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'profiles_role_check'
  ) then
    alter table public.profiles
      add constraint profiles_role_check
      check (role in ('user', 'admin', 'owner'));
  end if;
end $$;

create index if not exists idx_profiles_role
  on public.profiles(role) where role in ('admin', 'owner');

-- -----------------------------------------------------------------------------
-- 2. Fungsi Pemeriksa Akses Owner / Admin: is_admin_or_owner
-- -----------------------------------------------------------------------------
create or replace function public.is_admin_or_owner(check_user_id uuid default auth.uid())
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.profiles
    where id = check_user_id and role in ('admin', 'owner')
  );
$$;

-- -----------------------------------------------------------------------------
-- 3. RPC Otoritatif: get_saas_owner_metrics
-- Mengembalikan agregasi metrik SaaS lengkap terlindung hak akses Owner
-- -----------------------------------------------------------------------------
create or replace function public.get_saas_owner_metrics(
  p_timeframe text default '30d'
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_caller_id uuid;
  v_total_users bigint;
  v_active_pro_users bigint;
  v_free_users bigint;
  v_total_revenue_idr bigint;
  v_total_transactions bigint;
  v_total_vehicles bigint;
  v_growth_trend jsonb;
  v_conversion_stats jsonb;
  v_recent_users jsonb;
  v_pro_monthly_count bigint;
  v_pro_yearly_count bigint;
  v_cutoff timestamptz;
begin
  v_caller_id := auth.uid();
  if v_caller_id is null or not public.is_admin_or_owner(v_caller_id) then
    raise exception 'Akses ditolak: Hanya Owner/Admin yang diizinkan mengakses metrik SaaS'
      using errcode = '42501';
  end if;

  -- Hitung cutoff waktu berdasarkan p_timeframe
  if p_timeframe = '7d' then
    v_cutoff := now() - interval '7 days';
  elsif p_timeframe = 'all' then
    v_cutoff := '1970-01-01 00:00:00+00'::timestamptz;
  else -- '30d' default
    v_cutoff := now() - interval '30 days';
  end if;

  -- KPI 1: Total Pengguna Terdaftar
  select count(*) into v_total_users from public.profiles;

  -- KPI 2: Pengguna Pro Aktif (status active & masa aktif belum lewat)
  select count(distinct user_id) into v_active_pro_users
  from public.subscriptions
  where status = 'active' and (expires_at is null or expires_at > now());

  -- KPI 3: Pengguna Free Tier
  v_free_users := greatest(0, v_total_users - v_active_pro_users);

  -- KPI 4: Total Pendapatan / Omset IDR (dari transaksi berstatus 'paid')
  select coalesce(sum(amount_idr), 0) into v_total_revenue_idr
  from public.payment_transactions
  where status = 'paid';

  -- KPI 5: Total Catatan Transaksi BBM
  select count(*) into v_total_transactions from public.fuel_transactions;

  -- KPI 6: Total Kendaraan Armada Terdaftar
  select count(*) into v_total_vehicles from public.vehicles where is_archived = false;

  -- Breakdown Paket Pro
  select count(*) into v_pro_monthly_count
  from public.subscriptions s
  join public.subscription_plans p on s.plan_id = p.id
  where s.status = 'active' and (s.expires_at is null or s.expires_at > now())
    and p.billing_period = 'monthly';

  select count(*) into v_pro_yearly_count
  from public.subscriptions s
  join public.subscription_plans p on s.plan_id = p.id
  where s.status = 'active' and (s.expires_at is null or s.expires_at > now())
    and p.billing_period = 'yearly';

  v_conversion_stats := jsonb_build_object(
    'free_users', v_free_users,
    'pro_monthly_users', v_pro_monthly_count,
    'pro_yearly_users', v_pro_yearly_count,
    'total_pro_users', v_active_pro_users,
    'conversion_rate_percent', case when v_total_users > 0 then round((v_active_pro_users::numeric / v_total_users::numeric) * 100, 1) else 0 end
  );

  -- Tren Pertumbuhan Registrasi Harian
  select coalesce(
    jsonb_agg(
      jsonb_build_object(
        'date_label', to_char(d.day, 'YYYY-MM-DD'),
        'count', coalesce(u.cnt, 0)
      ) order by d.day asc
    ), '[]'::jsonb
  ) into v_growth_trend
  from (
    select generate_series(
      date_trunc('day', greatest(v_cutoff, now() - interval '30 days')),
      date_trunc('day', now()),
      interval '1 day'
    )::date as day
  ) d
  left join (
    select date_trunc('day', created_at)::date as reg_day, count(*) as cnt
    from public.profiles
    group by date_trunc('day', created_at)::date
  ) u on d.day = u.reg_day;

  -- Daftar 15 Pengguna Terbaru beserta Status Langganan & Armada
  select coalesce(
    jsonb_agg(
      jsonb_build_object(
        'id', p.id,
        'display_name', coalesce(p.display_name, 'Pengguna Mari'),
        'created_at', p.created_at,
        'role', p.role,
        'is_pro', coalesce(sub.is_active_pro, false),
        'plan_name', coalesce(sub.plan_name, 'Mari Free'),
        'expires_at', sub.expires_at,
        'vehicles_count', coalesce(v_cnt.cnt, 0),
        'transactions_count', coalesce(t_cnt.cnt, 0)
      ) order by p.created_at desc
    ), '[]'::jsonb
  ) into v_recent_users
  from (
    select id, display_name, created_at, role
    from public.profiles
    order by created_at desc
    limit 15
  ) p
  left join lateral (
    select true as is_active_pro, pl.name as plan_name, s.expires_at
    from public.subscriptions s
    join public.subscription_plans pl on s.plan_id = pl.id
    where s.user_id = p.id and s.status = 'active' and (s.expires_at is null or s.expires_at > now())
    order by s.created_at desc
    limit 1
  ) sub on true
  left join lateral (
    select count(*) as cnt from public.vehicles where user_id = p.id and is_archived = false
  ) v_cnt on true
  left join lateral (
    select count(*) as cnt from public.fuel_transactions where user_id = p.id
  ) t_cnt on true;

  return jsonb_build_object(
    'success', true,
    'timeframe', p_timeframe,
    'kpi', jsonb_build_object(
      'total_users', v_total_users,
      'active_pro_users', v_active_pro_users,
      'free_users', v_free_users,
      'total_revenue_idr', v_total_revenue_idr,
      'total_transactions_recorded', v_total_transactions,
      'total_vehicles_registered', v_total_vehicles
    ),
    'growth_trend', v_growth_trend,
    'conversion_stats', v_conversion_stats,
    'recent_users', v_recent_users,
    'generated_at', now()
  );
end;
$$;

-- -----------------------------------------------------------------------------
-- 4. RPC Otoritatif: admin_grant_user_pro
-- Memperpanjang / mengaktifkan status Pro secara manual oleh Owner
-- -----------------------------------------------------------------------------
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
begin
  v_caller_id := auth.uid();
  if v_caller_id is null or not public.is_admin_or_owner(v_caller_id) then
    raise exception 'Akses ditolak: Hanya Owner/Admin yang diizinkan memberikan status Pro'
      using errcode = '42501';
  end if;

  if p_duration_days <= 0 then
    raise exception 'Durasi hari harus lebih dari 0' using errcode = '22003';
  end if;

  -- Pilih plan_id yang sesuai durasi
  if p_duration_days >= 365 then
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

  if v_existing_sub.id is not null and v_existing_sub.expires_at is not null and v_existing_sub.expires_at > now() then
    v_new_expires := v_existing_sub.expires_at + (p_duration_days || ' days')::interval;
    v_is_extended := true;

    update public.subscriptions
    set expires_at = v_new_expires,
        provider = 'manual_owner_grant',
        provider_subscription_ref = coalesce(p_notes, 'Manual Owner Grant by ' || v_caller_id::text),
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

  return jsonb_build_object(
    'success', true,
    'is_extended', v_is_extended,
    'target_user_id', p_target_user_id,
    'new_expires_at', v_new_expires,
    'duration_days_added', p_duration_days,
    'message', case when v_is_extended then 'Masa aktif Pro berhasil diperpanjang.' else 'Status Pro aktif baru berhasil diberikan.' end
  );
end;
$$;

-- Berikan izin eksekusi kepada authenticated (fungsi sendiri yang memvalidasi role = owner/admin)
grant execute on function public.get_saas_owner_metrics(text) to authenticated;
grant execute on function public.admin_grant_user_pro(uuid, integer, text) to authenticated;

-- Tetapkan akun pertama di database sebagai Owner secara otomatis jika belum ada owner sama sekali
update public.profiles
set role = 'owner'
where id in (
  select id from public.profiles order by created_at asc limit 1
) and not exists (
  select 1 from public.profiles where role = 'owner'
);
