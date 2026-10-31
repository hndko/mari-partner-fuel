package id.maripartner.fuel.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiQuotaDto(
    val limit: Int = 5,
    val used: Int = 0,
    val remaining: Int = 5,
    @SerialName("resets_at") val resetsAt: String? = null
)

@Serializable
data class UserEntitlementDto(
    @SerialName("user_id") val userId: String = "",
    @SerialName("is_pro") val isPro: Boolean = false,
    @SerialName("plan_code") val planCode: String = "free",
    @SerialName("plan_name") val planName: String = "Mari Free",
    val status: String = "active",
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("auto_renew") val autoRenew: Boolean = false,
    @SerialName("max_vehicles") val maxVehicles: Int? = 2,
    val features: List<String> = emptyList(),
    @SerialName("ai_quota") val aiQuota: AiQuotaDto = AiQuotaDto()
)

@Serializable
data class SubscriptionRecordDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("plan_id") val planId: String = "",
    val status: String = "active",
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("auto_renew") val autoRenew: Boolean = false,
    @SerialName("provider") val provider: String? = null,
    @SerialName("subscription_plans") val subscriptionPlan: SubscriptionPlanDto? = null
)
