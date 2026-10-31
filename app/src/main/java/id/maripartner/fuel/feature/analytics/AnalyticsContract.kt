package id.maripartner.fuel.feature.analytics

import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.domain.model.AnalyticsPeriod
import id.maripartner.fuel.domain.model.AnalyticsReport
import id.maripartner.fuel.domain.model.EcoDriverReport
import id.maripartner.fuel.domain.model.FuelAnomalyResult
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleTcoResult
import java.io.File
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
    val fuelAnomalyResult: FuelAnomalyResult? = null,
    val vehicleTcoResult: VehicleTcoResult? = null,
    val ecoDriverReport: EcoDriverReport? = null,
    val driverRevenueRateIdr: Long = 3500L,
    val isExportingCertificate: Boolean = false,
    val exportedCertificateFile: File? = null,
    val showTcoMarginDialog: Boolean = false,
    val isProUser: Boolean = false,
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
    data class UpdateDriverRevenueRate(val rateIdr: Long) : AnalyticsUiEvent
    data object ExportEcoCertificate : AnalyticsUiEvent
    data object ClearExportedCertificate : AnalyticsUiEvent
    data class SetShowTcoMarginDialog(val show: Boolean) : AnalyticsUiEvent
    data object Refresh : AnalyticsUiEvent
    data object ClearError : AnalyticsUiEvent
}
