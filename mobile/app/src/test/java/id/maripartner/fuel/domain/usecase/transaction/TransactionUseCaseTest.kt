package id.maripartner.fuel.domain.usecase.transaction

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.TransactionFilter
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeFuelTransactionRepository : FuelTransactionRepository {
    val transactions = mutableListOf<FuelTransaction>()

    override suspend fun getTransactions(
        filter: TransactionFilter,
        limit: Int,
        offset: Int
    ): AppResult<List<FuelTransaction>> {
        var filtered = transactions.toList()
        if (filter.vehicleId != null) {
            filtered = filtered.filter { it.vehicleId == filter.vehicleId }
        }
        if (filter.fuelType != null) {
            filtered = filtered.filter { it.fuelType.equals(filter.fuelType, ignoreCase = true) }
        }
        if (filter.query.isNotBlank()) {
            filtered = filtered.filter { it.stationName?.contains(filter.query, ignoreCase = true) == true }
        }
        val paginated = filtered.drop(offset).take(limit)
        return AppResult.Success(paginated)
    }

    override suspend fun getTransactionById(id: String): AppResult<FuelTransaction> {
        val tx = transactions.firstOrNull { it.id == id }
        return if (tx != null) AppResult.Success(tx) else AppResult.Error(AppException.NotFound())
    }

    override suspend fun createTransaction(
        transaction: FuelTransaction,
        receiptBytes: ByteArray?,
        receiptMimeType: String?
    ): AppResult<FuelTransaction> {
        val created = transaction.copy(
            id = "tx-${transactions.size + 1}",
            receiptFileId = if (receiptBytes != null) "rec-${transactions.size + 1}" else null
        )
        transactions.add(created)
        return AppResult.Success(created)
    }

    override suspend fun updateTransaction(
        transaction: FuelTransaction,
        newReceiptBytes: ByteArray?,
        newReceiptMimeType: String?,
        removeReceipt: Boolean
    ): AppResult<FuelTransaction> {
        val idx = transactions.indexOfFirst { it.id == transaction.id }
        if (idx < 0) return AppResult.Error(AppException.NotFound())

        val updated = transaction.copy(
            receiptFileId = when {
                removeReceipt -> null
                newReceiptBytes != null -> "rec-new-${idx + 1}"
                else -> transaction.receiptFileId
            }
        )
        transactions[idx] = updated
        return AppResult.Success(updated)
    }

    override suspend fun deleteTransaction(id: String): AppResult<Unit> {
        val removed = transactions.removeIf { it.id == id }
        return if (removed) AppResult.Success(Unit) else AppResult.Error(AppException.NotFound())
    }

    override suspend fun getReceiptSignedUrl(storagePath: String): AppResult<String> {
        return AppResult.Success("https://storage.supabase.co/signed/$storagePath")
    }
}

class TransactionUseCaseTest {

    private val fakeRepo = FakeFuelTransactionRepository()
    private val getTransactionsUseCase = GetTransactionsUseCase(fakeRepo)
    private val getTransactionByIdUseCase = GetTransactionByIdUseCase(fakeRepo)
    private val saveTransactionUseCase = SaveTransactionUseCase(fakeRepo)
    private val deleteTransactionUseCase = DeleteTransactionUseCase(fakeRepo)

    @Test
    fun saveTransaction_createsAndAttachesReceiptId() = runTest {
        val tx = FuelTransaction(
            vehicleId = "veh-1",
            totalAmountIdr = 50000L,
            volumeLiters = 5.0,
            stationName = "SPBU Pertamina"
        )
        val dummyBytes = "dummy image bytes".toByteArray()

        val result = saveTransactionUseCase(tx, receiptBytes = dummyBytes, receiptMimeType = "image/jpeg")
        assertTrue(result.isSuccess)
        val created = (result as AppResult.Success).data
        assertEquals("tx-1", created.id)
        assertEquals("rec-1", created.receiptFileId)
        assertTrue(created.hasReceipt)
    }

    @Test
    fun saveTransaction_invalidTotal_failsWithoutSaving() = runTest {
        val invalidTx = FuelTransaction(vehicleId = "veh-1", totalAmountIdr = 0L)
        val result = saveTransactionUseCase(invalidTx)
        assertFalse(result.isSuccess)
        assertEquals(0, fakeRepo.transactions.size)
    }

    @Test
    fun getTransactions_appliesFiltersAndPagination() = runTest {
        fakeRepo.transactions.addAll(
            listOf(
                FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 20000L, fuelType = "Pertalite", stationName = "SPBU Cilandak"),
                FuelTransaction(id = "2", vehicleId = "v1", totalAmountIdr = 30000L, fuelType = "Pertamax", stationName = "SPBU Fatmawati"),
                FuelTransaction(id = "3", vehicleId = "v2", totalAmountIdr = 40000L, fuelType = "Pertalite", stationName = "SPBU Lebak Bulus")
            )
        )

        // Filter by vehicleId = v1
        val v1Result = getTransactionsUseCase(TransactionFilter(vehicleId = "v1"))
        assertEquals(2, (v1Result as AppResult.Success).data.size)

        // Filter by query = "Fatmawati"
        val queryResult = getTransactionsUseCase(TransactionFilter(query = "Fatmawati"))
        assertEquals(1, (queryResult as AppResult.Success).data.size)
        assertEquals("2", (queryResult as AppResult.Success).data.first().id)

        // Pagination limit 2
        val pageResult = getTransactionsUseCase(TransactionFilter(), limit = 2, offset = 0)
        assertEquals(2, (pageResult as AppResult.Success).data.size)
    }

    @Test
    fun deleteTransaction_removesTransaction() = runTest {
        fakeRepo.transactions.add(FuelTransaction(id = "tx-99", vehicleId = "v1", totalAmountIdr = 50000L))

        val result = deleteTransactionUseCase("tx-99")
        assertTrue(result.isSuccess)
        assertEquals(0, fakeRepo.transactions.size)
    }
}
