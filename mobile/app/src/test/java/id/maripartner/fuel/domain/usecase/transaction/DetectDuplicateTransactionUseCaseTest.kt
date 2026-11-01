package id.maripartner.fuel.domain.usecase.transaction

import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DetectDuplicateTransactionUseCaseTest {

    private val useCase = DetectDuplicateTransactionUseCase()

    @Test
    fun detect_returnsTrueWhenSimilarTransactionFoundWithinTimeWindow() {
        val now = Instant.now()
        val existing = listOf(
            FuelTransaction(
                id = "tx1",
                vehicleId = "v1",
                transactionAt = now.minusSeconds(1800), // 30 menit lalu
                totalAmountIdr = 50_000L,
                volumeLiters = 5.0,
                stationName = "SPBU Pertamina Pasteur"
            )
        )

        val candidate = FuelTransaction(
            id = "", // transaksi baru
            vehicleId = "v1",
            transactionAt = now,
            totalAmountIdr = 50_000L,
            volumeLiters = 5.0,
            stationName = "SPBU Pertamina Pasteur"
        )

        val result = useCase(candidate, existing)
        assertTrue(result.isDuplicate)
        assertTrue(result.message?.contains("Rp 50000") == true)
    }

    @Test
    fun detect_returnsFalseWhenDifferentVehicleOrDifferentAmount() {
        val now = Instant.now()
        val existing = listOf(
            FuelTransaction(
                id = "tx1",
                vehicleId = "v1",
                transactionAt = now.minusSeconds(1800),
                totalAmountIdr = 50_000L,
                volumeLiters = 5.0,
                stationName = "SPBU Pertamina Pasteur"
            )
        )

        val candidateDifferentAmount = FuelTransaction(
            id = "",
            vehicleId = "v1",
            transactionAt = now,
            totalAmountIdr = 150_000L,
            volumeLiters = 15.0,
            stationName = "SPBU Pertamina Pasteur"
        )

        val result = useCase(candidateDifferentAmount, existing)
        assertFalse(result.isDuplicate)
    }
}
