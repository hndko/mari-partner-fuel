package id.maripartner.fuel.domain.usecase.trip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateTripCostUseCaseTest {

    private val useCase = CalculateTripCostUseCase()

    @Test
    fun calculate_computesLitersAndCostAccurately() {
        // Jarak 150 km, Efisiensi 15 km/L -> 10 Liter
        // Harga Rp 10.000/L -> Rp 100.000
        // Penumpang 4 orang -> Rp 25.000 per orang
        val result = useCase.calculate(
            distanceKm = 150.0,
            fuelEfficiencyKmPerLiter = 15.0,
            fuelPricePerLiterIdr = 10_000L,
            passengerCount = 4,
            vehicleName = "Toyota Avanza",
            fuelTypeName = "Pertalite"
        )

        assertEquals(10.0, result.requiredLiters, 0.001)
        assertEquals(100_000L, result.totalFuelCostIdr)
        assertEquals(25_000L, result.costPerPassengerIdr)
        assertEquals(4, result.passengerCount)
        assertEquals(23.1, result.estimatedCo2EmissionKg, 0.01)
    }

    @Test
    fun calculate_handlesSinglePassengerAndOddDivision() {
        // Jarak 100 km, Efisiensi 12 km/L -> 8.3333 Liter
        // Harga Rp 12.100/L -> ~Rp 100.833
        // Penumpang 3 orang -> pembulatan ke atas (ceil)
        val result = useCase.calculate(
            distanceKm = 100.0,
            fuelEfficiencyKmPerLiter = 12.0,
            fuelPricePerLiterIdr = 12_100L,
            passengerCount = 3
        )

        assertTrue("Total biaya harus lebih dari 0", result.totalFuelCostIdr > 0L)
        assertTrue("Biaya patungan harus >= total / 3", result.costPerPassengerIdr >= result.totalFuelCostIdr / 3)
    }

    @Test
    fun generateShareableSummary_containsEssentialKeywords() {
        val result = useCase.calculate(
            distanceKm = 200.0,
            fuelEfficiencyKmPerLiter = 10.0,
            fuelPricePerLiterIdr = 10_000L,
            passengerCount = 2,
            vehicleName = "Honda HR-V",
            fuelTypeName = "Pertalite"
        )

        val summary = useCase.generateShareableSummary(result)

        assertTrue(summary.contains("Honda HR-V"))
        assertTrue(summary.contains("200 km"))
        assertTrue(summary.contains("20,00 Liter"))
        assertTrue(summary.contains("2 Penumpang"))
        assertTrue(summary.contains("Biaya Patungan"))
        assertTrue(summary.contains("Mari Partner Fuel"))
    }
}
