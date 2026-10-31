package id.maripartner.fuel.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.MariPartnerFuelApp
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.location.SpbuProximityManager
import id.maripartner.fuel.data.local.FuelBudgetPreferences
import id.maripartner.fuel.data.repository.AuthRepositoryImpl
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.data.repository.MaintenanceReminderRepositoryImpl
import id.maripartner.fuel.data.repository.SubscriptionRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.DashboardSummary
import id.maripartner.fuel.domain.model.FuelAnomalyResult
import id.maripartner.fuel.domain.model.TransactionFilter
import id.maripartner.fuel.domain.repository.AuthRepository
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.repository.MaintenanceReminderRepository
import id.maripartner.fuel.domain.repository.SubscriptionRepository
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.analytics.DetectFuelConsumptionAnomalyUseCase
import id.maripartner.fuel.domain.usecase.budget.CalculateFuelBudgetUseCase
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
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl(),
    private val detectFuelConsumptionAnomalyUseCase: DetectFuelConsumptionAnomalyUseCase = DetectFuelConsumptionAnomalyUseCase(),
    private val calculateFuelBudgetUseCase: CalculateFuelBudgetUseCase = CalculateFuelBudgetUseCase()
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
            is DashboardUiEvent.DismissExpiryBanner -> _uiState.update { it.copy(showExpiryBanner = false) }
            is DashboardUiEvent.SetShowBudgetDialog -> _uiState.update { it.copy(showBudgetDialog = event.show) }
            is DashboardUiEvent.SetShowFuelPriceRadar -> _uiState.update { it.copy(showFuelPriceRadarDialog = event.show) }
            is DashboardUiEvent.SetMonthlyBudget -> updateBudget(event.amountIdr)
            is DashboardUiEvent.CheckNearbySpbu -> checkNearbySpbu()
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

            // 3. Ambil data kendaraan aktif & evaluasi anomali konsumsi BBM
            var primaryVehicle = _uiState.value.primaryVehicle
            var totalVehicles = 0
            var anomalyResult: FuelAnomalyResult? = null
            try {
                val vehiclesFlow = vehicleRepository.getVehicles(includeArchived = false)
                val vehiclesResult = vehiclesFlow.firstOrNull()
                if (vehiclesResult is AppResult.Success) {
                    val vehicles = vehiclesResult.data
                    totalVehicles = vehicles.size
                    primaryVehicle = vehicles.firstOrNull()
                    if (primaryVehicle != null) {
                        val txRes = transactionRepository.getTransactions(
                            filter = TransactionFilter(vehicleId = primaryVehicle.id),
                            limit = 50
                        )
                        if (txRes is AppResult.Success) {
                            anomalyResult = detectFuelConsumptionAnomalyUseCase.detectAnomaly(primaryVehicle.id, txRes.data)
                        }
                    }
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

            // 5. Cek status Pro dan pengingat masa aktif
            var isPro = false
            var isExpiringSoon = false
            var daysUntilExpiry = 0
            var isRecentlyExpired = false
            var planName = "Mari Free"
            var expiresFormatted = ""

            try {
                val entitlementRes = subscriptionRepository.getMyEntitlement(forceRefresh = true)
                if (entitlementRes is AppResult.Success) {
                    val ent = entitlementRes.data
                    isPro = ent.isPro
                    planName = ent.planName.ifBlank { if (ent.isPro) "Mari Pro" else "Mari Free" }
                    val daysLeft = ent.effectiveDaysUntilExpiry ?: Int.MAX_VALUE
                    isExpiringSoon = ent.isExpiringSoon || (ent.isPro && daysLeft <= 7)
                    daysUntilExpiry = if (daysLeft != Int.MAX_VALUE) daysLeft else 0
                    isRecentlyExpired = ent.isRecentlyExpired
                    expiresFormatted = ent.expiresAt?.let { DateUtils.formatFullDate(it) }.orEmpty()
                }
            } catch (_: Exception) { }

            // 6. Hitung Pagu Anggaran BBM & Prediksi "Habis Sebelum Tanggal Tua"
            val appContext = MariPartnerFuelApp.instance
            val budgetLimit = FuelBudgetPreferences.getBudget(appContext, primaryVehicle?.id)
            val thisMonthTransactions = when (val res = transactionRepository.getTransactions(filter = TransactionFilter(), limit = 100)) {
                is AppResult.Success -> {
                    val now = LocalDate.now()
                    res.data.filter { tx ->
                        val txDate = tx.transactionAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        txDate.month == now.month && txDate.year == now.year
                    }
                }
                else -> emptyList()
            }
            val budgetReport = calculateFuelBudgetUseCase(
                budgetLimitIdr = budgetLimit,
                transactions = thisMonthTransactions,
                currentDate = LocalDate.now()
            )

            // 7. Cek Deteksi SPBU Terdekat (on-device location & catalog)
            val nearestSpbu = try {
                SpbuProximityManager.checkProximity(appContext)
            } catch (_: Exception) {
                null
            }

            _uiState.update {
                it.copy(
                    summary = summary,
                    primaryVehicle = primaryVehicle,
                    totalVehiclesCount = totalVehicles,
                    pendingRemindersCount = pendingReminders,
                    fuelAnomalyResult = anomalyResult,
                    fuelBudgetReport = budgetReport,
                    nearestSpbuResult = nearestSpbu,
                    userName = displayName,
                    isProUser = isPro,
                    isExpiringSoon = isExpiringSoon,
                    daysUntilExpiry = daysUntilExpiry,
                    isRecentlyExpired = isRecentlyExpired,
                    planName = planName,
                    expiresAtFormatted = expiresFormatted,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null
                )
            }
        }
    }

    private fun updateBudget(amountIdr: Long) {
        val appContext = MariPartnerFuelApp.instance
        FuelBudgetPreferences.setBudget(appContext, amountIdr, _uiState.value.primaryVehicle?.id)
        _uiState.update { it.copy(showBudgetDialog = false) }
        loadDashboard(isRefresh = true)
    }

    private fun checkNearbySpbu() {
        viewModelScope.launch {
            val appContext = MariPartnerFuelApp.instance
            val nearest = SpbuProximityManager.checkProximity(appContext)
            _uiState.update { it.copy(nearestSpbuResult = nearest) }
        }
    }
}
