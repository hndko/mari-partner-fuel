package id.maripartner.fuel.domain.repository

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.MaintenanceReminder

/**
 * Kontrak repositori untuk pengelolaan pengingat perawatan dan servis kendaraan.
 */
interface MaintenanceReminderRepository {

    /**
     * Mengambil daftar pengingat milik user yang sedang aktif login.
     * Dapat difilter berdasarkan kendaraan tertentu atau status selesai.
     */
    suspend fun getReminders(
        vehicleId: String? = null,
        isCompleted: Boolean? = null
    ): AppResult<List<MaintenanceReminder>>

    /**
     * Mengambil detail pengingat berdasarkan ID.
     */
    suspend fun getReminderById(id: String): AppResult<MaintenanceReminder>

    /**
     * Membuat pengingat baru.
     */
    suspend fun createReminder(reminder: MaintenanceReminder): AppResult<MaintenanceReminder>

    /**
     * Memperbarui pengingat yang ada.
     */
    suspend fun updateReminder(reminder: MaintenanceReminder): AppResult<MaintenanceReminder>

    /**
     * Menandai pengingat sebagai selesai atau aktif kembali.
     */
    suspend fun markAsCompleted(id: String, completed: Boolean): AppResult<MaintenanceReminder>

    /**
     * Menghapus pengingat.
     */
    suspend fun deleteReminder(id: String): AppResult<Unit>
}
