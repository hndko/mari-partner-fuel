package id.maripartner.fuel.data.remote.dto

import id.maripartner.fuel.domain.model.FuelBrandPrice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object untuk tabel 'fuel_prices' di Supabase.
 */
@Serializable
data class FuelPriceDto(
    val id: String? = null,
    val brand: String,
    @SerialName("fuel_name") val fuelName: String,
    @SerialName("grade_category") val gradeCategory: String,
    @SerialName("price_per_liter_idr") val pricePerLiterIdr: Long,
    @SerialName("is_subsidized") val isSubsidized: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toDomain(): FuelBrandPrice {
        return FuelBrandPrice(
            id = id.orEmpty(),
            brand = brand,
            fuelName = fuelName,
            gradeCategory = gradeCategory,
            pricePerLiterIdr = pricePerLiterIdr,
            isSubsidized = isSubsidized,
            updatedAt = updatedAt
        )
    }
}
