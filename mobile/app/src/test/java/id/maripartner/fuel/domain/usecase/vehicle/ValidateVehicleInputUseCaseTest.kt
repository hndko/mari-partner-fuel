package id.maripartner.fuel.domain.usecase.vehicle

import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateVehicleInputUseCaseTest {

    private val validator = ValidateVehicleInputUseCase()

    @Test
    fun validVehicle_returnsSuccess() {
        val vehicle = Vehicle(
            name = "Vario 160",
            vehicleType = VehicleType.MOTORCYCLE,
            make = "Honda",
            model = "Vario",
            productionYear = 2022,
            licensePlate = "B 1234 XYZ",
            tankCapacityLiters = 5.5,
            lastOdometerKm = 12000.0
        )
        val result = validator(vehicle)
        assertTrue(result.isSuccess)
    }

    @Test
    fun blankName_returnsError() {
        val vehicle = Vehicle(name = "   ")
        val result = validator(vehicle)
        assertFalse(result.isSuccess)
    }

    @Test
    fun futureYearBeyondMax_returnsError() {
        val vehicle = Vehicle(name = "Mobil Masa Depan", productionYear = 2200)
        val result = validator(vehicle)
        assertFalse(result.isSuccess)
    }

    @Test
    fun pastYearBefore1900_returnsError() {
        val vehicle = Vehicle(name = "Mobil Antik", productionYear = 1850)
        val result = validator(vehicle)
        assertFalse(result.isSuccess)
    }

    @Test
    fun negativeTankCapacity_returnsError() {
        val vehicle = Vehicle(name = "Motor", tankCapacityLiters = -10.0)
        val result = validator(vehicle)
        assertFalse(result.isSuccess)
    }

    @Test
    fun negativeOdometer_returnsError() {
        val vehicle = Vehicle(name = "Mobil", lastOdometerKm = -50.0)
        val result = validator(vehicle)
        assertFalse(result.isSuccess)
    }

    @Test
    fun optionalFieldsNull_returnsSuccess() {
        val vehicle = Vehicle(
            name = "Beat Minimal",
            vehicleType = VehicleType.MOTORCYCLE
        )
        val result = validator(vehicle)
        assertTrue(result.isSuccess)
    }
}
