package id.maripartner.fuel.domain.usecase.transaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EstimateFuelCalculationUseCaseTest {

    private val calculator = EstimateFuelCalculationUseCase()

    @Test
    fun calculatePricePerLiter_computesAccurately() {
        val price = calculator.calculatePricePerLiter(totalAmountIdr = 50000L, volumeLiters = 5.0)
        assertNotNull(price)
        assertEquals(10000.0, price!!, 0.001)
    }

    @Test
    fun calculatePricePerLiter_invalidInputs_returnsNull() {
        assertNull(calculator.calculatePricePerLiter(0L, 5.0))
        assertNull(calculator.calculatePricePerLiter(50000L, 0.0))
    }

    @Test
    fun calculateVolumeLiters_computesAccurately() {
        val volume = calculator.calculateVolumeLiters(totalAmountIdr = 50000L, pricePerLiterIdr = 10000.0)
        assertNotNull(volume)
        assertEquals(5.0, volume!!, 0.001)
    }

    @Test
    fun checkDiscrepancy_exactMatch_returnsNoDiscrepancy() {
        val result = calculator.checkDiscrepancy(
            totalAmountIdr = 50000L,
            volumeLiters = 5.0,
            pricePerLiterIdr = 10000.0
        )
        assertFalse(result.hasDiscrepancy)
        assertEquals(50000L, result.expectedTotalIdr)
        assertEquals(0L, result.differenceIdr)
    }

    @Test
    fun checkDiscrepancy_significantDifference_detectsDiscrepancy() {
        // volume 5L * Rp 10.000 = Rp 50.000, tapi pengguna memasukkan total Rp 70.000 (selisih Rp 20.000 > 5%)
        val result = calculator.checkDiscrepancy(
            totalAmountIdr = 70000L,
            volumeLiters = 5.0,
            pricePerLiterIdr = 10000.0
        )
        assertTrue(result.hasDiscrepancy)
        assertEquals(50000L, result.expectedTotalIdr)
        assertEquals(20000L, result.differenceIdr)
        assertNotNull(result.message)
    }

    @Test
    fun checkDiscrepancy_smallRoundingDifferenceWithinTolerance_returnsNoDiscrepancy() {
        // Total Rp 50.100 vs Rp 50.000 (selisih Rp 100 < 5% dan < 1000)
        val result = calculator.checkDiscrepancy(
            totalAmountIdr = 50100L,
            volumeLiters = 5.0,
            pricePerLiterIdr = 10000.0
        )
        assertFalse(result.hasDiscrepancy)
    }
}
