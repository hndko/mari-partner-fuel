package id.maripartner.fuel.domain.model.owner

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model paket langganan SaaS yang dapat dikelola oleh Pemilik (Owner).
 */
@Serializable
data class OwnerPlanItem(
    @SerialName("id")
    val id: String,
    @SerialName("code")
    val code: String,
    @SerialName("name")
    val name: String,
    @SerialName("billing_period")
    val billingPeriod: String,
    @SerialName("price_idr")
    val priceIdr: Long,
    @SerialName("ai_scan_quota")
    val aiScanQuota: Int,
    @SerialName("max_vehicles")
    val maxVehicles: Int? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("description")
    val description: String? = null
)
