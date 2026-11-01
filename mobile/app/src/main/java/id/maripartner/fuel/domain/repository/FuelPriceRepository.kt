package id.maripartner.fuel.domain.repository

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.FuelBrandPrice

/**
 * Kontrak repositori untuk data harga bahan bakar nasional (Pertamina, Shell, BP, Vivo).
 */
interface FuelPriceRepository {

    /**
     * Mengambil daftar harga bahan bakar resmi terkini dari database remote
     * dengan fallback otomatis ke data preset lokal jika offline.
     */
    suspend fun getFuelPrices(): AppResult<List<FuelBrandPrice>>

    /**
     * Memperbarui tarif bahan bakar tertentu (khusus akun Owner/Admin SaaS).
     */
    suspend fun updateFuelPrice(id: String, newPriceIdr: Long): AppResult<Unit>
}
