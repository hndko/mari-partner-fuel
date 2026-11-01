package id.maripartner.fuel.domain.usecase.fuel

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.FuelBrandPrice
import id.maripartner.fuel.domain.model.IndonesianFuelMarketData
import id.maripartner.fuel.domain.repository.FuelPriceRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFuelPricesUseCaseTest {

    private class FakeFuelPriceRepository(
        private val mockPrices: List<FuelBrandPrice> = IndonesianFuelMarketData.currentPrices
    ) : FuelPriceRepository {
        override suspend fun getFuelPrices(): AppResult<List<FuelBrandPrice>> {
            return AppResult.Success(mockPrices)
        }

        override suspend fun updateFuelPrice(id: String, newPriceIdr: Long): AppResult<Unit> {
            return AppResult.Success(Unit)
        }
    }

    private val fakeRepo = FakeFuelPriceRepository()
    private val useCase = GetFuelPricesUseCase(fakeRepo)

    @Test
    fun getFuelPrices_returnsMarketPricesSuccessfully() = runBlocking {
        val result = useCase()
        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertTrue(data.isNotEmpty())
        assertTrue(data.any { it.brand == "Pertamina" && it.fuelName == "Pertamax" })
    }

    @Test
    fun findPriceForFuel_exactAndKeywordMatching() {
        val prices = IndonesianFuelMarketData.currentPrices

        // 1. Exact match Pertamax
        val pertamaxPrice = useCase.findPriceForFuel("Pertamax", prices)
        assertEquals(12_100L, pertamaxPrice)

        // 2. Keyword match lowercase pertalite
        val pertalitePrice = useCase.findPriceForFuel("isi bensin pertalite", prices)
        assertEquals(10_000L, pertalitePrice)

        // 3. Keyword match Pertamax Turbo
        val turboPrice = useCase.findPriceForFuel("Pertamax Turbo", prices)
        assertEquals(13_250L, turboPrice)

        // 4. Keyword match Shell Super
        val shellPrice = useCase.findPriceForFuel("Shell Super", prices)
        assertEquals(12_290L, shellPrice)

        // 5. Keyword match Bio Solar
        val solarPrice = useCase.findPriceForFuel("Bio Solar", prices)
        assertEquals(6_800L, solarPrice)

        // 6. Unknown fuel returns null
        val unknown = useCase.findPriceForFuel("Bensin Ajaib 999", prices)
        assertEquals(null, unknown)
    }
}
