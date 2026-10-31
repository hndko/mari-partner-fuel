package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.AnomalySeverity
import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DetectFuelConsumptionAnomalyUseCaseTest {

    private val useCase = DetectFuelConsumptionAnomalyUseCase()

    @Test
    fun detectAnomaly_insufficientData_returnsNormal() {
        // Kurang dari 3 data transaksi
        val transactions = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 50000, volumeLiters = 5.0, odometerKm = 1000.0, transactionAt = Instant.now()),
            FuelTransaction(id = "2", vehicleId = "v1", totalAmountIdr = 50000, volumeLiters = 5.0, odometerKm = 1200.0, transactionAt = Instant.now())
        )

        val result = useCase.detectAnomaly("v1", transactions)
        assertFalse(result.hasAnomaly)
        assertEquals(AnomalySeverity.NORMAL, result.severity)
    }

    @Test
    fun detectAnomaly_severeDrop_detectsCriticalAnomaly() {
        // Baseline:
        // tx1 -> tx2: 200 km / 10 L = 20 km/L
        // tx2 -> tx3: 200 km / 10 L = 20 km/L (Baseline = 20 km/L)
        // Latest:
        // tx3 -> tx4: 100 km / 10 L = 10 km/L (Drop 50%!)
        val transactions = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1000.0, transactionAt = Instant.parse("2026-10-01T10:00:00Z")),
            FuelTransaction(id = "2", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1200.0, transactionAt = Instant.parse("2026-10-05T10:00:00Z")),
            FuelTransaction(id = "3", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1400.0, transactionAt = Instant.parse("2026-10-10T10:00:00Z")),
            FuelTransaction(id = "4", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1500.0, transactionAt = Instant.parse("2026-10-15T10:00:00Z"))
        )

        val result = useCase.detectAnomaly("v1", transactions)

        assertTrue(result.hasAnomaly)
        assertEquals(AnomalySeverity.CRITICAL, result.severity)
        assertTrue(result.percentageChange <= -35.0)
        assertTrue("Harus memuat rekomendasi teknis kebocoran", result.technicalRecommendations.any { it.contains("rembesan", ignoreCase = true) || it.contains("bensin", ignoreCase = true) })
    }

    @Test
    fun detectAnomaly_moderateDrop_detectsWarningAnomaly() {
        // Baseline: 20 km/L
        // Latest: 15 km/L (Drop 25%)
        val transactions = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1000.0, transactionAt = Instant.parse("2026-10-01T10:00:00Z")),
            FuelTransaction(id = "2", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1200.0, transactionAt = Instant.parse("2026-10-05T10:00:00Z")),
            FuelTransaction(id = "3", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1400.0, transactionAt = Instant.parse("2026-10-10T10:00:00Z")),
            FuelTransaction(id = "4", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1550.0, transactionAt = Instant.parse("2026-10-15T10:00:00Z"))
        )

        val result = useCase.detectAnomaly("v1", transactions)

        assertTrue(result.hasAnomaly)
        assertEquals(AnomalySeverity.WARNING, result.severity)
        assertTrue(result.technicalRecommendations.any { it.contains("ban", ignoreCase = true) })
    }

    @Test
    fun detectAnomaly_significantGain_detectsEfficientDriving() {
        // Baseline: 10 km/L
        // Latest: 13 km/L (+30%)
        val transactions = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1000.0, transactionAt = Instant.parse("2026-10-01T10:00:00Z")),
            FuelTransaction(id = "2", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1100.0, transactionAt = Instant.parse("2026-10-05T10:00:00Z")),
            FuelTransaction(id = "3", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1200.0, transactionAt = Instant.parse("2026-10-10T10:00:00Z")),
            FuelTransaction(id = "4", vehicleId = "v1", totalAmountIdr = 100000, volumeLiters = 10.0, odometerKm = 1330.0, transactionAt = Instant.parse("2026-10-15T10:00:00Z"))
        )

        val result = useCase.detectAnomaly("v1", transactions)

        assertTrue(result.hasAnomaly)
        assertEquals(AnomalySeverity.EFFICIENT, result.severity)
        assertTrue(result.percentageChange >= 20.0)
    }
}
