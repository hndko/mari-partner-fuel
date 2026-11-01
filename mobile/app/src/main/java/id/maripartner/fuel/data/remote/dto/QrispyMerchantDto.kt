package id.maripartner.fuel.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QrispyMerchantDataResponseDto(
    val success: Boolean,
    val data: QrispyMerchantPayloadDto? = null,
    val error: String? = null
)

@Serializable
data class QrispyMerchantPayloadDto(
    val balance: QrispyBalanceDto,
    val transactions: List<QrispyTxDto> = emptyList()
)

@Serializable
data class QrispyBalanceDto(
    @SerialName("merchant_id") val merchantId: String = "",
    @SerialName("merchant_name") val merchantName: String = "Mari Partner Fuel",
    val balance: Long = 0L,
    val currency: String = "IDR"
)

@Serializable
data class QrispyTxDto(
    @SerialName("qris_id") val qrisId: String = "",
    val amount: Long = 0L,
    val status: String = "pending",
    @SerialName("qris_image_url") val qrisImageUrl: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("expired_at") val expiredAt: String = "",
    @SerialName("payment_reference") val paymentReference: String = "",
    @SerialName("generated_via") val generatedVia: String = "api"
)
