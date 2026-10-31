package id.maripartner.fuel.feature.dashboard

import id.maripartner.fuel.domain.model.DashboardSummary
import id.maripartner.fuel.domain.model.FuelAnomalyResult
import id.maripartner.fuel.domain.model.Vehicle

/**
 * State untuk layar Dashboard Mari Partner Fuel yang kaya informasi & interaktif.
 */
data class DashboardUiState(
    val summary: DashboardSummary = DashboardSummary(),
    val primaryVehicle: Vehicle? = null,
    val totalVehiclesCount: Int = 0,
    val pendingRemindersCount: Int = 0,
    val fuelAnomalyResult: FuelAnomalyResult? = null,
    val userName: String = "Pengemudi",
    val isProUser: Boolean = false,
    val isExpiringSoon: Boolean = false,
    val daysUntilExpiry: Int = 0,
    val isRecentlyExpired: Boolean = false,
    val planName: String = "Mari Free",
    val expiresAtFormatted: String = "",
    val showExpiryBanner: Boolean = true,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Event aksi pada Dashboard.
 */
sealed interface DashboardUiEvent {
    data object Refresh : DashboardUiEvent
    data object ClearError : DashboardUiEvent
    data object DismissExpiryBanner : DashboardUiEvent
}
