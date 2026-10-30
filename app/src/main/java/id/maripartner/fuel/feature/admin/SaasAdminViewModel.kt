package id.maripartner.fuel.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.SaasAdminRepositoryImpl
import id.maripartner.fuel.domain.repository.SaasAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SaasAdminViewModel(
    private val repository: SaasAdminRepository
) : ViewModel() {

    constructor() : this(SaasAdminRepositoryImpl())

    private val _uiState = MutableStateFlow(SaasAdminUiState())
    val uiState: StateFlow<SaasAdminUiState> = _uiState.asStateFlow()

    init {
        loadMetrics(_uiState.value.selectedTimeframe, isRefresh = false)
    }

    fun onEvent(event: SaasAdminUiEvent) {
        when (event) {
            is SaasAdminUiEvent.ChangeTimeframe -> {
                if (event.timeframe != _uiState.value.selectedTimeframe) {
                    _uiState.update { it.copy(selectedTimeframe = event.timeframe) }
                    loadMetrics(event.timeframe, isRefresh = false)
                }
            }
            is SaasAdminUiEvent.Refresh -> {
                loadMetrics(_uiState.value.selectedTimeframe, isRefresh = true)
            }
            is SaasAdminUiEvent.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is SaasAdminUiEvent.OpenGrantProDialog -> {
                _uiState.update {
                    it.copy(
                        selectedUserForGrantPro = event.user,
                        grantDurationDays = 30,
                        grantNotes = "",
                        errorMessage = null
                    )
                }
            }
            is SaasAdminUiEvent.DismissGrantProDialog -> {
                _uiState.update {
                    it.copy(
                        selectedUserForGrantPro = null,
                        isGrantingPro = false
                    )
                }
            }
            is SaasAdminUiEvent.ChangeGrantDuration -> {
                _uiState.update { it.copy(grantDurationDays = event.days) }
            }
            is SaasAdminUiEvent.ChangeGrantNotes -> {
                _uiState.update { it.copy(grantNotes = event.notes) }
            }
            is SaasAdminUiEvent.SubmitGrantPro -> {
                submitGrantPro()
            }
            is SaasAdminUiEvent.DismissMessages -> {
                _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
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

    private fun submitGrantPro() {
        val user = _uiState.value.selectedUserForGrantPro ?: return
        val days = _uiState.value.grantDurationDays
        val notes = _uiState.value.grantNotes.trim().ifBlank { null }

        _uiState.update { it.copy(isGrantingPro = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = repository.grantUserPro(user.id, days, notes)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isGrantingPro = false,
                            selectedUserForGrantPro = null,
                            successMessage = result.data.message.ifBlank {
                                "Berhasil memberikan akses Pro kepada ${user.displayName} selama $days hari."
                            }
                        )
                    }
                    // Muat ulang metrik otomatis setelah pemberian status Pro
                    loadMetrics(_uiState.value.selectedTimeframe, isRefresh = true)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isGrantingPro = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }
}
