-- =============================================================================
-- MARI PARTNER FUEL — MIGRATION 20261029000004
-- Tahap 4: Penyempurnaan Policy RLS receipt_scan_jobs untuk Pelacakan Status OCR & AI
-- =============================================================================

-- Memungkinkan pengguna memperbarui status scan job miliknya sendiri (misal: pending -> completed)
create policy "receipt_scan_jobs_update_own"
  on public.receipt_scan_jobs for update
  to authenticated
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);
