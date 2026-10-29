package id.maripartner.fuel.data.remote.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiExtractionRequestDto(
    @SerialName("storage_path") val storagePath: String? = null,
    @SerialName("raw_ocr_text") val rawOcrText: String? = null,
    val provider: String = "gemini"
)

@Serializable
data class AiExtractedFieldsDto(
    @SerialName("transaction_date") val transactionDate: String? = null,
    @SerialName("transaction_time") val transactionTime: String? = null,
    @SerialName("total_amount_idr") val totalAmountIdr: Long? = null,
    @SerialName("volume_liters") val volumeLiters: Double? = null,
    @SerialName("price_per_liter_idr") val pricePerLiterIdr: Double? = null,
    @SerialName("fuel_type") val fuelType: String? = null,
    @SerialName("station_name") val stationName: String? = null,
    @SerialName("receipt_number") val receiptNumber: String? = null
)

@Serializable
data class AiExtractionResponseDto(
    val success: Boolean,
    val provider: String? = null,
    val fields: AiExtractedFieldsDto? = null,
    val warnings: List<String> = emptyList(),
    @SerialName("needs_review") val needsReview: Boolean = true,
    val error: String? = null
)
