package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GenerateEcoScoreUseCaseTest {

    private lateinit var useCase: GenerateEcoScoreUseCase

    @Before
    fun setUp() {
        useCase = GenerateEcoScoreUseCase()
    }

    @Test
    fun `calculates eco score and co2 saved for efficient driving`() {
        // 500 km dengan 30 liter mobil -> efisiensi 16.6 km/L (benchmark mobil 12.0 km/L)
        val txs = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 300_000L, volumeLiters = 15.0, odometerKm = 10_000.0, transactionAt = Instant.now()),
            FuelTransaction(id = "2", vehicleId = "v1", totalAmountIdr = 300_000L, volumeLiters = 15.0, odometerKm = 10_500.0, transactionAt = Instant.now())
        )

        val report = useCase(
            driverName = "John Doe",
            vehicleName = "Avanza",
            vehicleType = "Mobil",
            transactions = txs
        )

        assertNotNull(report.certificateId)
        assertTrue(report.certificateId.startsWith("ECO-2026-MPF-"))
        assertEquals("John Doe", report.driverName)
        assertEquals(16.7, report.averageKmL, 0.1)
        assertTrue(report.ecoScore >= 90) // Sangat efisien
        assertEquals("Eco Master Legend", report.badgeTitle)
        assertTrue(report.co2SavedKg > 0) // Menghasilkan reduksi emisi karbon
    }
}
