package id.maripartner.fuel.feature.subscription

import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.model.UserEntitlement

data class SubscriptionUiState(
    val isLoading: Boolean = true,
    val userEntitlement: UserEntitlement? = null,
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedBillingPeriod: String = "monthly", // "monthly" atau "yearly"
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val showComingSoonDialog: Boolean = false,
    val selectedPlanForCheckout: SubscriptionPlan? = null
)

sealed interface SubscriptionUiEvent {
    data object LoadData : SubscriptionUiEvent
    data object Refresh : SubscriptionUiEvent
    data class SelectBillingPeriod(val period: String) : SubscriptionUiEvent
    data class SelectPlan(val plan: SubscriptionPlan) : SubscriptionUiEvent
    data object DismissComingSoon : SubscriptionUiEvent
    data object DismissError : SubscriptionUiEvent
}
