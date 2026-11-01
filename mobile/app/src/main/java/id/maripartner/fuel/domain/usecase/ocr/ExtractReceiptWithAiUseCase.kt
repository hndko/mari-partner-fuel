package id.maripartner.fuel.domain.usecase.ocr

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.ai.AiReceiptExtractor
import id.maripartner.fuel.data.remote.ai.AiReceiptExtractorImpl
import id.maripartner.fuel.domain.model.ReceiptCandidateData
import id.maripartner.fuel.domain.usecase.transaction.EstimateFuelCalculationUseCase

/**
 * Use case untuk memanggil ekstraksi Cloud AI melalui Supabase Edge Function 'extract-fuel-receipt'.
 * Menegakkan kuota AI, idempotency key, dan verifikasi selisih matematis.
 */
class ExtractReceiptWithAiUseCase(
    private val extractor: AiReceiptExtractor = AiReceiptExtractorImpl(),
    private val calculator: EstimateFuelCalculationUseCase = EstimateFuelCalculationUseCase()
) {

    suspend operator fun invoke(
        storagePath: String? = null,
        rawOcrText: String? = null,
        existingLocalData: ReceiptCandidateData? = null,
        idempotencyKey: String? = null
    ): AppResult<ReceiptCandidateData> {
        val result = extractor.extractReceipt(storagePath, rawOcrText, idempotencyKey)
        if (result is AppResult.Error) {
            return AppResult.Error(result.exception)
        }

        val responseDto = (result as AppResult.Success).data
        var candidate = ReceiptCandidateData.fromAiExtraction(
            aiResponse = responseDto,
            fallbackRawText = rawOcrText.orEmpty(),
            existingLocal = existingLocalData
        )

        // Verifikasi selisih matematis
        if (candidate.totalAmountIdr != null && candidate.volumeLiters != null && candidate.pricePerLiterIdr != null) {
            val discrepancy = calculator.checkDiscrepancy(
                totalAmountIdr = candidate.totalAmountIdr,
                volumeLiters = candidate.volumeLiters,
                pricePerLiterIdr = candidate.pricePerLiterIdr
            )
            if (discrepancy.hasDiscrepancy) {
                candidate = candidate.copy(discrepancyMessage = discrepancy.message)
            }
        }

        return AppResult.Success(candidate)
    }
}
