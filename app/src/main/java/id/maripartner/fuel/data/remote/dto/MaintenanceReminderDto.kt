package id.maripartner.fuel.data.remote.dto

import id.maripartner.fuel.domain.model.MaintenanceReminder
import id.maripartner.fuel.domain.model.ReminderType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate

/**
 * Data Transfer Object untuk tabel 'maintenance_reminders' di Supabase PostgreSQL.
 */
@Serializable
data class MaintenanceReminderDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("vehicle_id") val vehicleId: String,
    val title: String,
    @SerialName("reminder_type") val reminderType: String = "both",
    @SerialName("target_odometer_km") val targetOdometerKm: Double? = null,
    @SerialName("target_date") val targetDate: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("completed_at") val completedAt: String? = null,
    val notes: String? = null,
    @SerialName("last_notified_at") val lastNotifiedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // Embedded vehicle object jika di-join melalui Postgrest
    val vehicles: VehicleDto? = null
) {
    fun toDomain(): MaintenanceReminder {
        val parsedTargetDate = targetDate?.takeIf { it.isNotBlank() }?.let {
            try {
                LocalDate.parse(it.substring(0, 10))
            } catch (e: Exception) {
                null
            }
        }

        return MaintenanceReminder(
            id = id.orEmpty(),
            userId = userId.orEmpty(),
            vehicleId = vehicleId,
            vehicleName = vehicles?.name,
            vehiclePlate = vehicles?.licensePlate,
            title = title,
            reminderType = ReminderType.fromCode(reminderType),
            targetOdometerKm = targetOdometerKm,
            targetDate = parsedTargetDate,
            isCompleted = isCompleted,
            completedAt = parseInstant(completedAt),
            notes = notes,
            lastNotifiedAt = parseInstant(lastNotifiedAt),
            createdAt = parseInstant(createdAt) ?: Instant.now(),
            updatedAt = parseInstant(updatedAt) ?: Instant.now()
        )
    }

    companion object {
        fun fromDomain(reminder: MaintenanceReminder, userId: String? = null): MaintenanceReminderDto {
            return MaintenanceReminderDto(
                id = reminder.id.ifBlank { null },
                userId = userId ?: reminder.userId.ifBlank { null },
                vehicleId = reminder.vehicleId,
                title = reminder.title.trim(),
                reminderType = reminder.reminderType.code,
                targetOdometerKm = reminder.targetOdometerKm?.takeIf { it > 0.0 },
                targetDate = reminder.targetDate?.toString(),
                isCompleted = reminder.isCompleted,
                completedAt = reminder.completedAt?.toString(),
                notes = reminder.notes?.trim()?.takeIf { it.isNotBlank() },
                lastNotifiedAt = reminder.lastNotifiedAt?.toString()
            )
        }

        private fun parseInstant(isoString: String?): Instant? {
            if (isoString.isNullOrBlank()) return null
            return try {
                Instant.parse(isoString)
            } catch (e: Exception) {
                null
            }
        }
    }
}
