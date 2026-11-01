package id.maripartner.fuel.data.remote.dto

import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Data Transfer Object untuk tabel 'vehicles' di Supabase.
 */
@Serializable
data class VehicleDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    @SerialName("vehicle_type") val vehicleType: String,
    val make: String? = null,
    val model: String? = null,
    @SerialName("production_year") val productionYear: Int? = null,
    @SerialName("license_plate") val licensePlate: String? = null,
    @SerialName("default_fuel_type") val defaultFuelType: String? = null,
    @SerialName("tank_capacity_liters") val tankCapacityLiters: Double? = null,
    @SerialName("last_odometer_km") val lastOdometerKm: Double? = null,
    @SerialName("is_archived") val isArchived: Boolean = false,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun toDomain(): Vehicle {
        return Vehicle(
            id = id.orEmpty(),
            userId = userId.orEmpty(),
            name = name,
            vehicleType = VehicleType.fromCode(vehicleType),
            make = make,
            model = model,
            productionYear = productionYear,
            licensePlate = licensePlate,
            defaultFuelType = defaultFuelType,
            tankCapacityLiters = tankCapacityLiters,
            lastOdometerKm = lastOdometerKm,
            isArchived = isArchived,
            notes = notes,
            createdAt = parseInstant(createdAt),
            updatedAt = parseInstant(updatedAt)
        )
    }

    companion object {
        fun fromDomain(vehicle: Vehicle, userId: String? = null): VehicleDto {
            return VehicleDto(
                id = vehicle.id.ifBlank { null },
                userId = userId ?: vehicle.userId.ifBlank { null },
                name = vehicle.name.trim(),
                vehicleType = vehicle.vehicleType.code,
                make = vehicle.make?.trim()?.takeIf { it.isNotBlank() },
                model = vehicle.model?.trim()?.takeIf { it.isNotBlank() },
                productionYear = vehicle.productionYear,
                licensePlate = vehicle.licensePlate?.trim()?.uppercase()?.takeIf { it.isNotBlank() },
                defaultFuelType = vehicle.defaultFuelType?.trim()?.takeIf { it.isNotBlank() },
                tankCapacityLiters = vehicle.tankCapacityLiters,
                lastOdometerKm = vehicle.lastOdometerKm,
                isArchived = vehicle.isArchived,
                notes = vehicle.notes?.trim()?.takeIf { it.isNotBlank() }
            )
        }

        private fun parseInstant(str: String?): Instant {
            if (str.isNullOrBlank()) return Instant.now()
            return try {
                Instant.parse(str)
            } catch (_: Exception) {
                Instant.now()
            }
        }
    }
}
