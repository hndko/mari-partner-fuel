package id.maripartner.fuel.domain.usecase.reminder

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.MaintenanceReminder
import id.maripartner.fuel.domain.model.ReminderType
import java.time.LocalDate

/**
 * Validasi fail-fast untuk form tambah atau edit pengingat perawatan.
 */
class ValidateReminderInputUseCase {

    operator fun invoke(
        reminder: MaintenanceReminder,
        currentDate: LocalDate = LocalDate.now()
    ): AppResult<Unit> {
        if (reminder.title.isBlank()) {
            return AppResult.Error(AppException.Validation("Judul pengingat perawatan wajib diisi."))
        }

        if (reminder.vehicleId.isBlank()) {
            return AppResult.Error(AppException.Validation("Kendaraan wajib dipilih."))
        }

        val hasOdo = reminder.targetOdometerKm != null && reminder.targetOdometerKm > 0.0
        val hasDate = reminder.targetDate != null

        when (reminder.reminderType) {
            ReminderType.ODOMETER -> {
                if (!hasOdo) {
                    return AppResult.Error(AppException.Validation("Target odometer kilometer wajib diisi dan harus lebih dari 0."))
                }
            }
            ReminderType.DATE -> {
                if (!hasDate) {
                    return AppResult.Error(AppException.Validation("Tanggal target pengingat wajib dipilih."))
                }
            }
            ReminderType.BOTH -> {
                if (!hasOdo && !hasDate) {
                    return AppResult.Error(AppException.Validation("Minimal salah satu target (odometer atau tanggal) wajib diisi."))
                }
            }
        }

        if (!hasOdo && !hasDate) {
            return AppResult.Error(AppException.Validation("Pengingat harus memiliki minimal satu target jatuh tempo (odometer atau tanggal)."))
        }

        return AppResult.Success(Unit)
    }
}
