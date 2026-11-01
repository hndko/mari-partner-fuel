package id.maripartner.fuel.domain.model

import id.maripartner.fuel.data.ocr.ParsedReceiptData
import id.maripartner.fuel.data.remote.ai.AiExtractionResponseDto
import java.time.LocalDate
import java.time.LocalTime

/**
 * Model data domain untuk kandidat field yang diekstrak dari struk (baik melalui OCR lokal maupun Cloud AI).
 */
data class ReceiptCandidateData(
    val totalAmountIdr: Long? = null,
    val volumeLiters: Double? = null,
    val pricePerLiterIdr: Double? = null,
    val fuelType: String? = null,
    val stationName: String? = null,
    val transactionDate: LocalDate? = null,
    val transactionTime: LocalTime? = null,
    val receiptNumber: String? = null,
    val rawOcrText: String = "",
    val warnings: List<String> = emptyList(),
    val provider: String = "mlkit_local",
    val needsReview: Boolean = true,
    val discrepancyMessage: String? = null
) {
    companion object {
        fun fromLocalOcr(parsed: ParsedReceiptData): ReceiptCandidateData {
            return ReceiptCandidateData(
                totalAmountIdr = parsed.totalAmountIdr.value,
                volumeLiters = parsed.volumeLiters.value,
                pricePerLiterIdr = parsed.pricePerLiterIdr.value,
                fuelType = parsed.fuelType.value,
                stationName = parsed.stationName.value,
                transactionDate = parsed.transactionDate.value,
                transactionTime = parsed.transactionTime.value,
                receiptNumber = parsed.receiptNumber.value,
                rawOcrText = parsed.rawText,
                warnings = parsed.warnings,
                provider = "mlkit_local",
                needsReview = parsed.needsReview
            )
        }

        fun fromAiExtraction(
            aiResponse: AiExtractionResponseDto,
            fallbackRawText: String = "",
            existingLocal: ReceiptCandidateData? = null
        ): ReceiptCandidateData {
            val f = aiResponse.fields
            val parsedDate = f?.transactionDate?.let {
                try { LocalDate.parse(it) } catch (_: Exception) { null }
            }
            val parsedTime = f?.transactionTime?.let {
                try { LocalTime.parse(it) } catch (_: Exception) { null }
            }

            return ReceiptCandidateData(
                totalAmountIdr = f?.totalAmountIdr ?: existingLocal?.totalAmountIdr,
                volumeLiters = f?.volumeLiters ?: existingLocal?.volumeLiters,
                pricePerLiterIdr = f?.pricePerLiterIdr ?: existingLocal?.pricePerLiterIdr,
                fuelType = f?.fuelType ?: existingLocal?.fuelType,
                stationName = f?.stationName ?: existingLocal?.stationName,
                transactionDate = parsedDate ?: existingLocal?.transactionDate,
                transactionTime = parsedTime ?: existingLocal?.transactionTime,
                receiptNumber = f?.receiptNumber ?: existingLocal?.receiptNumber,
                rawOcrText = fallbackRawText.ifBlank { existingLocal?.rawOcrText.orEmpty() },
                warnings = aiResponse.warnings.ifEmpty { existingLocal?.warnings.orEmpty() },
                provider = aiResponse.provider ?: "gemini_cloud",
                needsReview = aiResponse.needsReview
            )
        }
    }
}
