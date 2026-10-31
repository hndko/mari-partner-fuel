package id.maripartner.fuel.feature.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.SubscriptionRepositoryImpl
import id.maripartner.fuel.data.repository.owner.OwnerRepositoryImpl
import id.maripartner.fuel.domain.repository.SubscriptionRepository
import id.maripartner.fuel.domain.repository.owner.OwnerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OwnerConsoleViewModel(
    private val repository: OwnerRepository,
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl()
) : ViewModel() {

    constructor() : this(OwnerRepositoryImpl(), SubscriptionRepositoryImpl())

    private val _uiState = MutableStateFlow(OwnerConsoleUiState())
    val uiState: StateFlow<OwnerConsoleUiState> = _uiState.asStateFlow()

    init {
        loadAllData(isRefresh = false)
    }

    fun onEvent(event: OwnerConsoleUiEvent) {
        when (event) {
            is OwnerConsoleUiEvent.SelectTab -> {
                _uiState.update { it.copy(currentTab = event.tab) }
            }
            is OwnerConsoleUiEvent.ChangeTimeframe -> {
                if (event.timeframe != _uiState.value.selectedTimeframe) {
                    _uiState.update { it.copy(selectedTimeframe = event.timeframe) }
                    loadMetrics(event.timeframe, isRefresh = false)
                }
            }
            is OwnerConsoleUiEvent.Refresh -> {
                loadAllData(isRefresh = true)
            }
            is OwnerConsoleUiEvent.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is OwnerConsoleUiEvent.OpenGrantProDialog -> {
                _uiState.update {
                    it.copy(
                        selectedUserForGrantPro = event.user,
                        grantDurationDays = 30,
                        grantNotes = "",
                        errorMessage = null
                    )
                }
            }
            is OwnerConsoleUiEvent.DismissGrantProDialog -> {
                _uiState.update {
                    it.copy(
                        selectedUserForGrantPro = null,
                        isSubmittingGrant = false
                    )
                }
            }
            is OwnerConsoleUiEvent.ChangeGrantDuration -> {
                _uiState.update { it.copy(grantDurationDays = event.days) }
            }
            is OwnerConsoleUiEvent.ChangeGrantNotes -> {
                _uiState.update { it.copy(grantNotes = event.notes) }
            }
            is OwnerConsoleUiEvent.SubmitGrantPro -> {
                submitGrantPro()
            }
            is OwnerConsoleUiEvent.OpenEditPlanDialog -> {
                val plan = event.plan
                _uiState.update {
                    it.copy(
                        selectedPlanForEdit = plan,
                        editPlanName = plan.name,
                        editPlanPriceIdr = plan.priceIdr.toString(),
                        editPlanAiQuota = plan.aiScanQuota.toString(),
                        editPlanMaxVehicles = plan.maxVehicles?.toString().orEmpty(),
                        editPlanIsActive = plan.isActive,
                        editPlanDescription = plan.description.orEmpty(),
                        errorMessage = null
                    )
                }
            }
            is OwnerConsoleUiEvent.DismissEditPlanDialog -> {
                _uiState.update {
                    it.copy(
                        selectedPlanForEdit = null,
                        isSubmittingPlanEdit = false
                    )
                }
            }
            is OwnerConsoleUiEvent.ChangeEditPlanName -> {
                _uiState.update { it.copy(editPlanName = event.name) }
            }
            is OwnerConsoleUiEvent.ChangeEditPlanPrice -> {
                _uiState.update { it.copy(editPlanPriceIdr = event.price.filter { c -> c.isDigit() }) }
            }
            is OwnerConsoleUiEvent.ChangeEditPlanAiQuota -> {
                _uiState.update { it.copy(editPlanAiQuota = event.quota.filter { c -> c.isDigit() }) }
            }
            is OwnerConsoleUiEvent.ChangeEditPlanMaxVehicles -> {
                _uiState.update { it.copy(editPlanMaxVehicles = event.vehicles.filter { c -> c.isDigit() }) }
            }
            is OwnerConsoleUiEvent.ChangeEditPlanIsActive -> {
                _uiState.update { it.copy(editPlanIsActive = event.active) }
            }
            is OwnerConsoleUiEvent.ChangeEditPlanDescription -> {
                _uiState.update { it.copy(editPlanDescription = event.desc) }
            }
            is OwnerConsoleUiEvent.SubmitEditPlan -> {
                submitEditPlan()
            }
            is OwnerConsoleUiEvent.DismissMessages -> {
                _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
    }

    private fun loadAllData(isRefresh: Boolean) {
        loadMetrics(_uiState.value.selectedTimeframe, isRefresh)
        loadPlans()
        loadTransactions()
    }

    private fun loadMetrics(timeframe: String, isRefresh: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = repository.getSaasMetrics(timeframe)) {
                is AppResult.Success -> {
                    subscriptionRepository.clearCache()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            metrics = result.data,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun loadPlans() {
        viewModelScope.launch {
            when (val result = repository.getSubscriptionPlans()) {
                is AppResult.Success -> {
                    subscriptionRepository.clearCache()
                    _uiState.update { it.copy(plans = result.data) }
                }
                is AppResult.Error -> { }
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            when (val result = repository.getPaymentTransactions(50)) {
                is AppResult.Success -> {
                    subscriptionRepository.clearCache()
                    _uiState.update { it.copy(transactions = result.data) }
                }
                is AppResult.Error -> { }
            }
        }
    }

    private fun submitGrantPro() {
        val user = _uiState.value.selectedUserForGrantPro ?: return
        val days = _uiState.value.grantDurationDays
        val notes = _uiState.value.grantNotes.trim().ifBlank { null }

        _uiState.update { it.copy(isSubmittingGrant = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = repository.grantUserPro(user.id, days, notes)) {
                is AppResult.Success -> {
                    subscriptionRepository.clearCache()
                    _uiState.update {
                        it.copy(
                            isSubmittingGrant = false,
                            selectedUserForGrantPro = null,
                            successMessage = result.data.message.ifBlank {
                                if (days == 0) {
                                    "Berhasil memberikan akses Pro Permanen (Seumur Hidup) kepada ${user.displayName}."
                                } else {
                                    "Berhasil memberikan akses Pro kepada ${user.displayName} selama $days hari."
                                }
                            }
                        )
                    }
                    loadMetrics(_uiState.value.selectedTimeframe, isRefresh = true)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingGrant = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun submitEditPlan() {
        val plan = _uiState.value.selectedPlanForEdit ?: return
        val name = _uiState.value.editPlanName.trim().ifBlank { plan.name }
        val price = _uiState.value.editPlanPriceIdr.toLongOrNull() ?: plan.priceIdr
        val aiQuota = _uiState.value.editPlanAiQuota.toIntOrNull() ?: plan.aiScanQuota
        val maxVehicles = _uiState.value.editPlanMaxVehicles.toIntOrNull()
        val isActive = _uiState.value.editPlanIsActive
        val description = _uiState.value.editPlanDescription.trim().ifBlank { null }

        _uiState.update { it.copy(isSubmittingPlanEdit = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = repository.updateSubscriptionPlan(
                planId = plan.id,
                name = name,
                priceIdr = price,
                aiScanQuota = aiQuota,
                maxVehicles = maxVehicles,
                isActive = isActive,
                description = description
            )) {
                is AppResult.Success -> {
                    subscriptionRepository.clearCache()
                    _uiState.update {
                        it.copy(
                            isSubmittingPlanEdit = false,
                            selectedPlanForEdit = null,
                            successMessage = result.data
                        )
                    }
                    loadPlans()
                    loadMetrics(_uiState.value.selectedTimeframe, isRefresh = true)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingPlanEdit = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }
}
