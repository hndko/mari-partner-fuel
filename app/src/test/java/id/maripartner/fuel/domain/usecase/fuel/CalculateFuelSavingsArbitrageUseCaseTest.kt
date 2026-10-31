package id.maripartner.fuel.domain.usecase.fuel

import id.maripartner.fuel.domain.model.FuelBrandPrice
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateFuelSavingsArbitrageUseCaseTest {

    private lateinit var useCase: CalculateFuelSavingsArbitrageUseCase

    @Before
    fun setUp() {
        useCase = CalculateFuelSavingsArbitrageUseCase()
    }

    @Test
    fun `calculates arbitrage savings per tank and monthly correctly`() {
        val pertamax = FuelBrandPrice("Pertamina", "Pertamax", "RON 92", 12_100L)
        val shellSuper = FuelBrandPrice("Shell", "Shell Super", "RON 92", 12_290L)

        // Diff = 190 Rp/L, Tank = 45 Liters
        val result = useCase(pertamax, shellSuper, 45.0)

        assertEquals(190L, result.priceDiffPerLiter)
        assertEquals(8_550L, result.savingsPerFullTankIdr) // 190 * 45
        assertEquals(34_200L, result.estimatedMonthlySavingsIdr) // 8550 * 4
        assertEquals("Pertamina", result.cheaperBrandName)
    }
}
