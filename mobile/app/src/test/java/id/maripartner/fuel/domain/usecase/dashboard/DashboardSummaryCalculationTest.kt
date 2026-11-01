package id.maripartner.fuel.domain.usecase.dashboard

import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DashboardSummaryCalculationTest {

    private val dummyRepo = object : FuelTransactionRepository {
        override suspend fun getTransactions(filter: id.maripartner.fuel.domain.model.TransactionFilter, limit: Int, offset: Int) =
            id.maripartner.fuel.core.common.result.AppResult.Success(emptyList<FuelTransaction>())
        override suspend fun getTransactionById(id: String) = throw UnsupportedOperationException()
        override suspend fun createTransaction(transaction: FuelTransaction, receiptBytes: ByteArray?, receiptMimeType: String?) = throw UnsupportedOperationException()
        override suspend fun updateTransaction(transaction: FuelTransaction, newReceiptBytes: ByteArray?, newReceiptMimeType: String?, removeReceipt: Boolean) = throw UnsupportedOperationException()
        override suspend fun deleteTransaction(id: String) = throw UnsupportedOperationException()
        override suspend fun getReceiptSignedUrl(storagePath: String) = throw UnsupportedOperationException()
    }

    private val useCase = GetDashboardSummaryUseCase(dummyRepo)

    @Test
    fun computeSummary_calculatesTotalExpenseAndAveragesCorrectly() {
        val currentTx = listOf(
            FuelTransaction(
                id = "1", vehicleId = "v1", vehicleName = "Avanza",
                totalAmountIdr = 300_000L, volumeLiters = 25.0, transactionAt = Instant.now()
            ),
            FuelTransaction(
                id = "2", vehicleId = "v1", vehicleName = "Avanza",
                totalAmountIdr = 200_000L, volumeLiters = 15.0, transactionAt = Instant.now()
            )
        )
        val prevTx = listOf(
            FuelTransaction(
                id = "p1", vehicleId = "v1", vehicleName = "Avanza",
                totalAmountIdr = 400_000L, volumeLiters = 30.0, transactionAt = Instant.now()
            )
        )

        val summary = useCase.computeSummary(
            currentTransactions = currentTx,
            previousTransactions = prevTx,
            recentTransactions = currentTx,
            referenceDate = LocalDate.of(2026, 10, 29)
        )

        assertEquals(500_000L, summary.totalExpenseIdr)
        assertEquals(40.0, summary.totalVolumeLiters, 0.001)
        assertEquals(2, summary.transactionCount)
        assertEquals(250_000L, summary.averageExpensePerTransactionIdr)
        // Rata-rata tertimbang: 500.000 / 40.0 = 12.500
        assertEquals(12_500.0, summary.averagePricePerLiterIdr!!, 0.001)
        assertEquals(400_000L, summary.previousMonthExpenseIdr)
        assertEquals(100_000L, summary.expenseDifferenceIdr)
        // Kenaikan: (100.000 / 400.000) * 100 = 25.0%
        assertEquals(25.0, summary.expenseChangePercentage!!, 0.001)
        assertTrue(summary.hasComparisonData)
        assertFalse(summary.hasIncompleteVolumeData)
    }

    @Test
    fun computeSummary_handlesIncompleteVolumeWithoutTreatingAsZero() {
        val currentTx = listOf(
            FuelTransaction(
                id = "1", vehicleId = "v1", vehicleName = "Vario",
                totalAmountIdr = 50_000L, volumeLiters = 3.5, transactionAt = Instant.now()
            ),
            FuelTransaction(
                id = "2", vehicleId = "v1", vehicleName = "Vario",
                totalAmountIdr = 30_000L, volumeLiters = null, transactionAt = Instant.now() // input cepat tanpa volume
            )
        )

        val summary = useCase.computeSummary(
            currentTransactions = currentTx,
            previousTransactions = emptyList(),
            recentTransactions = currentTx
        )

        assertEquals(80_000L, summary.totalExpenseIdr)
        assertEquals(3.5, summary.totalVolumeLiters, 0.001)
        assertTrue(summary.hasIncompleteVolumeData)
        // Rata-rata tertimbang harga per liter hanya dari transaksi dengan volume: 50.000 / 3.5 = ~14.285,7
        assertEquals(50_000.0 / 3.5, summary.averagePricePerLiterIdr!!, 0.01)
    }

    @Test
    fun computeSummary_handlesZeroPreviousMonthWithoutMisleadingPercentage() {
        val currentTx = listOf(
            FuelTransaction(
                id = "1", vehicleId = "v1", vehicleName = "Avanza",
                totalAmountIdr = 150_000L, volumeLiters = 12.0, transactionAt = Instant.now()
            )
        )

        val summary = useCase.computeSummary(
            currentTransactions = currentTx,
            previousTransactions = emptyList(), // Bulan lalu 0
            recentTransactions = currentTx
        )

        assertEquals(150_000L, summary.totalExpenseIdr)
        assertEquals(0L, summary.previousMonthExpenseIdr)
        // Persentase tidak boleh Infinity atau menyesatkan
        assertNull(summary.expenseChangePercentage)
        assertFalse(summary.hasComparisonData)
    }

    @Test
    fun computeSummary_identifiesTopSpendingVehicleAndDistribution() {
        val currentTx = listOf(
            FuelTransaction(id = "1", vehicleId = "v-motor", vehicleName = "Vario", totalAmountIdr = 100_000L, volumeLiters = 8.0, transactionAt = Instant.now()),
            FuelTransaction(id = "2", vehicleId = "v-mobil", vehicleName = "Innova", totalAmountIdr = 400_000L, volumeLiters = 30.0, transactionAt = Instant.now())
        )

        val summary = useCase.computeSummary(
            currentTransactions = currentTx,
            previousTransactions = emptyList(),
            recentTransactions = currentTx
        )

        assertNotNull(summary.topSpendingVehicle)
        assertEquals("v-mobil", summary.topSpendingVehicle?.vehicleId)
        assertEquals("Innova", summary.topSpendingVehicle?.vehicleName)
        assertEquals(400_000L, summary.topSpendingVehicle?.totalAmountIdr)
        assertEquals(80.0, summary.topSpendingVehicle?.percentageOfTotal!!, 0.001)

        assertEquals(2, summary.vehicleSummaries.size)
    }
}
