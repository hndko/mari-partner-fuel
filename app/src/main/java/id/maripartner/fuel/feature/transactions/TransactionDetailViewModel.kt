package id.maripartner.fuel.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.usecase.transaction.DeleteTransactionUseCase
import id.maripartner.fuel.domain.usecase.transaction.GetTransactionByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val transactionRepository: FuelTransactionRepository = FuelTransactionRepositoryImpl(),
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase = GetTransactionByIdUseCase(transactionRepository),
    private val deleteTransactionUseCase: DeleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState(isLoading = true))
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    fun loadTransaction(id: String) {
        if (id.isBlank()) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = getTransactionByIdUseCase(id)) {
                is AppResult.Success -> {
                    val tx = result.data
                    _uiState.update {
                        it.copy(
                            transaction = tx,
                            signedReceiptUrl = tx.signedReceiptUrl,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun deleteTransaction() {
        val tx = _uiState.value.transaction ?: return
        if (_uiState.value.isDeleting) return

        _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = deleteTransactionUseCase(tx.id)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false, isDeleted = true) }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }
}
