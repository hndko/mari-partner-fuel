-- Migrasi: Pembuatan Tabel Harga Bahan Bakar Dinamis & RLS Multi-Brand
-- Versi: v2.0.7 (Build 18)

create table if not exists public.fuel_prices (
    id uuid primary key default gen_random_uuid(),
    brand text not null,
    fuel_name text not null,
    grade_category text not null,
    price_per_liter_idr bigint not null check (price_per_liter_idr >= 0),
    is_subsidized boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uq_fuel_prices_brand_fuel unique (brand, fuel_name)
);

create index if not exists idx_fuel_prices_brand on public.fuel_prices (brand);
create index if not exists idx_fuel_prices_grade on public.fuel_prices (grade_category);

alter table public.fuel_prices enable row level security;

-- Policy 1: Pembacaan publik / seluruh user yang terautentikasi
drop policy if exists allow_read_all_fuel_prices on public.fuel_prices;
create policy allow_read_all_fuel_prices on public.fuel_prices
    for select
    using (true);

-- Policy 2: Mutasi (Insert/Update/Delete) khusus Owner / Admin SaaS
drop policy if exists allow_admin_manage_fuel_prices on public.fuel_prices;
create policy allow_admin_manage_fuel_prices on public.fuel_prices
    for all
    using (
        auth.jwt() ->> 'email' = 'kyuoko13@gmail.com'
        or public.is_admin_or_owner(auth.uid())
    )
    with check (
        auth.jwt() ->> 'email' = 'kyuoko13@gmail.com'
        or public.is_admin_or_owner(auth.uid())
    );

-- Seed Data Awal: 16 Varian BBM Terpopuler Indonesia (Pertamina, Shell, BP, Vivo)
insert into public.fuel_prices (brand, fuel_name, grade_category, price_per_liter_idr, is_subsidized)
values
    -- Pertamina
    ('Pertamina', 'Pertalite', 'RON 90', 10000, true),
    ('Pertamina', 'Pertamax', 'RON 92', 12100, false),
    ('Pertamina', 'Pertamax Green 95', 'RON 95', 13150, false),
    ('Pertamina', 'Pertamax Turbo', 'RON 98', 13250, false),
    ('Pertamina', 'Bio Solar', 'Diesel', 6800, true),
    ('Pertamina', 'Dexlite (CN 51)', 'Diesel', 13050, false),
    ('Pertamina', 'Pertamina Dex (CN 53)', 'Diesel', 13400, false),

    -- Shell
    ('Shell', 'Shell Super', 'RON 92', 12290, false),
    ('Shell', 'Shell V-Power', 'RON 95', 13300, false),
    ('Shell', 'Shell V-Power Nitro+', 'RON 98', 13540, false),
    ('Shell', 'Shell V-Power Diesel', 'Diesel', 13610, false),

    -- BP AKR
    ('BP', 'BP 92', 'RON 92', 12290, false),
    ('BP', 'BP Ultimate', 'RON 95', 13300, false),
    ('BP', 'BP Ultimate Diesel', 'Diesel', 13610, false),

    -- Vivo
    ('Vivo', 'Revvo 90', 'RON 90', 11990, false),
    ('Vivo', 'Revvo 92', 'RON 92', 12200, false),
    ('Vivo', 'Revvo 95', 'RON 95', 13200, false)
on conflict (brand, fuel_name) do update
set price_per_liter_idr = excluded.price_per_liter_idr,
    grade_category = excluded.grade_category,
    is_subsidized = excluded.is_subsidized,
    updated_at = now();

-- RPC Fungsi Khusus Owner untuk Memperbarui Tarif BBM secara Aman
create or replace function public.admin_update_fuel_price(
    p_fuel_id uuid,
    p_new_price bigint
)
returns public.fuel_prices
language plpgsql
security definer
set search_path = public
as $$
declare
    v_updated public.fuel_prices;
begin
    if auth.jwt() ->> 'email' <> 'kyuoko13@gmail.com' and not public.is_admin_or_owner(auth.uid()) then
        raise exception 'Akses ditolak: Hanya Owner SaaS yang berhak memperbarui tarif BBM.';
    end if;

    update public.fuel_prices
    set price_per_liter_idr = p_new_price,
        updated_at = now()
    where id = p_fuel_id
    returning * into v_updated;

    return v_updated;
end;
$$;
