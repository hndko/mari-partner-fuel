package id.maripartner.fuel.feature.subscription

import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.model.UserEntitlement

data class SubscriptionUiState(
    val isLoading: Boolean = true,
    val isProcessingAction: Boolean = false,
    val userEntitlement: UserEntitlement? = null,
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedBillingPeriod: String = "monthly", // "monthly" atau "yearly"
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val actionSuccessMessage: String? = null,
    val showComingSoonDialog: Boolean = false,
    val selectedPlanForCheckout: SubscriptionPlan? = null,
    val showDowngradeDialog: Boolean = false,
    val showRenewalDialog: Boolean = false,
    val showSwitchCycleDialog: Boolean = false,
    val targetPlan: SubscriptionPlan? = null
)

sealed interface SubscriptionUiEvent {
    data object LoadData : SubscriptionUiEvent
    data object Refresh : SubscriptionUiEvent
    data class SelectBillingPeriod(val period: String) : SubscriptionUiEvent
    data class SelectPlan(val plan: SubscriptionPlan) : SubscriptionUiEvent
    data class RequestDowngrade(val freePlan: SubscriptionPlan) : SubscriptionUiEvent
    data class RequestRenewal(val currentPlan: SubscriptionPlan) : SubscriptionUiEvent
    data class RequestSwitchCycle(val targetPlan: SubscriptionPlan) : SubscriptionUiEvent
    data object ConfirmDowngrade : SubscriptionUiEvent
    data object DismissComingSoon : SubscriptionUiEvent
    data object DismissDowngradeDialog : SubscriptionUiEvent
    data object DismissRenewalDialog : SubscriptionUiEvent
    data object DismissSwitchCycleDialog : SubscriptionUiEvent
    data object DismissSuccessMessage : SubscriptionUiEvent
    data object DismissError : SubscriptionUiEvent
}
