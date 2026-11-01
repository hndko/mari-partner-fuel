package id.maripartner.fuel.domain.repository

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk operasi manajemen data kendaraan pengguna.
 */
interface VehicleRepository {

    /**
     * Mengambil daftar kendaraan milik pengguna yang sedang login.
     */
    fun getVehicles(includeArchived: Boolean = true): Flow<AppResult<List<Vehicle>>>

    /**
     * Mengambil data kendaraan berdasarkan ID.
     */
    suspend fun getVehicleById(id: String): AppResult<Vehicle>

    /**
     * Menyimpan data kendaraan baru.
     */
    suspend fun createVehicle(vehicle: Vehicle): AppResult<Vehicle>

    /**
     * Memperbarui data kendaraan yang ada.
     */
    suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle>

    /**
     * Mengubah status arsip kendaraan.
     */
    suspend fun archiveVehicle(id: String, archive: Boolean): AppResult<Unit>

    /**
     * Menghapus kendaraan secara permanen jika tidak memiliki riwayat transaksi (ON DELETE RESTRICT).
     */
    suspend fun deleteVehicle(id: String): AppResult<Unit>
}
