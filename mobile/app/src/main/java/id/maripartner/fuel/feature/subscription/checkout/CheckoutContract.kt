package id.maripartner.fuel.feature.subscription.checkout

import id.maripartner.fuel.domain.model.PaymentOrder
import id.maripartner.fuel.domain.model.PaymentProviderType
import id.maripartner.fuel.domain.model.SubscriptionPlan

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val plan: SubscriptionPlan? = null,
    val selectedProvider: PaymentProviderType = PaymentProviderType.GOOGLE_PLAY,
    val isCreatingOrder: Boolean = false,
    val createdOrder: PaymentOrder? = null,
    val errorMessage: String? = null,
    val isPaymentSuccess: Boolean = false
)

sealed interface CheckoutUiEvent {
    data class SelectProvider(val provider: PaymentProviderType) : CheckoutUiEvent
    data object StartCheckout : CheckoutUiEvent
    data class HandlePlayPurchaseSuccess(
        val purchaseToken: String,
        val orderId: String,
        val productId: String,
        val basePlanId: String
    ) : CheckoutUiEvent
    data object DismissError : CheckoutUiEvent
}
