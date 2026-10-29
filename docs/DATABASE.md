# DATABASE.md --- Mari Partner Fuel

**Desain Basis Data, Relasi, Indeks & Row Level Security (RLS)**\
**Aplikasi:** Mari Partner Fuel\
**Platform Backend:** Supabase (PostgreSQL 15+)

---

## 1. Filosofi & Standar Keamanan Data

1. **Row Level Security (RLS) Wajib Aktif:** Setiap tabel yang diakses oleh klien Android (`public.*`) wajib memiliki RLS aktif dengan kebijakan `auth.uid() = user_id`.
2. **Kunci Primer UUID:** Semua entitas menggunakan UUID v4 (`gen_random_uuid()`) untuk mencegah serangan enumerasi ID.
3. **Validasi Relasi Kepemilikan (Composite Foreign Keys):** Untuk mencegah referensi lintas akun, foreign key menyertakan kolom `user_id` (contoh: `foreign key (vehicle_id, user_id) references vehicles(id, user_id)`).
4. **Pemisahan Hak Akses:** Tabel sensitif seperti `subscriptions`, `payment_transactions`, dan `subscription_plans` hanya boleh dimutasi oleh backend/Edge Functions yang menggunakan `service_role` key. Klien Android hanya memiliki hak baca terbatas (*read-only*).

---

## 2. Struktur Tabel Utama

### 2.1 `profiles`
Menyimpan profil publik dan preferensi aplikasi pengguna.
- `id` (UUID, PK, references `auth.users(id)` on delete cascade)
- `display_name` (text, nullable)
- `timezone` (text, not null, default 'Asia/Jakarta')
- `theme_preference` (text, check in ('light','dark','system'), default 'system')
- `created_at` (timestamptz, default now())
- `updated_at` (timestamptz, default now())

### 2.2 `vehicles`
Menyimpan data kendaraan yang dimiliki pengguna.
- `id` (UUID, PK, default gen_random_uuid())
- `user_id` (UUID, not null, references `auth.users(id)` on delete cascade)
- `name` (text, not null)
- `vehicle_type` (text, not null, check in ('motorcycle','car','other'))
- `make` (text, nullable)
- `model` (text, nullable)
- `license_plate` (text, nullable) --- *data sensitif, tidak dikirim ke AI provider*
- `default_fuel_type` (text, nullable)
- `tank_capacity_liters` (numeric(8,2), nullable)
- `last_odometer_km` (numeric(12,1), nullable)
- `is_archived` (boolean, default false)
- `created_at` (timestamptz, default now())
- `updated_at` (timestamptz, default now())
- *Constraint:* `UNIQUE (id, user_id)`

### 2.3 `receipt_files`
Metadata file gambar struk yang tersimpan di private bucket Storage Supabase.
- `id` (UUID, PK, default gen_random_uuid())
- `user_id` (UUID, not null, references `auth.users(id)` on delete cascade)
- `storage_path` (text, not null, unique) --- *format: `receipts/{user_id}/{filename}`*
- `mime_type` (text, not null)
- `size_bytes` (bigint, not null)
- `sha256` (text, nullable) --- *fingerprint untuk deteksi struk duplikat*
- `created_at` (timestamptz, default now())
- *Constraint:* `UNIQUE (id, user_id)`

### 2.4 `fuel_transactions`
Pencatatan transaksi bahan bakar.
- `id` (UUID, PK, default gen_random_uuid())
- `user_id` (UUID, not null, references `auth.users(id)` on delete cascade)
- `vehicle_id` (UUID, not null)
- `receipt_file_id` (UUID, nullable)
- `transaction_at` (timestamptz, not null)
- `station_name` (text, nullable)
- `fuel_type` (text, nullable)
- `volume_liters` (numeric(10,3), not null, check > 0)
- `total_amount_idr` (bigint, not null, check >= 0)
- `price_per_liter_idr` (numeric(12,2), nullable)
- `odometer_km` (numeric(12,1), nullable)
- `scan_status` (text, not null, check in ('manual','ocr_review','ai_review','confirmed'))
- `notes` (text, nullable)
- `created_at` (timestamptz, default now())
- `updated_at` (timestamptz, default now())
- *Composite FKs:*
  - `FOREIGN KEY (vehicle_id, user_id) REFERENCES vehicles(id, user_id)`
  - `FOREIGN KEY (receipt_file_id, user_id) REFERENCES receipt_files(id, user_id)`

