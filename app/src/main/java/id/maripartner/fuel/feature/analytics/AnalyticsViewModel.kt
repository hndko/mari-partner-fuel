package id.maripartner.fuel.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.MariPartnerFuelApp
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.data.repository.AuthRepositoryImpl
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.data.repository.MaintenanceReminderRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.AnalyticsPeriod
import id.maripartner.fuel.domain.model.AnalyticsReport
import id.maripartner.fuel.domain.model.EcoDriverReport
import id.maripartner.fuel.domain.model.FuelAnomalyResult
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.TransactionFilter
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleTcoResult
import id.maripartner.fuel.domain.repository.AuthRepository
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.repository.MaintenanceReminderRepository
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.analytics.CalculateFuelEfficiencyUseCase
import id.maripartner.fuel.domain.usecase.analytics.CalculateVehicleTcoUseCase
import id.maripartner.fuel.domain.usecase.analytics.DetectFuelConsumptionAnomalyUseCase
import id.maripartner.fuel.domain.usecase.analytics.GenerateEcoScoreUseCase
import id.maripartner.fuel.domain.usecase.analytics.GetAnalyticsReportUseCase
import id.maripartner.fuel.domain.usecase.export.GenerateEcoCertificatePdfUseCase
import id.maripartner.fuel.domain.usecase.subscription.GetMyEntitlementsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

/**
 * ViewModel pengelola data analitik pengeluaran dan estimasi konsumsi BBM.
 */
