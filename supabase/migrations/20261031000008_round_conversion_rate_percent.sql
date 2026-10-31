-- Migration: 20261031000008_round_conversion_rate_percent.sql
-- Description: Update get_saas_owner_metrics RPC to round conversion_rate_percent to clean integer (0 decimal places)

create or replace function public.get_saas_owner_metrics(p_timeframe text default '30d')
returns jsonb
language plpgsql
security definer
set search_path = public, pg_temp
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
    'conversion_rate_percent', case when v_total_users > 0 then round((v_active_pro_users::numeric / v_total_users::numeric) * 100, 0) else 0 end
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
