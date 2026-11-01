package id.maripartner.fuel.feature.reminders

import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.domain.model.MaintenanceReminder
import id.maripartner.fuel.domain.model.ReminderDueStatus
import id.maripartner.fuel.domain.model.ReminderType
import id.maripartner.fuel.domain.model.Vehicle
import java.time.LocalDate

enum class ReminderTab(val label: String) {
    ACTIVE("Aktif & Jatuh Tempo"),
    COMPLETED("Riwayat Selesai")
}

enum class ReminderFilterStatus(val label: String) {
    ALL("Semua Status"),
    DUE_SOON("Segera Datang"),
    OVERDUE("Lewat Jatuh Tempo")
}

data class RemindersUiState(
    val remindersState: UiState<List<MaintenanceReminder>> = UiState.Loading,
    val selectedTab: ReminderTab = ReminderTab.ACTIVE,
    val selectedFilterStatus: ReminderFilterStatus = ReminderFilterStatus.ALL,
    val searchQuery: String = "",
    val allReminders: List<MaintenanceReminder> = emptyList(),
    val dueStatusMap: Map<String, ReminderDueStatus> = emptyMap(),
    val vehiclesMap: Map<String, Vehicle> = emptyMap(),
    val isRefreshing: Boolean = false,
    val actionLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

sealed interface RemindersUiEvent {
    data class SelectTab(val tab: ReminderTab) : RemindersUiEvent
    data class SelectFilterStatus(val status: ReminderFilterStatus) : RemindersUiEvent
    data class SearchQueryChanged(val query: String) : RemindersUiEvent
    data class ToggleComplete(val reminderId: String, val completed: Boolean) : RemindersUiEvent
    data class DeleteReminder(val reminderId: String) : RemindersUiEvent
    data object Refresh : RemindersUiEvent
    data object ClearError : RemindersUiEvent
    data object ClearSuccessMessage : RemindersUiEvent
}

data class ReminderFormUiState(
    val reminderId: String = "",
    val title: String = "",
    val vehicleId: String = "",
    val reminderType: ReminderType = ReminderType.BOTH,
    val targetOdometerStr: String = "",
    val targetDate: LocalDate? = null,
    val notes: String = "",
    val availableVehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ReminderFormUiEvent {
    data class TitleChanged(val title: String) : ReminderFormUiEvent
    data class VehicleSelected(val vehicleId: String) : ReminderFormUiEvent
    data class ReminderTypeChanged(val type: ReminderType) : ReminderFormUiEvent
    data class TargetOdometerChanged(val odoStr: String) : ReminderFormUiEvent
    data class TargetDateChanged(val date: LocalDate?) : ReminderFormUiEvent
    data class NotesChanged(val notes: String) : ReminderFormUiEvent
    data object SaveReminder : ReminderFormUiEvent
    data object DismissSuccessDialog : ReminderFormUiEvent
    data object ClearError : ReminderFormUiEvent
}
