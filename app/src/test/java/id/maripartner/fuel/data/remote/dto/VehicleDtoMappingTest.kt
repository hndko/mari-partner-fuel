package id.maripartner.fuel.data.remote.dto

import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleDtoMappingTest {

    @Test
    fun toDomain_mapsAllFieldsCorrectly() {
        val dto = VehicleDto(
            id = "veh-uuid-1",
            userId = "user-uuid-1",
            name = "Vario 160",
            vehicleType = "motorcycle",
            make = "Honda",
            model = "Vario",
            productionYear = 2023,
            licensePlate = "B 1234 XYZ",
            defaultFuelType = "Pertalite",
            tankCapacityLiters = 5.5,
            lastOdometerKm = 14500.5,
            isArchived = false,
            notes = "Motor harian",
            createdAt = "2026-10-29T07:00:00Z",
            updatedAt = "2026-10-29T07:00:00Z"
        )

        val domain = dto.toDomain()

        assertEquals("veh-uuid-1", domain.id)
        assertEquals("user-uuid-1", domain.userId)
        assertEquals("Vario 160", domain.name)
        assertEquals(VehicleType.MOTORCYCLE, domain.vehicleType)
        assertEquals("Honda", domain.make)
        assertEquals("Vario", domain.model)
        assertEquals(2023, domain.productionYear)
        assertEquals("B 1234 XYZ", domain.licensePlate)
        assertEquals("Pertalite", domain.defaultFuelType)
        assertEquals(5.5, domain.tankCapacityLiters ?: 0.0, 0.001)
        assertEquals(14500.5, domain.lastOdometerKm ?: 0.0, 0.001)
        assertFalse(domain.isArchived)
        assertEquals("Motor harian", domain.notes)
    }

    @Test
    fun fromDomain_mapsAllFieldsCorrectly() {
        val vehicle = Vehicle(
            id = "veh-1",
            userId = "user-1",
            name = "Avanza Veloz",
            vehicleType = VehicleType.CAR,
            make = "Toyota",
            model = "Veloz",
            productionYear = 2021,
            licensePlate = "d 5678 ab",
            defaultFuelType = "Pertamax",
            tankCapacityLiters = 45.0,
            lastOdometerKm = 35000.0,
            isArchived = true,
            notes = "Mobil keluarga"
        )

        val dto = VehicleDto.fromDomain(vehicle, userId = "user-override")

        assertEquals("veh-1", dto.id)
        assertEquals("user-override", dto.userId)
        assertEquals("Avanza Veloz", dto.name)
        assertEquals("car", dto.vehicleType)
        assertEquals("D 5678 AB", dto.licensePlate) // Upper-cased automatically
        assertTrue(dto.isArchived)
    }

    @Test
    fun blankId_mapsToNullInDto() {
        val vehicle = Vehicle(name = "Motor Baru")
        val dto = VehicleDto.fromDomain(vehicle)
        assertNull(dto.id)
    }
}