### 2.5 `subscription_plans`
Daftar paket Pro yang dikelola backend.
- `id` (UUID, PK, default gen_random_uuid())
- `code` (text, not null, unique) --- 'pro_monthly', 'pro_yearly'
- `name` (text, not null)
- `billing_period` (text, not null, check in ('monthly','yearly'))
- `price_idr` (bigint, not null, check >= 0)
- `ai_scan_quota` (integer, not null, check >= 0)
- `max_vehicles` (integer, nullable)
- `is_active` (boolean, default true)
- `created_at` (timestamptz, default now())

### 2.6 `subscriptions`
Status langganan aktif pengguna.
- `id` (UUID, PK, default gen_random_uuid())
- `user_id` (UUID, not null, references `auth.users(id)` on delete cascade)
- `plan_id` (UUID, not null, references `subscription_plans(id)`)
- `status` (text, not null, check in ('pending','active','expired','cancelled','past_due'))
- `starts_at` (timestamptz, nullable)
- `expires_at` (timestamptz, nullable)
- `provider` (text, nullable)
- `provider_subscription_ref` (text, nullable)
- `created_at` (timestamptz, default now())
- `updated_at` (timestamptz, default now())

### 2.7 `payment_transactions`
Riwayat order transaksi pembayaran.
- `id` (UUID, PK, default gen_random_uuid())
- `user_id` (UUID, not null, references `auth.users(id)`)
- `subscription_id` (UUID, nullable, references `subscriptions(id)`)
- `provider` (text, not null, check in ('qrispy','midtrans','xendit'))
- `payment_reference` (text, not null, unique)
- `provider_payment_id` (text, nullable)
- `amount_idr` (bigint, not null)
- `currency` (text, not null, default 'IDR')
- `status` (text, not null, check in ('created','pending','paid','expired','cancelled','failed','refunded'))
- `checkout_url` (text, nullable)
- `qris_image_url` (text, nullable)
- `expires_at` (timestamptz, nullable)
- `paid_at` (timestamptz, nullable)
- `created_at` (timestamptz, default now())
- `updated_at` (timestamptz, default now())
- *Constraint:* `UNIQUE (provider, provider_payment_id)`

---

## 3. Indeks Kinerja

```sql
create index idx_vehicles_user_active on public.vehicles(user_id, is_archived);
create index idx_fuel_transactions_user_date on public.fuel_transactions(user_id, transaction_at desc);
create index idx_fuel_transactions_vehicle_date on public.fuel_transactions(vehicle_id, transaction_at desc);
create index idx_subscriptions_user_status on public.subscriptions(user_id, status);
create index idx_payment_transactions_reference on public.payment_transactions(payment_reference);
```

---

## 4. Kebijakan Row Level Security (RLS)

- **`vehicles` & `fuel_transactions`:**
  - `SELECT`: `auth.uid() = user_id`
  - `INSERT`: `auth.uid() = user_id`
  - `UPDATE`: `auth.uid() = user_id`
  - `DELETE`: `auth.uid() = user_id`
- **`receipt_files`:**
  - `SELECT`: `auth.uid() = user_id`
  - `INSERT`: `auth.uid() = user_id`
  - `DELETE`: `auth.uid() = user_id`
- **`subscription_plans`:**
  - `SELECT`: `true` (dapat dibaca publik)
  - `INSERT/UPDATE/DELETE`: `false` (hanya via migrasi / admin)
- **`subscriptions` & `payment_transactions`:**
  - `SELECT`: `auth.uid() = user_id`
  - `INSERT/UPDATE/DELETE`: `false` (hanya via Edge Functions berkredensial service-role)
