-- =============================================================================
-- Migration: 20261101000001_add_delete_user_account_rpc.sql
-- Proyek: Mari Partner Fuel
-- Deskripsi: Fungsi RPC berkeamanan tinggi (SECURITY DEFINER) untuk hak
--            penghapusan akun & data pengguna (Google Play Account Deletion Policy & GDPR)
-- =============================================================================

create or replace function public.delete_user_account()
returns jsonb
language plpgsql
security definer
set search_path = public, auth
as $$
declare
  v_user_id uuid := auth.uid();
begin
  if v_user_id is null then
    raise exception 'Pengguna belum terautentikasi.';
  end if;

  -- 1. Hapus transaksi bahan bakar (mencegah restrict FK dari vehicles)
  delete from public.fuel_transactions where user_id = v_user_id;

  -- 2. Hapus pengingat servis perawatan armada
  delete from public.maintenance_reminders where user_id = v_user_id;

  -- 3. Hapus job scan struk
  delete from public.receipt_scan_jobs where user_id = v_user_id;

  -- 4. Hapus metadata berkas struk
  delete from public.receipt_files where user_id = v_user_id;

  -- 5. Hapus transaksi pembayaran & langganan pengguna
  delete from public.payment_transactions where user_id = v_user_id;
  delete from public.subscriptions where user_id = v_user_id;

  -- 6. Hapus preferensi notifikasi
  delete from public.notification_preferences where id = v_user_id;

  -- 7. Hapus kendaraan armada
  delete from public.vehicles where user_id = v_user_id;

  -- 8. Hapus profil publik pengguna
  delete from public.profiles where id = v_user_id;

  -- 9. Hapus akun otentikasi Supabase Auth
  delete from auth.users where id = v_user_id;

  return jsonb_build_object(
    'success', true,
    'message', 'Akun pengguna dan seluruh data terkait telah berhasil dihapus secara permanen.'
  );
exception
  when others then
    return jsonb_build_object(
      'success', false,
      'error', sqlerrm
    );
end;
$$;

-- Berikan izin eksekusi RPC hanya untuk pengguna terautentikasi (authenticated)
grant execute on function public.delete_user_account() to authenticated;
