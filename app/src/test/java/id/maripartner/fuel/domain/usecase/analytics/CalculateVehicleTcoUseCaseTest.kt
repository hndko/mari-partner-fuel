package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

class CalculateVehicleTcoUseCaseTest {

    private lateinit var useCase: CalculateVehicleTcoUseCase

    @Before
    fun setUp() {
        useCase = CalculateVehicleTcoUseCase()
    }

    @Test
    fun `calculates fuel cost per km and maintenance cost per km correctly`() {
        val txs = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 400_000L, volumeLiters = 35.0, odometerKm = 10_000.0, transactionAt = Instant.now()),
            FuelTransaction(id = "2", vehicleId = "v1", totalAmountIdr = 400_000L, volumeLiters = 35.0, odometerKm = 11_000.0, transactionAt = Instant.now())
        ) // Total distance = 1000 km, total fuel cost = 800.000 (800 Rp/km)

        val maintenanceCost = 200_000L // 200 Rp/km

        val result = useCase(
            transactions = txs,
            totalMaintenanceCostIdr = maintenanceCost,
            driverRevenueRatePerKm = 3500L
        )

        assertEquals(800_000L, result.totalFuelCostIdr)
        assertEquals(200_000L, result.totalMaintenanceCostIdr)
        assertEquals(1_000_000L, result.totalOwnershipCostIdr)
        assertEquals(1000.0, result.totalDistanceKm, 0.01)
        assertEquals(800.0, result.fuelCostPerKm, 0.01)
        assertEquals(200.0, result.maintenanceCostPerKm, 0.01)
        assertEquals(1000.0, result.trueCostPerKm, 0.01) // 800 + 200 = 1000 Rp/km

        // Driver margin: 3500 - 1000 = 2500 Rp/km profit
        assertEquals(2500.0, result.driverNetMarginPerKm, 0.01)
        assertEquals(71.42, result.driverProfitMarginPercent, 0.5)
    }
}
