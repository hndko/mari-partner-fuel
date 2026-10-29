-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION 20261029000003
-- Tahap 3: Penyempurnaan Skema Kendaraan, Transaksi BBM Manual, dan Indeks Query
-- =============================================================================

-- 1. Penambahan kolom tahun pembuatan dan catatan pada public.vehicles
alter table if exists public.vehicles
  add column if not exists production_year integer
    check (production_year is null or (production_year >= 1900 and production_year <= 2100)),
  add column if not exists notes text;

-- 2. Pelonggaran batasan volume_liters pada fuel_transactions (Opsional pada manual input)
alter table if exists public.fuel_transactions
  alter column volume_liters drop not null;

alter table if exists public.fuel_transactions
  drop constraint if exists fuel_transactions_volume_liters_check;

alter table if exists public.fuel_transactions
  add constraint fuel_transactions_volume_liters_check
  check (volume_liters is null or volume_liters > 0);

-- 3. Pengetatan batasan nominal pembayaran: total_amount_idr wajib > 0
alter table if exists public.fuel_transactions
  drop constraint if exists fuel_transactions_total_amount_idr_check;

alter table if exists public.fuel_transactions
  add constraint fuel_transactions_total_amount_idr_check
  check (total_amount_idr > 0);

-- 4. Indeks komposit performa tinggi untuk pencarian riwayat transaksi
create index if not exists idx_fuel_transactions_station
  on public.fuel_transactions(user_id, station_name);

create index if not exists idx_fuel_transactions_fuel_type
  on public.fuel_transactions(user_id, fuel_type);

-- 5. Trigger otomatis untuk memperbarui odometer terakhir kendaraan
create or replace function public.handle_vehicle_odometer_update()
returns trigger as $$
begin
  if new.odometer_km is not null then
    update public.vehicles
    set last_odometer_km = greatest(coalesce(last_odometer_km, 0), new.odometer_km),
        updated_at = now()
    where id = new.vehicle_id and user_id = new.user_id;
  end if;
  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists trg_fuel_transactions_odometer on public.fuel_transactions;

create trigger trg_fuel_transactions_odometer
  after insert or update of odometer_km on public.fuel_transactions
  for each row execute function public.handle_vehicle_odometer_update();