class AnalyticsViewModel(
    private val transactionRepository: FuelTransactionRepository = FuelTransactionRepositoryImpl(),
    private val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(),
    private val reminderRepository: MaintenanceReminderRepository = MaintenanceReminderRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val getAnalyticsReportUseCase: GetAnalyticsReportUseCase = GetAnalyticsReportUseCase(
        transactionRepository,
        CalculateFuelEfficiencyUseCase()
    ),
    private val detectFuelConsumptionAnomalyUseCase: DetectFuelConsumptionAnomalyUseCase = DetectFuelConsumptionAnomalyUseCase(),
    private val getMyEntitlementsUseCase: GetMyEntitlementsUseCase = GetMyEntitlementsUseCase(),
    private val calculateVehicleTcoUseCase: CalculateVehicleTcoUseCase = CalculateVehicleTcoUseCase(),
    private val generateEcoScoreUseCase: GenerateEcoScoreUseCase = GenerateEcoScoreUseCase(),
    private val generateEcoCertificatePdfUseCase: GenerateEcoCertificatePdfUseCase = GenerateEcoCertificatePdfUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadVehicles()
        loadReport()
    }

    fun onEvent(event: AnalyticsUiEvent) {
        when (event) {
            is AnalyticsUiEvent.SelectPeriod -> {
                _uiState.update { it.copy(selectedPeriod = event.period) }
                loadReport()
            }
            is AnalyticsUiEvent.SelectCustomDateRange -> {
                _uiState.update {
                    it.copy(
                        selectedPeriod = AnalyticsPeriod.CUSTOM,
                        customStartDate = event.startDate,
                        customEndDate = event.endDate
                    )
                }
                loadReport()
            }
            is AnalyticsUiEvent.SelectVehicle -> {
                _uiState.update { it.copy(selectedVehicleId = event.vehicleId) }
                loadReport()
            }
            is AnalyticsUiEvent.UpdateDriverRevenueRate -> {
                _uiState.update { it.copy(driverRevenueRateIdr = event.rateIdr) }
                loadReport()
            }
            is AnalyticsUiEvent.SetShowTcoMarginDialog -> {
                _uiState.update { it.copy(showTcoMarginDialog = event.show) }
            }
            is AnalyticsUiEvent.ExportEcoCertificate -> exportEcoCertificate()
            is AnalyticsUiEvent.ClearExportedCertificate -> _uiState.update { it.copy(exportedCertificateFile = null) }
            is AnalyticsUiEvent.Refresh -> loadReport(isRefresh = true)
            is AnalyticsUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getVehicles(includeArchived = false).collect { result ->
                if (result is AppResult.Success) {
                    _uiState.update { it.copy(availableVehicles = result.data) }
                }
            }
        }
    }

    fun loadReport(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(reportState = UiState.Loading) }
            }

            // Cek status Pro pengguna
            var isPro = false
            try {
                when (val ent = getMyEntitlementsUseCase()) {
                    is AppResult.Success -> isPro = ent.data.isPro
                    else -> {}
                }
            } catch (_: Exception) {}

            // Deteksi Anomali Konsumsi BBM (Prioritas 3) & Kalkulasi TCO / Eco Score
            var anomalyResult: FuelAnomalyResult? = null
            var vehicleTcoResult: VehicleTcoResult? = null
            var ecoDriverReport: EcoDriverReport? = null

            val targetVehicle = if (currentState.selectedVehicleId != null) {
                currentState.availableVehicles.find { it.id == currentState.selectedVehicleId }
            } else {
                currentState.availableVehicles.firstOrNull()
            }
            val targetVehicleId = targetVehicle?.id

            var vehicleTransactions: List<FuelTransaction> = emptyList()
            if (targetVehicleId != null) {
                try {
                    val txRes = transactionRepository.getTransactions(
                        filter = TransactionFilter(vehicleId = targetVehicleId),
                        limit = 100
                    )
                    if (txRes is AppResult.Success) {
                        vehicleTransactions = txRes.data
                        anomalyResult = detectFuelConsumptionAnomalyUseCase.detectAnomaly(targetVehicleId, vehicleTransactions)
                    }
                } catch (_: Exception) {}
            } else {
                try {
                    val txRes = transactionRepository.getTransactions(filter = TransactionFilter(), limit = 100)
                    if (txRes is AppResult.Success) {
                        vehicleTransactions = txRes.data
                    }
                } catch (_: Exception) {}
            }

            // Ambil pengeluaran servis dari Reminder (jika ada jadwal servis, rata-rata servis rutin ~Rp 250.000)
            var maintenanceCost = 0L
            try {
                val remRes = reminderRepository.getReminders(vehicleId = targetVehicleId)
                if (remRes is AppResult.Success) {
                    maintenanceCost = remRes.data.size * 250_000L
                }
            } catch (_: Exception) {}

            // Hitung TCO (True Cost per Km)
            if (vehicleTransactions.isNotEmpty()) {
                vehicleTcoResult = calculateVehicleTcoUseCase(
                    transactions = vehicleTransactions,
                    totalMaintenanceCostIdr = maintenanceCost,
                    driverRevenueRatePerKm = currentState.driverRevenueRateIdr
                )
            }

            // Ambil nama profil pengguna untuk sertifikat Eco-Driver
            var driverName = "Pengemudi"
            try {
                when (val p = authRepository.getUserProfile()) {
                    is AppResult.Success -> {
                        p.data.displayName?.takeIf { it.isNotBlank() }?.let { driverName = it }
                    }
                    else -> {}
                }
            } catch (_: Exception) {}

            // Hitung Skor Eco-Driver & Emisi CO2
            if (vehicleTransactions.isNotEmpty()) {
                ecoDriverReport = generateEcoScoreUseCase(
                    driverName = driverName,
                    vehicleName = targetVehicle?.name ?: "Kendaraan Utama",
                    vehicleType = targetVehicle?.vehicleType?.displayName ?: "Mobil",
                    transactions = vehicleTransactions
                )
            }

            val result = getAnalyticsReportUseCase(
                period = currentState.selectedPeriod,
                customStartDate = currentState.customStartDate,
                customEndDate = currentState.customEndDate,
                vehicleId = currentState.selectedVehicleId,
                today = LocalDate.now()
            )

            when (result) {
                is AppResult.Success -> {
                    val report = result.data
                    val state = if (report.transactionCount == 0) {
                        UiState.Empty("Tidak ada data transaksi BBM pada periode yang dipilih.")
                    } else {
                        UiState.Success(report)
                    }
                    _uiState.update {
                        it.copy(
                            reportState = state,
                            fuelAnomalyResult = anomalyResult,
                            vehicleTcoResult = vehicleTcoResult,
                            ecoDriverReport = ecoDriverReport,
                            isProUser = isPro,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    val isAuthError = result.exception is id.maripartner.fuel.core.common.result.AppException.Authentication ||
                        result.exception.message?.contains("login", ignoreCase = true) == true
                    val fallbackState: UiState<AnalyticsReport> = if (isAuthError) {
                        UiState.Empty("Belum ada data transaksi")
                    } else {
                        UiState.Error(result.exception.message ?: "Gagal memuat analitik.")
                    }
                    _uiState.update {
                        it.copy(
                            reportState = fallbackState,
                            fuelAnomalyResult = anomalyResult,
                            vehicleTcoResult = vehicleTcoResult,
                            ecoDriverReport = ecoDriverReport,
                            isProUser = isPro,
                            isRefreshing = false,
                            errorMessage = if (fallbackState is UiState.Error) result.exception.message else null
                        )
                    }
                }
            }
        }
    }

    private fun exportEcoCertificate() {
        val report = _uiState.value.ecoDriverReport ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExportingCertificate = true) }
            try {
                val file = withContext(Dispatchers.IO) {
                    val cacheDir = MariPartnerFuelApp.instance.cacheDir
                    val exportDir = File(cacheDir, "certificates").apply { mkdirs() }
                    val targetFile = File(exportDir, "Sertifikat_Eco_Driver_${report.certificateId}.pdf")
                    generateEcoCertificatePdfUseCase.generateCertificateFile(report, targetFile)
                }
                _uiState.update {
                    it.copy(
                        isExportingCertificate = false,
                        exportedCertificateFile = file
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExportingCertificate = false,
                        errorMessage = "Gagal membuat sertifikat: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}
