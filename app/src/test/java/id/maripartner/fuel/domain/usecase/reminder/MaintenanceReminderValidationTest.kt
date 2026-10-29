package id.maripartner.fuel.domain.usecase.reminder

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.MaintenanceReminder
import id.maripartner.fuel.domain.model.ReminderDueStatus
import id.maripartner.fuel.domain.model.ReminderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MaintenanceReminderValidationTest {

    private val validateUseCase = ValidateReminderInputUseCase()
    private val checkDueStatusUseCase = CheckReminderDueStatusUseCase()

    @Test
    fun validate_failsWhenTitleIsBlank() {
        val reminder = MaintenanceReminder(
            title = "   ",
            vehicleId = "v1",
            reminderType = ReminderType.DATE,
            targetDate = LocalDate.now().plusDays(10)
        )
        val result = validateUseCase(reminder)
        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).exception.message!!.contains("Judul"))
    }

    @Test
    fun validate_failsWhenVehicleIsEmpty() {
        val reminder = MaintenanceReminder(
            title = "Ganti Oli",
            vehicleId = "",
            reminderType = ReminderType.DATE,
            targetDate = LocalDate.now().plusDays(10)
        )
        val result = validateUseCase(reminder)
        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).exception.message!!.contains("Kendaraan"))
    }

    @Test
    fun validate_failsWhenNeitherOdometerNorDateIsSet() {
        val reminder = MaintenanceReminder(
            title = "Ganti Oli",
            vehicleId = "v1",
            reminderType = ReminderType.BOTH,
            targetOdometerKm = null,
            targetDate = null
        )
        val result = validateUseCase(reminder)
        assertTrue(result is AppResult.Error)
    }

    @Test
    fun validate_succeedsWithDateOnly() {
        val reminder = MaintenanceReminder(
            title = "Servis Berkala 6 Bulan",
            vehicleId = "v1",
            reminderType = ReminderType.DATE,
            targetDate = LocalDate.now().plusMonths(2)
        )
        val result = validateUseCase(reminder)
        assertTrue(result is AppResult.Success)
    }

    @Test
    fun validate_succeedsWithOdometerOnly() {
        val reminder = MaintenanceReminder(
            title = "Ganti Busi",
            vehicleId = "v1",
            reminderType = ReminderType.ODOMETER,
            targetOdometerKm = 20_000.0
        )
        val result = validateUseCase(reminder)
        assertTrue(result is AppResult.Success)
    }

    @Test
    fun validate_succeedsWithBothTriggers() {
        val reminder = MaintenanceReminder(
            title = "Ganti Oli Mesin & Filter",
            vehicleId = "v1",
            reminderType = ReminderType.BOTH,
            targetDate = LocalDate.now().plusMonths(3),
            targetOdometerKm = 15_000.0
        )
        val result = validateUseCase(reminder)
        assertTrue(result is AppResult.Success)
    }

    @Test
    fun checkDueStatus_evaluatesOverdueAndUpcomingCorrectly() {
        val today = LocalDate.of(2026, 10, 29)

        // 1. Sudah lewat tanggal
        val pastReminder = MaintenanceReminder(
            vehicleId = "v1", title = "Servis",
            reminderType = ReminderType.DATE,
            targetDate = today.minusDays(2)
        )
        assertEquals(ReminderDueStatus.OVERDUE, checkDueStatusUseCase(pastReminder, today = today))

        // 2. Mendekati tanggal (dalam 7 hari)
        val soonReminder = MaintenanceReminder(
            vehicleId = "v1", title = "Servis",
            reminderType = ReminderType.DATE,
            targetDate = today.plusDays(4)
        )
        assertEquals(ReminderDueStatus.DUE_SOON, checkDueStatusUseCase(soonReminder, today = today))

        // 3. Masih lama
        val upcomingReminder = MaintenanceReminder(
            vehicleId = "v1", title = "Servis",
            reminderType = ReminderType.DATE,
            targetDate = today.plusDays(30)
        )
        assertEquals(ReminderDueStatus.UPCOMING, checkDueStatusUseCase(upcomingReminder, today = today))

        // 4. Odometer sudah lewat target
        val odoOverdue = MaintenanceReminder(
            vehicleId = "v1", title = "Ganti Oli",
            reminderType = ReminderType.ODOMETER,
            targetOdometerKm = 10_000.0
        )
        assertEquals(ReminderDueStatus.OVERDUE, checkDueStatusUseCase(odoOverdue, currentVehicleOdometer = 10_050.0, today = today))

        // 5. Odometer mendekati (dalam 500 km)
        val odoSoon = MaintenanceReminder(
            vehicleId = "v1", title = "Ganti Oli",
            reminderType = ReminderType.ODOMETER,
            targetOdometerKm = 10_000.0
        )
        assertEquals(ReminderDueStatus.DUE_SOON, checkDueStatusUseCase(odoSoon, currentVehicleOdometer = 9_700.0, today = today))

        // 6. Selesai
        val completedReminder = MaintenanceReminder(
            vehicleId = "v1", title = "Ganti Oli",
            isCompleted = true,
            targetDate = today.minusDays(10)
        )
        assertEquals(ReminderDueStatus.COMPLETED, checkDueStatusUseCase(completedReminder, today = today))
    }
}
