package id.maripartner.fuel.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.AnalyticsPeriod
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.analytics.CalculateFuelEfficiencyUseCase
import id.maripartner.fuel.domain.usecase.analytics.GetAnalyticsReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel pengelola data analitik pengeluaran dan estimasi konsumsi BBM.
 */
class AnalyticsViewModel(
    private val transactionRepository: FuelTransactionRepository = FuelTransactionRepositoryImpl(),
    private val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(),
    private val getAnalyticsReportUseCase: GetAnalyticsReportUseCase = GetAnalyticsReportUseCase(
        transactionRepository,
        CalculateFuelEfficiencyUseCase()
    )
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
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            reportState = UiState.Error(result.exception.message ?: "Gagal memuat analitik."),
                            isRefreshing = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }
}
