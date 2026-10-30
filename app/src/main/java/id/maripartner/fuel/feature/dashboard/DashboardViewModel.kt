package id.maripartner.fuel.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.AuthRepositoryImpl
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.data.repository.MaintenanceReminderRepositoryImpl
import id.maripartner.fuel.data.repository.SubscriptionRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.DashboardSummary
import id.maripartner.fuel.domain.repository.AuthRepository
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.repository.MaintenanceReminderRepository
import id.maripartner.fuel.domain.repository.SubscriptionRepository
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.dashboard.GetDashboardSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel untuk Dashboard Mari Partner Fuel.
 * Mengagregasi ringkasan pengeluaran, status kendaraan, pengingat servis, dan profil pengguna.
 */
class DashboardViewModel(
    private val transactionRepository: FuelTransactionRepository = FuelTransactionRepositoryImpl(),
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase = GetDashboardSummaryUseCase(transactionRepository),
    private val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(),
    private val reminderRepository: MaintenanceReminderRepository = MaintenanceReminderRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.Refresh -> loadDashboard(isRefresh = true)
            is DashboardUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    fun loadDashboard(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            // 1. Ambil ringkasan pengeluaran bulan ini
            var summary = _uiState.value.summary
            when (val result = getDashboardSummaryUseCase(LocalDate.now())) {
                is AppResult.Success -> {
                    summary = result.data
                }
                is AppResult.Error -> {
                    // Simpan fallback kosong jika error jaringan
                    if (summary.transactionCount == 0) {
                        summary = DashboardSummary(currentMonth = LocalDate.now())
                    }
                }
            }

            // 2. Ambil profil user untuk greeting
            var displayName = "Pengemudi"
            try {
                when (val profileRes = authRepository.getUserProfile()) {
                    is AppResult.Success -> {
                        val name = profileRes.data.displayName?.trim()
                        if (!name.isNullOrBlank()) {
                            displayName = name
                        }
                    }
                    else -> {
                        val session = authRepository.getCurrentSession()
                        if (session != null && !session.email.isNullOrBlank()) {
                            displayName = session.email.substringBefore("@").replaceFirstChar { it.uppercase() }
                        }
                    }
                }
            } catch (_: Exception) { }

            // 3. Ambil data kendaraan aktif
            var primaryVehicle = _uiState.value.primaryVehicle
            var totalVehicles = 0
            try {
                val vehiclesFlow = vehicleRepository.getVehicles(includeArchived = false)
                val vehiclesResult = vehiclesFlow.firstOrNull()
                if (vehiclesResult is AppResult.Success) {
                    val vehicles = vehiclesResult.data
                    totalVehicles = vehicles.size
                    primaryVehicle = vehicles.firstOrNull()
                }
            } catch (_: Exception) { }

            // 4. Ambil pengingat servis yang belum selesai
            var pendingReminders = 0
            try {
                val reminderRes = reminderRepository.getReminders(isCompleted = false)
                if (reminderRes is AppResult.Success) {
                    pendingReminders = reminderRes.data.size
                }
            } catch (_: Exception) { }

            // 5. Cek status Pro
            var isPro = false
            try {
                val entitlementRes = subscriptionRepository.getMyEntitlement()
                if (entitlementRes is AppResult.Success) {
                    isPro = entitlementRes.data.isPro
                }
            } catch (_: Exception) { }

            _uiState.update {
                it.copy(
                    summary = summary,
                    primaryVehicle = primaryVehicle,
                    totalVehiclesCount = totalVehicles,
                    pendingRemindersCount = pendingReminders,
                    userName = displayName,
                    isProUser = isPro,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null
                )
            }
        }
    }
}
