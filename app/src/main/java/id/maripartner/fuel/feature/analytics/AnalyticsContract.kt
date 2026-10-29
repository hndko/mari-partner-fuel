package id.maripartner.fuel.feature.analytics

import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.domain.model.AnalyticsPeriod
import id.maripartner.fuel.domain.model.AnalyticsReport
import id.maripartner.fuel.domain.model.Vehicle
import java.time.LocalDate

/**
 * State untuk layar Analitik & Konsumsi BBM.
 */
data class AnalyticsUiState(
    val reportState: UiState<AnalyticsReport> = UiState.Loading,
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.THIS_MONTH,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val selectedVehicleId: String? = null,
    val availableVehicles: List<Vehicle> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Event interaksi pengguna pada layar Analitik.
 */
sealed interface AnalyticsUiEvent {
    data class SelectPeriod(val period: AnalyticsPeriod) : AnalyticsUiEvent
    data class SelectCustomDateRange(val startDate: LocalDate, val endDate: LocalDate) : AnalyticsUiEvent
    data class SelectVehicle(val vehicleId: String?) : AnalyticsUiEvent
    data object Refresh : AnalyticsUiEvent
    data object ClearError : AnalyticsUiEvent
}
