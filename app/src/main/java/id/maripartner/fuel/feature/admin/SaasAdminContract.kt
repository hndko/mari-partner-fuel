package id.maripartner.fuel.feature.admin

import id.maripartner.fuel.domain.model.AdminUserSummary
import id.maripartner.fuel.domain.model.SaasAdminMetrics

data class SaasAdminUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedTimeframe: String = "30d",
    val metrics: SaasAdminMetrics = SaasAdminMetrics(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedUserForGrantPro: AdminUserSummary? = null,
    val isGrantingPro: Boolean = false,
    val grantDurationDays: Int = 30,
    val grantNotes: String = "",
    val searchQuery: String = ""
) {
    val filteredRecentUsers: List<AdminUserSummary>
        get() = if (searchQuery.isBlank()) {
            metrics.recentUsers
        } else {
            metrics.recentUsers.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true) ||
                it.planName.contains(searchQuery, ignoreCase = true)
            }
        }
}

sealed interface SaasAdminUiEvent {
    data class ChangeTimeframe(val timeframe: String) : SaasAdminUiEvent
    data object Refresh : SaasAdminUiEvent
    data class UpdateSearchQuery(val query: String) : SaasAdminUiEvent
    data class OpenGrantProDialog(val user: AdminUserSummary) : SaasAdminUiEvent
    data object DismissGrantProDialog : SaasAdminUiEvent
    data class ChangeGrantDuration(val days: Int) : SaasAdminUiEvent
    data class ChangeGrantNotes(val notes: String) : SaasAdminUiEvent
    data object SubmitGrantPro : SaasAdminUiEvent
    data object DismissMessages : SaasAdminUiEvent
}
