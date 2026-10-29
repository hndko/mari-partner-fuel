-- =============================================================================
-- Migrasi Tahap 5: Penyempurnaan Tabel maintenance_reminders & Proteksi BOLA
-- File: 20261029000005_maintenance_reminders_enhancement.sql
-- =============================================================================

-- 1. Tambahkan constraint Composite Foreign Key untuk isolasi kepemilikan anti-BOLA/IDOR
do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'fk_maintenance_reminders_vehicle_user'
  ) then
    alter table public.maintenance_reminders
      add constraint fk_maintenance_reminders_vehicle_user
      foreign key (vehicle_id, user_id)
      references public.vehicles(id, user_id)
      on delete cascade;
  end if;
end $$;

-- 2. Tambahkan kolom pendukung penyelesaian dan histori notifikasi pengingat
alter table public.maintenance_reminders
  add column if not exists completed_at timestamptz,
  add column if not exists last_notified_at timestamptz;

-- 3. Tambahkan check constraint untuk menjamin minimal salah satu pemicu jatuh tempo diisi
do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'chk_maintenance_reminders_trigger'
  ) then
    alter table public.maintenance_reminders
      add constraint chk_maintenance_reminders_trigger
      check (target_odometer_km is not null or target_date is not null);
  end if;
end $$;

-- 4. Indeks komposit performa query pengingat aktif dan jatuh tempo
create index if not exists idx_maintenance_reminders_due
  on public.maintenance_reminders(user_id, is_completed, target_date);

create index if not exists idx_maintenance_reminders_vehicle
  on public.maintenance_reminders(vehicle_id, is_completed);
