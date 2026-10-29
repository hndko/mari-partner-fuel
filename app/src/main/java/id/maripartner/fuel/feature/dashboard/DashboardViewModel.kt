package id.maripartner.fuel.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.usecase.dashboard.GetDashboardSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel untuk Dashboard Mari Partner Fuel.
 */
class DashboardViewModel(
    private val transactionRepository: FuelTransactionRepository = FuelTransactionRepositoryImpl(),
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase = GetDashboardSummaryUseCase(transactionRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.Refresh -> loadDashboard(isRefresh = true)
            is DashboardUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    fun loadDashboard(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(summaryState = UiState.Loading) }
            }

            when (val result = getDashboardSummaryUseCase(LocalDate.now())) {
                is AppResult.Success -> {
                    val summary = result.data
                    val state = if (summary.transactionCount == 0 && summary.previousMonthExpenseIdr == 0L) {
                        UiState.Empty("Belum ada pengeluaran BBM yang dicatat bulan ini.")
                    } else {
                        UiState.Success(summary)
                    }
                    _uiState.update {
                        it.copy(
                            summaryState = state,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            summaryState = UiState.Error(result.exception.message ?: "Gagal memuat ringkasan dashboard."),
                            isRefreshing = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }
}
