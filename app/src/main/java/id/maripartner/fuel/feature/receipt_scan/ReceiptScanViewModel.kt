package id.maripartner.fuel.feature.receipt_scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.ReceiptCandidateData
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.ocr.ExtractReceiptWithAiUseCase
import id.maripartner.fuel.domain.usecase.ocr.ProcessReceiptOcrUseCase
import id.maripartner.fuel.domain.usecase.transaction.EstimateFuelCalculationUseCase
import id.maripartner.fuel.domain.usecase.transaction.SaveTransactionUseCase
import id.maripartner.fuel.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ReceiptScanViewModel(
    private val processReceiptOcrUseCase: ProcessReceiptOcrUseCase = ProcessReceiptOcrUseCase(),
    private val extractReceiptWithAiUseCase: ExtractReceiptWithAiUseCase = ExtractReceiptWithAiUseCase(),
    private val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(),
    private val transactionRepository: FuelTransactionRepository = FuelTransactionRepositoryImpl(),
    private val getVehiclesUseCase: GetVehiclesUseCase = GetVehiclesUseCase(vehicleRepository),
    private val saveTransactionUseCase: SaveTransactionUseCase = SaveTransactionUseCase(transactionRepository),
    private val calculator: EstimateFuelCalculationUseCase = EstimateFuelCalculationUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptScanUiState())
    val uiState: StateFlow<ReceiptScanUiState> = _uiState.asStateFlow()

    private var usedCloudAi: Boolean = false

    init {
        loadActiveVehicles()
    }

    private fun loadActiveVehicles() {
        viewModelScope.launch {
            getVehiclesUseCase(includeArchived = false).collect { result ->
                if (result is AppResult.Success) {
                    val vehicles = result.data
                    _uiState.update { current ->
                        val defaultVehicleId = if (current.selectedVehicleId.isBlank()) {
                            vehicles.firstOrNull()?.id.orEmpty()
                        } else {
                            current.selectedVehicleId
                        }
                        current.copy(
                            availableVehicles = vehicles,
                            selectedVehicleId = defaultVehicleId
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: ReceiptScanUiEvent) {
        when (event) {
            is ReceiptScanUiEvent.ImageSelected -> {
                _uiState.update {
                    it.copy(
                        selectedBitmap = event.bitmap,
                        imageBytes = event.bytes,
                        imageMimeType = event.mimeType,
                        stage = OcrProcessingStage.PREPARING_IMAGE,
                        errorMessage = null
                    )
                }
                runLocalOcr()
            }
            is ReceiptScanUiEvent.ClearImage -> {
                _uiState.update {
                    ReceiptScanUiState(
                        availableVehicles = it.availableVehicles,
                        selectedVehicleId = it.selectedVehicleId
                    )
                }
            }
            is ReceiptScanUiEvent.RetryLocalOcr -> {
                runLocalOcr()
            }
            is ReceiptScanUiEvent.RequestAiOptimization -> {
                _uiState.update { it.copy(showAiConsentDialog = true) }
            }
            is ReceiptScanUiEvent.ConfirmAiOptimization -> {
                _uiState.update { it.copy(showAiConsentDialog = false) }
                runCloudAiExtraction()
            }
            is ReceiptScanUiEvent.DismissAiConsent -> {
                _uiState.update { it.copy(showAiConsentDialog = false) }
            }
            is ReceiptScanUiEvent.VehicleSelected -> {
                _uiState.update { it.copy(selectedVehicleId = event.vehicleId) }
            }
            is ReceiptScanUiEvent.TotalAmountChanged -> {
                val clean = event.text.filter { it.isDigit() }
                _uiState.update { it.copy(totalAmountRaw = clean) }
                checkDiscrepancy()
            }
            is ReceiptScanUiEvent.VolumeLitersChanged -> {
                _uiState.update { it.copy(volumeLitersRaw = event.text) }
                checkDiscrepancy()
            }
            is ReceiptScanUiEvent.PricePerLiterChanged -> {
                _uiState.update { it.copy(pricePerLiterRaw = event.text) }
                checkDiscrepancy()
            }
            is ReceiptScanUiEvent.FuelTypeChanged -> {
                _uiState.update { it.copy(fuelType = event.text) }
            }
            is ReceiptScanUiEvent.StationNameChanged -> {
                _uiState.update { it.copy(stationName = event.text) }
            }
            is ReceiptScanUiEvent.OdometerChanged -> {
                _uiState.update { it.copy(odometerKmRaw = event.text) }
            }
            is ReceiptScanUiEvent.NotesChanged -> {
                _uiState.update { it.copy(notes = event.text) }
            }
            is ReceiptScanUiEvent.ConfirmAndSave -> {
                confirmAndSave()
            }
            is ReceiptScanUiEvent.DismissMessage -> {
                _uiState.update { it.copy(actionMessage = null, errorMessage = null) }
            }
        }
    }

    private fun runLocalOcr() {
        val bitmap = _uiState.value.selectedBitmap ?: return

        _uiState.update { it.copy(stage = OcrProcessingStage.READING_TEXT, errorMessage = null) }

        viewModelScope.launch {
            _uiState.update { it.copy(stage = OcrProcessingStage.PROCESSING_FIELDS) }

            when (val result = processReceiptOcrUseCase(bitmap)) {
                is AppResult.Success -> {
                    val candidate = result.data
                    _uiState.update {
                        it.copy(
                            stage = OcrProcessingStage.COMPLETED,
                            candidateData = candidate,
                            totalAmountRaw = candidate.totalAmountIdr?.toString().orEmpty(),
                            volumeLitersRaw = candidate.volumeLiters?.toString().orEmpty(),
                            pricePerLiterRaw = candidate.pricePerLiterIdr?.let { p -> "%.2f".format(p) }.orEmpty(),
                            fuelType = candidate.fuelType.orEmpty(),
                            stationName = candidate.stationName.orEmpty(),
                            transactionDate = candidate.transactionDate ?: LocalDate.now(),
                            transactionTime = candidate.transactionTime ?: LocalTime.now(),
                            discrepancyWarning = candidate.discrepancyMessage,
                            actionMessage = "Berhasil membaca struk dengan Google ML Kit lokal"
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            stage = OcrProcessingStage.FAILED,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun runCloudAiExtraction() {
        val current = _uiState.value
        _uiState.update {
            it.copy(
                isAiProcessing = true,
                stage = OcrProcessingStage.CALLING_CLOUD_AI,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val rawText = current.candidateData?.rawOcrText.orEmpty()
            val result = extractReceiptWithAiUseCase(
                rawOcrText = rawText,
                existingLocalData = current.candidateData
            )

            when (result) {
                is AppResult.Success -> {
                    usedCloudAi = true
                    val aiCandidate = result.data
                    _uiState.update {
                        it.copy(
                            isAiProcessing = false,
                            stage = OcrProcessingStage.COMPLETED,
                            candidateData = aiCandidate,
                            totalAmountRaw = aiCandidate.totalAmountIdr?.toString() ?: it.totalAmountRaw,
                            volumeLitersRaw = aiCandidate.volumeLiters?.toString() ?: it.volumeLitersRaw,
                            pricePerLiterRaw = aiCandidate.pricePerLiterIdr?.let { p -> "%.2f".format(p) } ?: it.pricePerLiterRaw,
                            fuelType = aiCandidate.fuelType ?: it.fuelType,
                            stationName = aiCandidate.stationName ?: it.stationName,
                            transactionDate = aiCandidate.transactionDate ?: it.transactionDate,
                            transactionTime = aiCandidate.transactionTime ?: it.transactionTime,
                            discrepancyWarning = aiCandidate.discrepancyMessage ?: it.discrepancyWarning,
                            actionMessage = "Data struk berhasil dioptimalkan oleh Cloud AI"
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAiProcessing = false,
                            stage = OcrProcessingStage.COMPLETED,
                            errorMessage = "AI Cloud: ${result.exception.message}. Anda tetap dapat menggunakan hasil OCR lokal."
                        )
                    }
                }
            }
        }
    }

    private fun checkDiscrepancy() {
        val s = _uiState.value
        val total = s.totalAmountRaw.toLongOrNull() ?: 0L
        val volume = s.volumeLitersRaw.replace(',', '.').toDoubleOrNull()
        val price = s.pricePerLiterRaw.replace(',', '.').toDoubleOrNull()

        if (total > 0 && volume != null && price != null) {
            val res = calculator.checkDiscrepancy(total, volume, price)
            _uiState.update { it.copy(discrepancyWarning = if (res.hasDiscrepancy) res.message else null) }
        } else {
            _uiState.update { it.copy(discrepancyWarning = null) }
        }
    }

    private fun confirmAndSave() {
        val s = _uiState.value
        if (s.isSubmittingTransaction) return

        if (s.selectedVehicleId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pilih kendaraan terlebih dahulu.") }
            return
        }

        val totalAmount = s.totalAmountRaw.toLongOrNull() ?: 0L
        if (totalAmount <= 0) {
            _uiState.update { it.copy(errorMessage = "Total pembayaran harus lebih besar dari Rp 0.") }
            return
        }

        val volume = s.volumeLitersRaw.trim().replace(',', '.').toDoubleOrNull()
        val price = s.pricePerLiterRaw.trim().replace(',', '.').toDoubleOrNull()
        val odo = s.odometerKmRaw.trim().replace(',', '.').toDoubleOrNull()

        // Gabungkan tanggal dan waktu menjadi Instant
        val dateTime = s.transactionDate.atTime(s.transactionTime)
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()

        val scanStatus = if (usedCloudAi) "ai_review" else "ocr_review"

        val transaction = FuelTransaction(
            vehicleId = s.selectedVehicleId,
            transactionAt = instant,
            totalAmountIdr = totalAmount,
            volumeLiters = volume,
            pricePerLiterIdr = price,
            stationName = s.stationName.trim().takeIf { it.isNotBlank() },
            fuelType = s.fuelType.trim().takeIf { it.isNotBlank() },
            odometerKm = odo,
            notes = s.notes.trim().takeIf { it.isNotBlank() },
            scanStatus = scanStatus
        )

        _uiState.update { it.copy(isSubmittingTransaction = true, errorMessage = null) }

        viewModelScope.launch {
            val result = saveTransactionUseCase(
                transaction = transaction,
                receiptBytes = s.imageBytes,
                receiptMimeType = s.imageMimeType,
                removeReceipt = false
            )

            when (result) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingTransaction = false,
                            isSuccessSaved = true
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingTransaction = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }
}
