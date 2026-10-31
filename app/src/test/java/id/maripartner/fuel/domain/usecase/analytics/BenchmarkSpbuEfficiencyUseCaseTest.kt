package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class BenchmarkSpbuEfficiencyUseCaseTest {

    private val useCase = BenchmarkSpbuEfficiencyUseCase()

    @Test
    fun benchmark_withLessThanTwoTransactions_returnsEmptyRecommendation() {
        val result = useCase(emptyList(), baselineKmPerLiter = 14.0)
        assertTrue(result.items.isEmpty())
        assertTrue(result.recommendation.contains("minimal 2 transaksi", ignoreCase = true))
    }

    @Test
    fun benchmark_calculatesStationEfficiencyAndRanksCorrectly() {
        val now = Instant.now()
        val tx1 = FuelTransaction(
            id = "tx1",
            vehicleId = "v1",
            transactionAt = now.minusSeconds(7200),
            totalAmountIdr = 100_000L,
            volumeLiters = 10.0,
            odometerKm = 1000.0,
            stationName = "SPBU Pertamina Dago"
        )
        val tx2 = FuelTransaction(
            id = "tx2",
            vehicleId = "v1",
            transactionAt = now.minusSeconds(3600),
            totalAmountIdr = 100_000L,
            volumeLiters = 10.0,
            odometerKm = 1180.0, // Dari tx1 (Dago) ke tx2 menempuh 180 km dengan 10 L = 18.0 km/L
            stationName = "Shell Gatot Subroto"
        )
        val tx3 = FuelTransaction(
            id = "tx3",
            vehicleId = "v1",
            transactionAt = now,
            totalAmountIdr = 100_000L,
            volumeLiters = 10.0,
            odometerKm = 1320.0, // Dari tx2 (Shell) ke tx3 menempuh 140 km dengan 10 L = 14.0 km/L
            stationName = "SPBU Pertamina Dago"
        )

        val report = useCase(listOf(tx1, tx2, tx3))

        assertEquals(2, report.items.size)
        // SPBU Pertamina Dago harus ranking 1 (18.0 km/L)
        val best = report.bestStation
        assertNotNull(best)
        assertEquals("SPBU Pertamina Dago", best?.stationName)
        assertEquals("Pertamina", best?.brand)
        assertEquals(18.0, best?.avgKmPerLiter ?: 0.0, 0.1)
        assertEquals("A+", best?.ratingGrade)
    }
}
