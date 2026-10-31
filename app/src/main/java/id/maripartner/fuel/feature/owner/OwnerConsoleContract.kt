package id.maripartner.fuel.feature.owner

import id.maripartner.fuel.domain.model.owner.AdminUserSummary
import id.maripartner.fuel.domain.model.owner.OwnerPaymentTx
import id.maripartner.fuel.domain.model.owner.OwnerPlanItem
import id.maripartner.fuel.domain.model.owner.SaasAdminMetrics
import id.maripartner.fuel.feature.owner.navigation.OwnerTab

data class OwnerConsoleUiState(
    val currentTab: OwnerTab = OwnerTab.DASHBOARD,
    val selectedTimeframe: String = "30d",
    val metrics: SaasAdminMetrics = SaasAdminMetrics(),
    val plans: List<OwnerPlanItem> = emptyList(),
    val transactions: List<OwnerPaymentTx> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    // State Dialog Aktivasi Pro Manual
    val selectedUserForGrantPro: AdminUserSummary? = null,
    val grantDurationDays: Int = 30,
    val grantNotes: String = "",
    val isSubmittingGrant: Boolean = false,
    // State Dialog Pengaturan Paket & Harga
    val selectedPlanForEdit: OwnerPlanItem? = null,
    val editPlanName: String = "",
    val editPlanPriceIdr: String = "",
    val editPlanAiQuota: String = "",
    val editPlanMaxVehicles: String = "",
    val editPlanIsActive: Boolean = true,
    val editPlanDescription: String = "",
    val editPlanIsPromoActive: Boolean = false,
    val editPlanPromoPriceIdr: String = "",
    val editPlanPromoMaxPerUser: String = "",
    val editPlanPromoLabel: String = "PROMO SPESIAL",
    val isSubmittingPlanEdit: Boolean = false
) {
    val filteredUsers: List<AdminUserSummary>
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

sealed interface OwnerConsoleUiEvent {
    data class SelectTab(val tab: OwnerTab) : OwnerConsoleUiEvent
    data class ChangeTimeframe(val timeframe: String) : OwnerConsoleUiEvent
    data object Refresh : OwnerConsoleUiEvent
    data class UpdateSearchQuery(val query: String) : OwnerConsoleUiEvent

    // Event Manajemen Pro
    data class OpenGrantProDialog(val user: AdminUserSummary) : OwnerConsoleUiEvent
    data object DismissGrantProDialog : OwnerConsoleUiEvent
    data class ChangeGrantDuration(val days: Int) : OwnerConsoleUiEvent
    data class ChangeGrantNotes(val notes: String) : OwnerConsoleUiEvent
    data object SubmitGrantPro : OwnerConsoleUiEvent

    // Event Manajemen Paket & Harga
    data class OpenEditPlanDialog(val plan: OwnerPlanItem) : OwnerConsoleUiEvent
    data object DismissEditPlanDialog : OwnerConsoleUiEvent
    data class ChangeEditPlanName(val name: String) : OwnerConsoleUiEvent
    data class ChangeEditPlanPrice(val price: String) : OwnerConsoleUiEvent
    data class ChangeEditPlanAiQuota(val quota: String) : OwnerConsoleUiEvent
    data class ChangeEditPlanMaxVehicles(val vehicles: String) : OwnerConsoleUiEvent
    data class ChangeEditPlanIsActive(val active: Boolean) : OwnerConsoleUiEvent
    data class ChangeEditPlanDescription(val desc: String) : OwnerConsoleUiEvent
    data class ChangeEditPlanIsPromoActive(val active: Boolean) : OwnerConsoleUiEvent
    data class ChangeEditPlanPromoPrice(val price: String) : OwnerConsoleUiEvent
    data class ChangeEditPlanPromoMaxPerUser(val max: String) : OwnerConsoleUiEvent
    data class ChangeEditPlanPromoLabel(val label: String) : OwnerConsoleUiEvent
    data object SubmitEditPlan : OwnerConsoleUiEvent

    data object DismissMessages : OwnerConsoleUiEvent
}
