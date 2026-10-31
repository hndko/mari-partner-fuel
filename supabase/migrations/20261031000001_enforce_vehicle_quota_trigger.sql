-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION: ENFORCE VEHICLE QUOTA TRIGGER
-- Mencegah bypass limit 2 kendaraan aktif untuk paket Free di level PostgreSQL
-- =============================================================================

create or replace function public.check_vehicle_limit_before_insert()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_is_pro boolean := false;
  v_active_count integer := 0;
begin
  -- Lewati jika kendaraan langsung dibuat dalam status arsip
  if new.is_archived = true then
    return new;
  end if;

  -- Periksa status Pro aktif pengguna
  select coalesce(
    (
      select true
      from public.subscriptions s
      where s.user_id = new.user_id
        and (
          (s.status = 'active' and (s.expires_at is null or s.expires_at > now()))
          or
          (s.status = 'grace_period' and s.expires_at > now() - interval '3 days')
        )
      limit 1
    ),
    false
  ) into v_is_pro;

  -- Jika bukan pengguna Pro, batasi maksimal 2 kendaraan aktif
  if not v_is_pro then
    select count(*) into v_active_count
    from public.vehicles
    where user_id = new.user_id and is_archived = false;

    if v_active_count >= 2 then
      raise exception 'Batas paket Free adalah 2 kendaraan aktif. Silakan berlangganan Mari Pro untuk menambah kendaraan tanpa batas.'
        using errcode = '42501';
    end if;
  end if;

  return new;
end;
$$;

drop trigger if exists trg_check_vehicle_limit on public.vehicles;
create trigger trg_check_vehicle_limit
  before insert on public.vehicles
  for each row
  execute function public.check_vehicle_limit_before_insert();
