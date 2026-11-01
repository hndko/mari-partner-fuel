package id.maripartner.fuel.feature.subscription.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.PaymentProviderType
import id.maripartner.fuel.domain.usecase.payment.CreateCheckoutOrderUseCase
import id.maripartner.fuel.domain.usecase.payment.VerifyPlayBillingPurchaseUseCase
import id.maripartner.fuel.domain.usecase.subscription.GetMyEntitlementsUseCase
import id.maripartner.fuel.domain.usecase.subscription.GetSubscriptionPlansUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val getSubscriptionPlansUseCase: GetSubscriptionPlansUseCase = GetSubscriptionPlansUseCase(),
    private val createCheckoutOrderUseCase: CreateCheckoutOrderUseCase = CreateCheckoutOrderUseCase(),
    private val verifyPlayBillingPurchaseUseCase: VerifyPlayBillingPurchaseUseCase = VerifyPlayBillingPurchaseUseCase(),
    private val getMyEntitlementsUseCase: GetMyEntitlementsUseCase = GetMyEntitlementsUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun loadPlan(planCode: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val res = getSubscriptionPlansUseCase()) {
                is AppResult.Success -> {
                    val matching = res.data.firstOrNull { it.code.equals(planCode, ignoreCase = true) }
                    if (matching != null) {
                        _uiState.update { it.copy(isLoading = false, plan = matching) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Paket $planCode tidak ditemukan.") }
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.exception.message) }
                }
            }
        }
    }

    fun onEvent(event: CheckoutUiEvent) {
        when (event) {
            is CheckoutUiEvent.SelectProvider -> {
                _uiState.update { it.copy(selectedProvider = event.provider) }
            }
            is CheckoutUiEvent.StartCheckout -> {
                startCheckout()
            }
            is CheckoutUiEvent.HandlePlayPurchaseSuccess -> {
                verifyPlayPurchase(event)
            }
            is CheckoutUiEvent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun startCheckout() {
        val s = _uiState.value
        val plan = s.plan ?: return
        if (s.isCreatingOrder) return

        _uiState.update { it.copy(isCreatingOrder = true, errorMessage = null) }
        viewModelScope.launch {
            val providerStr = s.selectedProvider.id
            when (val res = createCheckoutOrderUseCase(plan.code, providerStr)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            createdOrder = res.data
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            errorMessage = res.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun verifyPlayPurchase(event: CheckoutUiEvent.HandlePlayPurchaseSuccess) {
        val s = _uiState.value
        val paymentRef = s.createdOrder?.paymentReference

        _uiState.update { it.copy(isCreatingOrder = true, errorMessage = null) }
        viewModelScope.launch {
            when (val res = verifyPlayBillingPurchaseUseCase(
                purchaseToken = event.purchaseToken,
                orderId = event.orderId,
                productId = event.productId,
                basePlanId = event.basePlanId,
                paymentReference = paymentRef
            )) {
                is AppResult.Success -> {
                    // Segarkan entitlement di background
                    getMyEntitlementsUseCase(forceRefresh = true)
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            isPaymentSuccess = true
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            errorMessage = "Verifikasi Google Play gagal: ${res.exception.message}"
                        )
                    }
                }
            }
        }
    }
}
