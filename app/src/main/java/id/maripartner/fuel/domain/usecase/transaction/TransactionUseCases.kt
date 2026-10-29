package id.maripartner.fuel.domain.usecase.transaction

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.TransactionFilter
import id.maripartner.fuel.domain.repository.FuelTransactionRepository

class GetTransactionsUseCase(
    private val repository: FuelTransactionRepository
) {
    suspend operator fun invoke(
        filter: TransactionFilter,
        limit: Int = 20,
        offset: Int = 0
    ): AppResult<List<FuelTransaction>> {
        return repository.getTransactions(filter, limit, offset)
    }
}

class GetTransactionByIdUseCase(
    private val repository: FuelTransactionRepository
) {
    suspend operator fun invoke(id: String): AppResult<FuelTransaction> {
        return repository.getTransactionById(id)
    }
}

class SaveTransactionUseCase(
    private val repository: FuelTransactionRepository,
    private val validator: ValidateTransactionInputUseCase = ValidateTransactionInputUseCase()
) {
    suspend operator fun invoke(
        transaction: FuelTransaction,
        receiptBytes: ByteArray? = null,
        receiptMimeType: String? = null,
        removeReceipt: Boolean = false
    ): AppResult<FuelTransaction> {
        val validation = validator(transaction)
        if (!validation.isSuccess) {
            return AppResult.Error((validation as AppResult.Error).exception)
        }

        return if (transaction.id.isBlank()) {
            repository.createTransaction(transaction, receiptBytes, receiptMimeType)
        } else {
            repository.updateTransaction(transaction, receiptBytes, receiptMimeType, removeReceipt)
        }
    }
}

class DeleteTransactionUseCase(
    private val repository: FuelTransactionRepository
) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        return repository.deleteTransaction(id)
    }
}
