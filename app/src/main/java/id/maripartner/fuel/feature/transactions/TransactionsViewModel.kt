package id.maripartner.fuel.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.TransactionFilter
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.transaction.GetTransactionsUseCase
import id.maripartner.fuel.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class TransactionsViewModel(
    private val transactionRepository: FuelTransactionRepository = FuelTransactionRepositoryImpl(),
    private val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(),
    private val getTransactionsUseCase: GetTransactionsUseCase = GetTransactionsUseCase(transactionRepository),
    private val getVehiclesUseCase: GetVehiclesUseCase = GetVehiclesUseCase(vehicleRepository)
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    private val _uiState = MutableStateFlow(TransactionsUiState(isLoading = true))
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        loadVehicles()
        loadTransactions(reset = true)
        observeSearchQuery()
    }

    fun onEvent(event: TransactionUiEvent) {
        when (event) {
            is TransactionUiEvent.QueryChanged -> {
                _uiState.update { it.copy(filter = it.filter.copy(query = event.query)) }
                searchQueryFlow.value = event.query
            }
            is TransactionUiEvent.VehicleFilterChanged -> {
                _uiState.update { it.copy(filter = it.filter.copy(vehicleId = event.vehicleId)) }
                loadTransactions(reset = true)
            }
            is TransactionUiEvent.FuelTypeFilterChanged -> {
                _uiState.update { it.copy(filter = it.filter.copy(fuelType = event.fuelType)) }
                loadTransactions(reset = true)
            }
            is TransactionUiEvent.DateRangeChanged -> {
                _uiState.update { it.copy(filter = it.filter.copy(startDate = event.startDate, endDate = event.endDate)) }
                loadTransactions(reset = true)
            }
            is TransactionUiEvent.SortOrderChanged -> {
                _uiState.update { it.copy(filter = it.filter.copy(sortOrder = event.sortOrder)) }
                loadTransactions(reset = true)
            }
            is TransactionUiEvent.LoadNextPage -> {
                loadNextPage()
            }
            is TransactionUiEvent.Refresh -> {
                loadVehicles()
                loadTransactions(reset = true)
            }
            is TransactionUiEvent.DismissMessage -> {
                _uiState.update { it.copy(actionMessage = null, errorMessage = null) }
            }
        }
    }

    private fun observeSearchQuery() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchQueryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect {
                    loadTransactions(reset = true)
                }
        }
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            getVehiclesUseCase(includeArchived = true).collect { result ->
                if (result is AppResult.Success) {
                    _uiState.update { it.copy(availableVehicles = result.data) }
                }
            }
        }
    }

    fun loadTransactions(reset: Boolean) {
        val current = _uiState.value
        val offset = if (reset) 0 else current.transactions.size

        if (reset) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        } else {
            if (current.isLoadingMore || !current.canLoadMore) return
            _uiState.update { it.copy(isLoadingMore = true) }
        }

        viewModelScope.launch {
            when (val result = getTransactionsUseCase(_uiState.value.filter, limit = PAGE_SIZE, offset = offset)) {
                is AppResult.Success -> {
                    val newItems = result.data
                    val merged = if (reset) newItems else current.transactions + newItems
                    _uiState.update {
                        it.copy(
                            transactions = merged,
                            isLoading = false,
                            isLoadingMore = false,
                            canLoadMore = newItems.size >= PAGE_SIZE,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun loadNextPage() {
        loadTransactions(reset = false)
    }
}
