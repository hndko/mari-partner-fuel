package id.maripartner.fuel.domain.usecase.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulateFuelPriceShockUseCaseTest {

    private val useCase = SimulateFuelPriceShockUseCase()

    @Test
    fun simulate_calculatesMonthlyAndYearlyFinancialImpactCorrectly() {
        // 100 Liter per bulan, harga bensin naik Rp 1.000/liter
        val result = useCase(
            monthlyLiters = 100.0,
            currentMonthlyCostIdr = 1_000_000L,
            priceHikePerLiter = 1000.0,
            avgCostPerKmIdr = 750.0
        )

        assertEquals(100_000L, result.monthlyImpactIdr)
        assertEquals(1_100_000L, result.projectedMonthlyCostIdr)
        assertEquals(1_200_000L, result.yearlyImpactIdr)
        assertEquals(10.0, result.percentIncrease, 0.01)
        assertTrue(result.recommendedKmReduction > 0.0)
        assertTrue(result.actionableTips.isNotEmpty())
    }
}
