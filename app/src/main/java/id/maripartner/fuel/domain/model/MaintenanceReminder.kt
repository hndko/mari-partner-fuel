package id.maripartner.fuel.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Jenis pemicu jatuh tempo pengingat servis perawatan.
 */
enum class ReminderType(val code: String, val label: String) {
    ODOMETER("odometer", "Berdasarkan Odometer"),
    DATE("date", "Berdasarkan Tanggal"),
    BOTH("both", "Odometer & Tanggal");

    companion object {
        fun fromCode(code: String): ReminderType {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: BOTH
        }
    }
}

/**
 * Status jatuh tempo pengingat berdasarkan evaluasi tanggal & odometer saat ini.
 */
enum class ReminderDueStatus(val label: String) {
    UPCOMING("Mendatang"),
    DUE_SOON("Mendekati Jatuh Tempo"),
    OVERDUE("Sudah Jatuh Tempo"),
    COMPLETED("Selesai")
}

/**
 * Model domain bersih untuk pengingat servis dan perawatan berkala kendaraan.
 */
data class MaintenanceReminder(
    val id: String = "",
    val userId: String = "",
    val vehicleId: String,
    val vehicleName: String? = null,
    val vehiclePlate: String? = null,
    val title: String,
    val reminderType: ReminderType = ReminderType.BOTH,
    val targetOdometerKm: Double? = null,
    val targetDate: LocalDate? = null,
    val isCompleted: Boolean = false,
    val completedAt: Instant? = null,
    val notes: String? = null,
    val lastNotifiedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    /**
     * Memeriksa apakah pengingat memiliki pemicu odometer.
     */
    val hasOdometerTrigger: Boolean
        get() = (reminderType == ReminderType.ODOMETER || reminderType == ReminderType.BOTH) &&
                targetOdometerKm != null && targetOdometerKm > 0.0

    /**
     * Memeriksa apakah pengingat memiliki pemicu tanggal.
     */
    val hasDateTrigger: Boolean
        get() = (reminderType == ReminderType.DATE || reminderType == ReminderType.BOTH) &&
                targetDate != null
}
