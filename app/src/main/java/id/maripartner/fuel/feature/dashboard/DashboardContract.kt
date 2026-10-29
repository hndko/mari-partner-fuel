package id.maripartner.fuel.feature.dashboard

import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.domain.model.DashboardSummary

/**
 * State untuk layar Dashboard.
 */
data class DashboardUiState(
    val summaryState: UiState<DashboardSummary> = UiState.Loading,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Event aksi pada Dashboard.
 */
sealed interface DashboardUiEvent {
    data object Refresh : DashboardUiEvent
    data object ClearError : DashboardUiEvent
}
