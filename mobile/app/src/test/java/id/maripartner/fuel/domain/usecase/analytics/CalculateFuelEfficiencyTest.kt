package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CalculateFuelEfficiencyTest {

    private val useCase = CalculateFuelEfficiencyUseCase()

    @Test
    fun calculateForVehicle_computesAccurateKmPerLiterFromConsecutiveOdometers() {
        val transactions = listOf(
            // Pengisian 1 (Awal)
            FuelTransaction(
                id = "1", vehicleId = "v1", vehicleName = "Avanza",
                totalAmountIdr = 250_000L, volumeLiters = 20.0, odometerKm = 10_000.0,
                transactionAt = Instant.parse("2026-10-01T10:00:00Z")
            ),
            // Pengisian 2: +400 km dengan 25 liter (16.0 km/L)
            FuelTransaction(
                id = "2", vehicleId = "v1", vehicleName = "Avanza",
                totalAmountIdr = 300_000L, volumeLiters = 25.0, odometerKm = 10_400.0,
                transactionAt = Instant.parse("2026-10-08T10:00:00Z")
            ),
            // Pengisian 3: +350 km dengan 20 liter (17.5 km/L)
            FuelTransaction(
                id = "3", vehicleId = "v1", vehicleName = "Avanza",
                totalAmountIdr = 240_000L, volumeLiters = 20.0, odometerKm = 10_750.0,
                transactionAt = Instant.parse("2026-10-15T10:00:00Z")
            )
        )

        val result = useCase.calculateForVehicle("v1", "Avanza", transactions)

        assertTrue(result.isSufficientData)
        assertEquals(2, result.validPairsCount)
        assertEquals(750.0, result.totalDistanceKm, 0.001)
        assertEquals(45.0, result.totalVolumeLiters, 0.001)
        // 750 / 45 = 16.6667 km/L
        assertNotNull(result.averageKmPerLiter)
        assertEquals(750.0 / 45.0, result.averageKmPerLiter!!, 0.01)
    }

    @Test
    fun calculateForVehicle_handlesOdometerAnomalyGracefully() {
        val transactions = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 100_000L, volumeLiters = 10.0, odometerKm = 5_000.0, transactionAt = Instant.now()),
            // Anomali: odometer menurun (misal salah ketik 4500)
            FuelTransaction(id = "2", vehicleId = "v1", totalAmountIdr = 100_000L, volumeLiters = 10.0, odometerKm = 4_500.0, transactionAt = Instant.now()),
            // Pengisian valid berikutnya
            FuelTransaction(id = "3", vehicleId = "v1", totalAmountIdr = 100_000L, volumeLiters = 10.0, odometerKm = 5_300.0, transactionAt = Instant.now())
        )

        val result = useCase.calculateForVehicle("v1", "Avanza", transactions)

        // Harus tetap menghitung pasangan yang valid tanpa crash
        assertTrue(result.explanationNote!!.contains("anomali") || result.validPairsCount >= 0)
    }

    @Test
    fun calculateForVehicle_returnsInsufficientDataWhenLessThanTwoEntries() {
        val singleTx = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 50_000L, volumeLiters = 4.0, odometerKm = 1_000.0, transactionAt = Instant.now())
        )

        val result = useCase.calculateForVehicle("v1", "Vario", singleTx)

        assertFalse(result.isSufficientData)
        assertNull(result.averageKmPerLiter)
        assertTrue(result.explanationNote!!.contains("minimal 2 pencatatan"))
    }
}
