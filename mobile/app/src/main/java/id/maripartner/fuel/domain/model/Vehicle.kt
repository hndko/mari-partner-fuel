package id.maripartner.fuel.domain.model

import java.time.Instant

/**
 * Jenis kendaraan yang didukung aplikasi.
 */
enum class VehicleType(val code: String, val displayName: String) {
    MOTORCYCLE("motorcycle", "Motor"),
    CAR("car", "Mobil"),
    OTHER("other", "Lainnya");

    companion object {
        fun fromCode(code: String?): VehicleType {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: OTHER
        }
    }
}

/**
 * Model domain bersih untuk data kendaraan.
 */
data class Vehicle(
    val id: String = "",
    val userId: String = "",
    val name: String,
    val vehicleType: VehicleType = VehicleType.MOTORCYCLE,
    val make: String? = null,
    val model: String? = null,
    val productionYear: Int? = null,
    val licensePlate: String? = null,
    val defaultFuelType: String? = null,
    val tankCapacityLiters: Double? = null,
    val lastOdometerKm: Double? = null,
    val isArchived: Boolean = false,
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    val displaySubtitle: String
        get() {
            val parts = mutableListOf<String>()
            val brandModel = listOfNotNull(make?.takeIf { it.isNotBlank() }, model?.takeIf { it.isNotBlank() }).joinToString(" ")
            if (brandModel.isNotBlank()) parts.add(brandModel)
            productionYear?.let { parts.add(it.toString()) }
            licensePlate?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
            return if (parts.isEmpty()) vehicleType.displayName else parts.joinToString(" • ")
        }
}
