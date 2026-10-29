# DATABASE.md --- Mari Partner Fuel

**Desain Basis Data, Relasi, Indeks & Row Level Security (RLS)**\
**Aplikasi:** Mari Partner Fuel\
**Platform Backend:** Supabase (PostgreSQL 15+)

---

## 1. Filosofi & Standar Keamanan Data

1. **Row Level Security (RLS) Wajib Aktif:** Setiap tabel di Supabase (`public.*`) yang dapat diakses oleh klien Android wajib memiliki RLS aktif dengan kebijakan kepemilikan berbasis `auth.uid() = user_id`.
2. **Kunci Primer UUID:** Semua entitas menggunakan UUID v4 (`gen_random_uuid()`) untuk mencegah serangan enumerasi ID.
3. **Validasi Relasi Kepemilikan (Composite Foreign Keys):** Untuk mencegah serangan BOLA/IDOR (misal menghubungkan transaksi BBM ke kendaraan milik pengguna lain), foreign key komposit menyertakan kolom `user_id`:
   ```sql
   foreign key (vehicle_id, user_id) references public.vehicles(id, user_id)
   foreign key (receipt_file_id, user_id) references public.receipt_files(id, user_id)
   ```
4. **Pemisahan Hak Akses Privileged:** Tabel `subscriptions`, `payment_transactions`, `payment_webhook_events`, `subscription_plans`, dan `ai_usage_logs` dilindungi secara ketat. Klien Android hanya memiliki hak baca (*read-only*), sedangkan mutasi status (`active`, `paid`, kuota) hanya diizinkan melalui Supabase Edge Functions dengan `service_role`.

---

## 2. Inventarisasi 13 Tabel Relasional

Seluruh skema tersimpan rapi dalam migrasi SQL:
- [`supabase/migrations/20261029000001_initial_schema.sql`](../supabase/migrations/20261029000001_initial_schema.sql)
- [`supabase/migrations/20261029000002_rls_and_storage.sql`](../supabase/migrations/20261029000002_rls_and_storage.sql)

| No | Nama Tabel | Deskripsi & Tujuan | Kebijakan RLS Klien Android |
|:--:|:---|:---|:---|
| **1** | `profiles` | Profil pengguna, preferensi zona waktu & tema | SELECT, UPDATE, INSERT untuk `auth.uid() = id` |
| **2** | `vehicles` | Data kendaraan (motor, mobil, armada) | CRUD penuh hanya untuk `auth.uid() = user_id` |
| **3** | `receipt_files` | Metadata berkas foto struk di private bucket | SELECT, INSERT, DELETE hanya untuk `auth.uid() = user_id` |
| **4** | `fuel_transactions` | Catatan pengisian BBM & odometer | CRUD penuh hanya untuk `auth.uid() = user_id` (Composite FK) |
| **5** | `receipt_scan_jobs` | Audit hasil OCR lokal & fallback AI | SELECT, INSERT hanya untuk `auth.uid() = user_id` |
| **6** | `maintenance_reminders`| Pengingat servis & ganti oli mesin | CRUD penuh hanya untuk `auth.uid() = user_id` |
| **7** | `notification_preferences`| Preferensi push notification pengguna | SELECT, UPDATE hanya untuk `auth.uid() = id` |
| **8** | `subscription_plans` | Master harga paket Pro & kuota AI | SELECT publik (`is_active = true`); INSERT/UPDATE/DELETE diblokir |
| **9** | `subscriptions` | Hak akses langganan aktif pengguna | SELECT hanya untuk `auth.uid() = user_id`; Mutasi diblokir dari klien |
| **10**| `payment_transactions` | Riwayat order pembayaran gateway | SELECT hanya untuk `auth.uid() = user_id`; Mutasi diblokir dari klien |
| **11**| `payment_webhook_events`| Log idempotensi event gateway | Akses klien diblokir total (khusus `service_role` backend) |
| **12**| `ai_usage_logs` | Catatan pemakaian kuota AI per bulan | SELECT hanya untuk `auth.uid() = user_id`; Insert via backend |
| **13**| `user_feedback` | Masukan pengguna & rating aplikasi | INSERT dan SELECT untuk `auth.uid() = user_id` |

---

## 3. Skema Rinci & Tipe Data Baku

### 3.1 Presisi Angka & Keuangan
- **Nominal Rupiah:** Wajib menggunakan `bigint` (contoh: `total_amount_idr bigint not null check (total_amount_idr >= 0)`). Dilarang floating-point.
- **Volume Liter:** Wajib menggunakan `numeric(10,3)` dengan presisi 3 angka desimal (contoh: `25.450 L`).
- **Odometer:** Menggunakan `numeric(12,1)` untuk presisi 1 angka desimal kilometer (contoh: `12540.5 km`).

### 3.2 Indeks Kinerja Query Utama
```sql
create index idx_vehicles_user_active on public.vehicles(user_id, is_archived);
create index idx_fuel_transactions_user_date on public.fuel_transactions(user_id, transaction_at desc);
create index idx_fuel_transactions_vehicle_date on public.fuel_transactions(vehicle_id, transaction_at desc);
create index idx_subscriptions_user_status on public.subscriptions(user_id, status);
create index idx_payment_transactions_reference on public.payment_transactions(payment_reference);
create index idx_payment_webhook_events_reference on public.payment_webhook_events(payment_reference);
create index idx_ai_usage_logs_user_month on public.ai_usage_logs(user_id, billing_month);
```

### 3.3 Otomasi Trigger
- **`handle_updated_at()`**: Fungsi trigger konsisten yang memperbarui kolom `updated_at = now()` sebelum operasi update pada seluruh tabel utama.
- **`handle_new_user()`**: Trigger `after insert on auth.users` yang secara otomatis membuat baris awal pada `profiles` dan `notification_preferences`.

---

## 4. Konfigurasi Private Storage Bucket (`receipts`)

- **Nama Bucket:** `receipts`
- **Visibilitas:** `public = false` (Privat)
- **Ukuran Maksimal:** 5.242.880 bytes (5 MB)
- **Tipe MIME Diizinkan:** `['image/jpeg', 'image/png', 'image/webp']`
- **Kebijakan Akses Objek (`storage.objects`):**
  Path berkas berformat: `receipts/{user_id}/{uuid}.{ext}`.
  Aturan RLS memastikan pengguna hanya dapat mengunggah, membaca, memperbarui, dan menghapus berkas yang berada di dalam folder ID penggunanya sendiri (`(storage.foldername(name))[1] = auth.uid()::text`).
