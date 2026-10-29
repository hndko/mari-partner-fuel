package id.maripartner.fuel.feature.receipt_scan

import android.graphics.Bitmap
import id.maripartner.fuel.domain.model.ReceiptCandidateData
import id.maripartner.fuel.domain.model.Vehicle
import java.time.LocalDate
import java.time.LocalTime

enum class OcrProcessingStage(val displayLabel: String) {
    IDLE("Pilih atau potret struk"),
    PREPARING_IMAGE("Menyiapkan gambar struk..."),
    READING_TEXT("Membaca teks struk dengan ML Kit lokal..."),
    PROCESSING_FIELDS("Mengekstrak data transaksi BBM..."),
    CALLING_CLOUD_AI("Mengoptimalkan dengan AI Cloud via server..."),
    COMPLETED("Ekstraksi selesai"),
    FAILED("Pemrosesan gagal")
}

data class ReceiptScanUiState(
    val selectedBitmap: Bitmap? = null,
    val imageBytes: ByteArray? = null,
    val imageMimeType: String = "image/jpeg",
    val stage: OcrProcessingStage = OcrProcessingStage.IDLE,
    val candidateData: ReceiptCandidateData? = null,
    val availableVehicles: List<Vehicle> = emptyList(),
    val selectedVehicleId: String = "",
    // Field formulir review yang dapat disunting pengguna
    val totalAmountRaw: String = "",
    val volumeLitersRaw: String = "",
    val pricePerLiterRaw: String = "",
    val fuelType: String = "",
    val stationName: String = "",
    val transactionDate: LocalDate = LocalDate.now(),
    val transactionTime: LocalTime = LocalTime.now(),
    val odometerKmRaw: String = "",
    val notes: String = "",
    val discrepancyWarning: String? = null,
    val showAiConsentDialog: Boolean = false,
    val isAiProcessing: Boolean = false,
    val isSubmittingTransaction: Boolean = false,
    val isSuccessSaved: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null
) {
    val isImageLoaded: Boolean
        get() = selectedBitmap != null

    val isProcessing: Boolean
        get() = stage in listOf(
            OcrProcessingStage.PREPARING_IMAGE,
            OcrProcessingStage.READING_TEXT,
            OcrProcessingStage.PROCESSING_FIELDS,
            OcrProcessingStage.CALLING_CLOUD_AI
        )
}

sealed interface ReceiptScanUiEvent {
    data class ImageSelected(val bitmap: Bitmap, val bytes: ByteArray, val mimeType: String) : ReceiptScanUiEvent
    data object ClearImage : ReceiptScanUiEvent
    data object RetryLocalOcr : ReceiptScanUiEvent
    data object RequestAiOptimization : ReceiptScanUiEvent
    data object ConfirmAiOptimization : ReceiptScanUiEvent
    data object DismissAiConsent : ReceiptScanUiEvent
    data class VehicleSelected(val vehicleId: String) : ReceiptScanUiEvent
    data class TotalAmountChanged(val text: String) : ReceiptScanUiEvent
    data class VolumeLitersChanged(val text: String) : ReceiptScanUiEvent
    data class PricePerLiterChanged(val text: String) : ReceiptScanUiEvent
    data class FuelTypeChanged(val text: String) : ReceiptScanUiEvent
    data class StationNameChanged(val text: String) : ReceiptScanUiEvent
    data class OdometerChanged(val text: String) : ReceiptScanUiEvent
    data class NotesChanged(val text: String) : ReceiptScanUiEvent
    data object ConfirmAndSave : ReceiptScanUiEvent
    data object DismissMessage : ReceiptScanUiEvent
}
