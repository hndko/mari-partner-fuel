package id.maripartner.fuel.domain.usecase.reminder

import id.maripartner.fuel.domain.model.MaintenanceReminder
import id.maripartner.fuel.domain.model.ReminderDueStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Use case murni untuk mengevaluasi status jatuh tempo pengingat servis.
 */
class CheckReminderDueStatusUseCase {

    operator fun invoke(
        reminder: MaintenanceReminder,
        currentVehicleOdometer: Double? = null,
        today: LocalDate = LocalDate.now()
    ): ReminderDueStatus {
        if (reminder.isCompleted) {
            return ReminderDueStatus.COMPLETED
        }

        var isOverdue = false
        var isDueSoon = false

        // 1. Evaluasi Tanggal
        if (reminder.hasDateTrigger && reminder.targetDate != null) {
            val targetDate = reminder.targetDate
            if (targetDate.isBefore(today)) {
                isOverdue = true
            } else {
                val daysUntil = ChronoUnit.DAYS.between(today, targetDate)
                if (daysUntil <= 7) {
                    isDueSoon = true
                }
            }
        }

        // 2. Evaluasi Odometer
        if (reminder.hasOdometerTrigger && reminder.targetOdometerKm != null && currentVehicleOdometer != null && currentVehicleOdometer > 0.0) {
            val targetOdo = reminder.targetOdometerKm
            if (currentVehicleOdometer >= targetOdo) {
                isOverdue = true
            } else if (targetOdo - currentVehicleOdometer <= 500.0) {
                isDueSoon = true
            }
        }

        return when {
            isOverdue -> ReminderDueStatus.OVERDUE
            isDueSoon -> ReminderDueStatus.DUE_SOON
            else -> ReminderDueStatus.UPCOMING
        }
    }
}
