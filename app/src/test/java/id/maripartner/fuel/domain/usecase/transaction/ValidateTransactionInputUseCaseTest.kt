package id.maripartner.fuel.domain.usecase.transaction

import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateTransactionInputUseCaseTest {

    private val validator = ValidateTransactionInputUseCase()

    @Test
    fun validTransaction_returnsSuccess() {
        val tx = FuelTransaction(
            vehicleId = "veh-123",
            totalAmountIdr = 50000L,
            volumeLiters = 5.0,
            pricePerLiterIdr = 10000.0,
            odometerKm = 15200.0
        )
        val result = validator(tx)
        assertTrue(result.isSuccess)
    }

    @Test
    fun blankVehicleId_returnsError() {
        val tx = FuelTransaction(
            vehicleId = "",
            totalAmountIdr = 50000L
        )
        val result = validator(tx)
        assertFalse(result.isSuccess)
    }

    @Test
    fun zeroOrNegativeTotalAmount_returnsError() {
        val txZero = FuelTransaction(vehicleId = "veh-123", totalAmountIdr = 0L)
        val txNegative = FuelTransaction(vehicleId = "veh-123", totalAmountIdr = -5000L)

        assertFalse(validator(txZero).isSuccess)
        assertFalse(validator(txNegative).isSuccess)
    }

    @Test
    fun negativeOrZeroVolume_returnsError() {
        val txZeroVolume = FuelTransaction(vehicleId = "veh-123", totalAmountIdr = 50000L, volumeLiters = 0.0)
        val txNegVolume = FuelTransaction(vehicleId = "veh-123", totalAmountIdr = 50000L, volumeLiters = -2.5)

        assertFalse(validator(txZeroVolume).isSuccess)
        assertFalse(validator(txNegVolume).isSuccess)
    }

    @Test
    fun nullVolume_isAllowed_returnsSuccess() {
        val tx = FuelTransaction(
            vehicleId = "veh-123",
            totalAmountIdr = 50000L,
            volumeLiters = null
        )
        val result = validator(tx)
        assertTrue(result.isSuccess)
    }

    @Test
    fun negativeOdometer_returnsError() {
        val tx = FuelTransaction(
            vehicleId = "veh-123",
            totalAmountIdr = 50000L,
            odometerKm = -10.0
        )
        val result = validator(tx)
        assertFalse(result.isSuccess)
    }
}
