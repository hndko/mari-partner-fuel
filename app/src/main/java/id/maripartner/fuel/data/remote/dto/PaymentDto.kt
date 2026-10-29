package id.maripartner.fuel.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckoutRequestDto(
    @SerialName("plan_code") val planCode: String,
    val provider: String
)

@Serializable
data class CheckoutResponseDto(
    val success: Boolean,
    @SerialName("payment_reference") val paymentReference: String = "",
    val provider: String = "",
    @SerialName("amount_idr") val amountIdr: Long = 0L,
    val currency: String = "IDR",
    @SerialName("plan_code") val planCode: String = "",
    @SerialName("plan_name") val planName: String = "",
    @SerialName("billing_period") val billingPeriod: String = "monthly",
    @SerialName("product_id") val productId: String? = null,
    @SerialName("base_plan_id") val basePlanId: String? = null,
    @SerialName("checkout_url") val checkoutUrl: String? = null,
    @SerialName("qris_image_url") val qrisImageUrl: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    val error: String? = null
)

@Serializable
data class VerifyPlayPurchaseRequestDto(
    @SerialName("purchase_token") val purchaseToken: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("base_plan_id") val basePlanId: String,
    @SerialName("payment_reference") val paymentReference: String? = null
)

@Serializable
data class VerifyPlayPurchaseResponseDto(
    val success: Boolean,
    @SerialName("is_pro") val isPro: Boolean = false,
    @SerialName("plan_code") val planCode: String = "",
    @SerialName("expires_at") val expiresAt: String? = null,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class PaymentStatusRequestDto(
    @SerialName("payment_reference") val paymentReference: String
)

@Serializable
data class PaymentStatusResponseDto(
    val success: Boolean,
    @SerialName("payment_reference") val paymentReference: String = "",
    val provider: String = "",
    @SerialName("amount_idr") val amountIdr: Long = 0L,
    val status: String = "pending",
    @SerialName("is_paid") val isPaid: Boolean = false,
    @SerialName("is_pro") val isPro: Boolean = false,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    val error: String? = null
)
