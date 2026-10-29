package id.maripartner.fuel.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class SubscriptionPlanDto(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    @SerialName("billing_period") val billingPeriod: String = "monthly",
    @SerialName("price_idr") val priceIdr: Long = 0L,
    @SerialName("ai_scan_quota") val aiScanQuota: Int = 0,
    @SerialName("max_vehicles") val maxVehicles: Int? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    val features: JsonElement? = null,
    val description: String? = null
) {
    fun getFeaturesList(): List<String> {
        return try {
            features?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
