package id.maripartner.fuel.feature.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.core.notification.ReminderNotificationScheduler
import id.maripartner.fuel.data.repository.MaintenanceReminderRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.MaintenanceReminder
import id.maripartner.fuel.domain.model.ReminderDueStatus
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.repository.MaintenanceReminderRepository
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.reminder.CheckReminderDueStatusUseCase
import id.maripartner.fuel.domain.usecase.reminder.ValidateReminderInputUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel pengelola daftar dan form pengingat servis perawatan kendaraan.
 */
class RemindersViewModel @JvmOverloads constructor(
    application: Application,
    private val reminderRepository: MaintenanceReminderRepository = MaintenanceReminderRepositoryImpl(),
    private val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(),
    private val validateReminderInputUseCase: ValidateReminderInputUseCase = ValidateReminderInputUseCase(),
    private val checkDueStatusUseCase: CheckReminderDueStatusUseCase = CheckReminderDueStatusUseCase()
) : AndroidViewModel(application) {

    private val notificationScheduler = ReminderNotificationScheduler(application)

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ReminderFormUiState())
    val formState: StateFlow<ReminderFormUiState> = _formState.asStateFlow()

    init {
        loadVehiclesAndReminders()
    }

    fun onEvent(event: RemindersUiEvent) {
        when (event) {
            is RemindersUiEvent.SelectTab -> _uiState.update { it.copy(selectedTab = event.tab) }
            is RemindersUiEvent.SelectFilterStatus -> _uiState.update { it.copy(selectedFilterStatus = event.status) }
            is RemindersUiEvent.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.query) }
            is RemindersUiEvent.ToggleComplete -> toggleComplete(event.reminderId, event.completed)
            is RemindersUiEvent.DeleteReminder -> deleteReminder(event.reminderId)
            is RemindersUiEvent.Refresh -> loadVehiclesAndReminders(isRefresh = true)
            is RemindersUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            is RemindersUiEvent.ClearSuccessMessage -> _uiState.update { it.copy(actionSuccessMessage = null) }
        }
    }

    fun onFormEvent(event: ReminderFormUiEvent) {
        when (event) {
            is ReminderFormUiEvent.TitleChanged -> _formState.update { it.copy(title = event.title) }
            is ReminderFormUiEvent.VehicleSelected -> _formState.update { it.copy(vehicleId = event.vehicleId) }
            is ReminderFormUiEvent.ReminderTypeChanged -> _formState.update { it.copy(reminderType = event.type) }
            is ReminderFormUiEvent.TargetOdometerChanged -> _formState.update { it.copy(targetOdometerStr = event.odoStr) }
            is ReminderFormUiEvent.TargetDateChanged -> _formState.update { it.copy(targetDate = event.date) }
            is ReminderFormUiEvent.NotesChanged -> _formState.update { it.copy(notes = event.notes) }
            is ReminderFormUiEvent.SaveReminder -> saveReminder()
            is ReminderFormUiEvent.DismissSuccessDialog -> _formState.update { it.copy(showSuccessDialog = false) }
            is ReminderFormUiEvent.ClearError -> _formState.update { it.copy(errorMessage = null) }
        }
    }

    fun initFormForEdit(reminderId: String?) {
        viewModelScope.launch {
            loadVehiclesOnly()
            if (reminderId.isNullOrBlank()) {
                val defaultVehicle = _uiState.value.vehiclesMap.values.firstOrNull()?.id.orEmpty()
                _formState.value = ReminderFormUiState(
                    availableVehicles = _uiState.value.vehiclesMap.values.toList(),
                    vehicleId = defaultVehicle
                )
            } else {
                when (val result = reminderRepository.getReminderById(reminderId)) {
                    is AppResult.Success -> {
                        val reminder = result.data
                        _formState.value = ReminderFormUiState(
                            reminderId = reminder.id,
                            title = reminder.title,
                            vehicleId = reminder.vehicleId,
                            reminderType = reminder.reminderType,
                            targetOdometerStr = reminder.targetOdometerKm?.let { String.format(java.util.Locale.US, "%.0f", it) }.orEmpty(),
                            targetDate = reminder.targetDate,
                            notes = reminder.notes.orEmpty(),
                            availableVehicles = _uiState.value.vehiclesMap.values.toList()
                        )
                    }
                    is AppResult.Error -> {
                        _formState.update { it.copy(errorMessage = result.exception.message) }
                    }
                }
            }
        }
    }

    private fun loadVehiclesOnly() {
        viewModelScope.launch {
            vehicleRepository.getVehicles(includeArchived = false).collect { result ->
                if (result is AppResult.Success) {
                    val map = result.data.associateBy { it.id }
                    _uiState.update { it.copy(vehiclesMap = map) }
                    _formState.update { it.copy(availableVehicles = result.data) }
                }
            }
        }
    }

    fun loadVehiclesAndReminders(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(remindersState = UiState.Loading) }
            }

            // 1. Ambil kendaraan
            vehicleRepository.getVehicles(includeArchived = true).collect { vResult ->
                val vMap = if (vResult is AppResult.Success) vResult.data.associateBy { it.id } else emptyMap()

                // 2. Ambil pengingat
                when (val rResult = reminderRepository.getReminders()) {
                    is AppResult.Success -> {
                        val reminders = rResult.data
                        val dueMap = reminders.associate { reminder ->
                            val currentOdo = vMap[reminder.vehicleId]?.lastOdometerKm
                            reminder.id to checkDueStatusUseCase(reminder, currentOdo, LocalDate.now())
                        }

                        val state = if (reminders.isEmpty()) {
                            UiState.Empty("Belum ada pengingat perawatan kendaraan.")
                        } else {
                            UiState.Success(reminders)
                        }

                        _uiState.update {
                            it.copy(
                                remindersState = state,
                                allReminders = reminders,
                                dueStatusMap = dueMap,
                                vehiclesMap = vMap,
                                isRefreshing = false,
                                errorMessage = null
                            )
                        }
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                remindersState = UiState.Error(rResult.exception.message ?: "Gagal memuat pengingat."),
                                isRefreshing = false,
                                errorMessage = rResult.exception.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun toggleComplete(reminderId: String, completed: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            when (val result = reminderRepository.markAsCompleted(reminderId, completed)) {
                is AppResult.Success -> {
                    val updated = result.data
                    if (completed) {
                        notificationScheduler.cancelReminder(reminderId)
                    } else {
                        notificationScheduler.scheduleReminder(updated)
                    }
                    _uiState.update { 
                        it.copy(
                            actionSuccessMessage = if (completed) "Pengingat ditandai selesai." else "Pengingat diaktifkan kembali."
                        )
                    }
                    loadVehiclesAndReminders()
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(actionLoading = false, errorMessage = result.exception.message) }
                }
            }
        }
    }

    private fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            notificationScheduler.cancelReminder(reminderId)
            when (val result = reminderRepository.deleteReminder(reminderId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(actionSuccessMessage = "Pengingat berhasil dihapus.") }
                    loadVehiclesAndReminders()
                }
                is AppResult.Error -> _uiState.update { it.copy(actionLoading = false, errorMessage = result.exception.message) }
            }
        }
    }

    private fun saveReminder() {
        viewModelScope.launch {
            val form = _formState.value
            _formState.update { it.copy(isLoading = true, errorMessage = null) }

            val odoValue = form.targetOdometerStr.replace(".", "").replace(",", ".").toDoubleOrNull()
            val domainReminder = MaintenanceReminder(
                id = form.reminderId,
                vehicleId = form.vehicleId,
                title = form.title,
                reminderType = form.reminderType,
                targetOdometerKm = odoValue,
                targetDate = form.targetDate,
                notes = form.notes.ifBlank { null }
            )

            // Validasi Input
            when (val validation = validateReminderInputUseCase(domainReminder)) {
                is AppResult.Error -> {
                    _formState.update { it.copy(isLoading = false, errorMessage = validation.exception.message) }
                    return@launch
                }
                is AppResult.Success -> Unit
            }

            val result = if (form.reminderId.isBlank()) {
                reminderRepository.createReminder(domainReminder)
            } else {
                reminderRepository.updateReminder(domainReminder)
            }

            when (result) {
                is AppResult.Success -> {
                    val saved = result.data
                    notificationScheduler.scheduleReminder(saved)
                    _formState.update { it.copy(isLoading = false, isSaved = true, showSuccessDialog = true) }
                    loadVehiclesAndReminders()
                }
                is AppResult.Error -> {
                    _formState.update { it.copy(isLoading = false, errorMessage = result.exception.message) }
                }
            }
        }
    }
}
