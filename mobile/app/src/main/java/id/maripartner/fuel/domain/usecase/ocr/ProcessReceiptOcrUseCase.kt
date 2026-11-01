package id.maripartner.fuel.domain.usecase.ocr

import android.graphics.Bitmap
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.ocr.ReceiptOcrProcessor
import id.maripartner.fuel.data.ocr.ReceiptTextParser
import id.maripartner.fuel.domain.model.ReceiptCandidateData
import id.maripartner.fuel.domain.usecase.transaction.EstimateFuelCalculationUseCase

/**
 * Use case untuk orkestrasi pemindaian OCR lokal on-device dan parsing kandidat transaksi BBM.
 */
class ProcessReceiptOcrUseCase(
    private val ocrProcessor: ReceiptOcrProcessor = ReceiptOcrProcessor(),
    private val parser: ReceiptTextParser = ReceiptTextParser(),
    private val calculator: EstimateFuelCalculationUseCase = EstimateFuelCalculationUseCase()
) {

    suspend operator fun invoke(bitmap: Bitmap): AppResult<ReceiptCandidateData> {
        val ocrResult = ocrProcessor.processImage(bitmap)
        if (ocrResult is AppResult.Error) {
            return AppResult.Error(ocrResult.exception)
        }

        val rawText = (ocrResult as AppResult.Success).data
        val parsed = parser.parse(rawText)
        var candidate = ReceiptCandidateData.fromLocalOcr(parsed)

        // Verifikasi selisih matematis antara volume x harga dengan total pembayaran
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
