package id.maripartner.fuel.feature.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.usecase.subscription.GetMyEntitlementsUseCase
import id.maripartner.fuel.domain.usecase.subscription.GetSubscriptionPlansUseCase
import id.maripartner.fuel.domain.usecase.subscription.RequestDowngradeToFreeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val getMyEntitlementsUseCase: GetMyEntitlementsUseCase = GetMyEntitlementsUseCase(),
    private val getSubscriptionPlansUseCase: GetSubscriptionPlansUseCase = GetSubscriptionPlansUseCase(),
    private val requestDowngradeToFreeUseCase: RequestDowngradeToFreeUseCase = RequestDowngradeToFreeUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadData(forceRefresh = true)
    }

    fun onEvent(event: SubscriptionUiEvent) {
        when (event) {
            is SubscriptionUiEvent.LoadData -> loadData(forceRefresh = true)
            is SubscriptionUiEvent.Refresh -> loadData(forceRefresh = true)
            is SubscriptionUiEvent.SelectBillingPeriod -> {
                _uiState.update { it.copy(selectedBillingPeriod = event.period) }
            }
            is SubscriptionUiEvent.SelectPlan -> {
                _uiState.update {
                    it.copy(
                        showComingSoonDialog = true,
                        selectedPlanForCheckout = event.plan
                    )
                }
            }
            is SubscriptionUiEvent.RequestDowngrade -> {
                _uiState.update {
                    it.copy(
                        showDowngradeDialog = true,
                        targetPlan = event.freePlan
                    )
                }
            }
            is SubscriptionUiEvent.RequestRenewal -> {
                _uiState.update {
                    it.copy(
                        showRenewalDialog = true,
                        targetPlan = event.currentPlan
                    )
                }
            }
            is SubscriptionUiEvent.RequestSwitchCycle -> {
                _uiState.update {
                    it.copy(
                        showSwitchCycleDialog = true,
                        targetPlan = event.targetPlan
                    )
                }
            }
            is SubscriptionUiEvent.ConfirmDowngrade -> {
                executeDowngrade()
            }
            is SubscriptionUiEvent.DismissDowngradeDialog -> {
                _uiState.update { it.copy(showDowngradeDialog = false, targetPlan = null) }
            }
            is SubscriptionUiEvent.DismissRenewalDialog -> {
                _uiState.update { it.copy(showRenewalDialog = false, targetPlan = null) }
            }
            is SubscriptionUiEvent.DismissSwitchCycleDialog -> {
                _uiState.update { it.copy(showSwitchCycleDialog = false, targetPlan = null) }
            }
            is SubscriptionUiEvent.DismissSuccessMessage -> {
                _uiState.update { it.copy(actionSuccessMessage = null) }
            }
            is SubscriptionUiEvent.DismissComingSoon -> {
                _uiState.update {
                    it.copy(
                        showComingSoonDialog = false,
                        selectedPlanForCheckout = null
                    )
                }
            }
            is SubscriptionUiEvent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun executeDowngrade() {
        val s = _uiState.value
        if (s.isProcessingAction) return

        _uiState.update { it.copy(isProcessingAction = true, showDowngradeDialog = false) }
        viewModelScope.launch {
            when (val res = requestDowngradeToFreeUseCase()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessingAction = false,
                            actionSuccessMessage = res.data,
                            targetPlan = null
                        )
                    }
                    loadData(forceRefresh = true)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessingAction = false,
                            errorMessage = res.exception.message,
                            targetPlan = null
                        )
                    }
                }
            }
        }
    }

    private fun loadData(forceRefresh: Boolean) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val entitlementResult = getMyEntitlementsUseCase(forceRefresh)
            val plansResult = getSubscriptionPlansUseCase()

            val entitlement = (entitlementResult as? AppResult.Success)?.data
            val plans = (plansResult as? AppResult.Success)?.data ?: emptyList()

            val errorMsg = if (entitlementResult is AppResult.Error && plans.isEmpty()) {
                entitlementResult.exception.message
            } else null

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userEntitlement = entitlement ?: it.userEntitlement,
                    plans = if (plans.isNotEmpty()) plans else it.plans,
                    errorMessage = errorMsg
                )
            }
        }
    }
}
