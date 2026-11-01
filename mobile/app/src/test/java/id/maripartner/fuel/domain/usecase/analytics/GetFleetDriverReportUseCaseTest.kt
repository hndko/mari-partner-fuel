package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant

class GetFleetDriverReportUseCaseTest {

    private val useCase = GetFleetDriverReportUseCase()

    @Test
    fun extractDriverName_parsesDriverTagCorrectly() {
        val notes = "[Driver: Budi Santoso] Tugas pengantaran barang"
        val driver = useCase.extractDriverName(notes)
        assertEquals("Budi Santoso", driver)

        val format = useCase.formatDriverTag("Agus Setiawan", "Isi full tank")
        assertEquals("[Driver: Agus Setiawan] Isi full tank", format)
    }

    @Test
    fun getFleetReport_aggregatesDriverTransactionsAndExpenses() {
        val now = Instant.now()
        val txList = listOf(
            FuelTransaction(
                id = "tx1",
                vehicleId = "v1",
                transactionAt = now.minusSeconds(7200),
                totalAmountIdr = 100_000L,
                volumeLiters = 10.0,
                notes = "[Driver: Budi] Shift Pagi"
            ),
            FuelTransaction(
                id = "tx2",
                vehicleId = "v1",
                transactionAt = now.minusSeconds(3600),
                totalAmountIdr = 150_000L,
                volumeLiters = 15.0,
                notes = "[Driver: Andi] Shift Malam"
            ),
            FuelTransaction(
                id = "tx3",
                vehicleId = "v1",
                transactionAt = now,
                totalAmountIdr = 80_000L,
                volumeLiters = 8.0,
                notes = "[Driver: Budi] Shift Siang"
            )
        )

        val report = useCase(txList)
        assertEquals(2, report.totalDrivers)

        val budi = report.driverSummaries.find { it.driverName == "Budi" }
        assertNotNull(budi)
        assertEquals(2, budi?.totalTransactions)
        assertEquals(18.0, budi?.totalVolumeLiters ?: 0.0, 0.01)
        assertEquals(180_000L, budi?.totalExpenseIdr)
    }
}
