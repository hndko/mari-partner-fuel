-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION: 20261030000001_ephemeral_receipt_storage_policy
-- Kebijakan Penyimpanan Struk Ephemeral & Retensi Nol File Jangka Panjang.
-- Seluruh transaksi disimpan permanen berbasis nilai numerik & teks terstruktur di fuel_transactions.
-- Foto struk hanya berfungsi sebagai input sementara untuk pemindaian OCR/AI on-device.
-- =============================================================================

-- 1. Fungsi Pembersihan Otomatis File Struk Sementara / Kedaluwarsa
create or replace function public.cleanup_ephemeral_receipts(p_retention_hours int default 24)
returns int
language plpgsql
security definer
set search_path = public, auth
as $$
declare
  v_deleted_count int := 0;
begin
  -- Lepaskan referensi receipt_file_id pada transaksi BBM historis
  update public.fuel_transactions
  set receipt_file_id = null
  where receipt_file_id is not null
    and created_at < (now() - (p_retention_hours || ' hours')::interval);

  -- Hapus rekaman metadata berkas struk yang telah melewati batas retensi sementara
  with deleted_rows as (
    delete from public.receipt_files
    where created_at < (now() - (p_retention_hours || ' hours')::interval)
    returning id
  )
  select count(*) into v_deleted_count from deleted_rows;

  return v_deleted_count;
end;
$$;

-- Berikan hak eksekusi fungsi pembersihan kepada service role
grant execute on function public.cleanup_ephemeral_receipts(int) to service_role;

-- 2. Pastikan kolom receipt_file_id pada fuel_transactions selalu opsional (nullable)
alter table public.fuel_transactions alter column receipt_file_id drop not null;

-- 3. Dokumentasi Arsitektur Kolom
comment on column public.fuel_transactions.receipt_file_id is
  'ID file struk opsional berstatus sementara. Transaksi BBM historis disimpan murni berbasis teks & numerik tanpa menyimpan file foto fisik permanen di server.';
