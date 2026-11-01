package id.maripartner.fuel.feature.transactions

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.storage.ReceiptStorageManager
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.analytics.GetFleetDriverReportUseCase
import id.maripartner.fuel.domain.usecase.fuel.GetFuelPricesUseCase
import id.maripartner.fuel.domain.usecase.ocr.ProcessReceiptOcrUseCase
import id.maripartner.fuel.domain.usecase.transaction.DetectDuplicateTransactionUseCase
import id.maripartner.fuel.domain.usecase.transaction.EstimateFuelCalculationUseCase
import id.maripartner.fuel.domain.usecase.transaction.SaveTransactionUseCase
import id.maripartner.fuel.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZoneId

class TransactionFormViewModel(
    private val transactionRepository: FuelTransactionRepository = FuelTransactionRepositoryImpl(),
    private val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(),
    private val saveTransactionUseCase: SaveTransactionUseCase = SaveTransactionUseCase(transactionRepository),
    private val getVehiclesUseCase: GetVehiclesUseCase = GetVehiclesUseCase(vehicleRepository),
    private val calculator: EstimateFuelCalculationUseCase = EstimateFuelCalculationUseCase(),
    private val processReceiptOcrUseCase: ProcessReceiptOcrUseCase = ProcessReceiptOcrUseCase(),
    private val storageManager: ReceiptStorageManager = ReceiptStorageManager(),
    private val detectDuplicateTransactionUseCase: DetectDuplicateTransactionUseCase = DetectDuplicateTransactionUseCase(),
    private val getFleetDriverReportUseCase: GetFleetDriverReportUseCase = GetFleetDriverReportUseCase(),
    private val getFuelPricesUseCase: GetFuelPricesUseCase = GetFuelPricesUseCase()
) : ViewModel() {

    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    init {
        loadActiveVehicles()
        loadActiveFuelPrices()
    }

    private fun loadActiveFuelPrices() {
        viewModelScope.launch {
            when (val result = getFuelPricesUseCase()) {
                is AppResult.Success -> {
                    val prices = result.data
                    _formState.update { current ->
                        val autoPrice = if (current.pricePerLiterRaw.isBlank() && current.fuelType.isNotBlank()) {
                            getFuelPricesUseCase.findPriceForFuel(current.fuelType, prices)?.toString()
                        } else null
                        current.copy(
                            activeFuelPrices = prices,
                            pricePerLiterRaw = autoPrice ?: current.pricePerLiterRaw
                        )
                    }
                    verifyDiscrepancy()
                }
                is AppResult.Error -> {}
            }
        }
    }

    private fun loadActiveVehicles() {
        viewModelScope.launch {
            getVehiclesUseCase(includeArchived = false).collect { result ->
                if (result is AppResult.Success) {
                    val activeVehicles = result.data
                    _formState.update { current ->
                        val initialVehicleId = if (current.vehicleId.isBlank()) {
                            activeVehicles.firstOrNull()?.id.orEmpty()
                        } else {
                            current.vehicleId
                        }
                        val selectedV = activeVehicles.firstOrNull { it.id == initialVehicleId }
                        val initialFuel = if (current.fuelType.isBlank()) {
                            selectedV?.defaultFuelType.orEmpty()
                        } else {
                            current.fuelType
                        }
                        current.copy(
                            availableVehicles = activeVehicles,
                            vehicleId = initialVehicleId,
                            fuelType = initialFuel,
                            latestRecordedOdometer = selectedV?.lastOdometerKm
                        )
                    }
                }
            }
        }
    }

    fun loadTransaction(transactionId: String) {
        if (transactionId.isBlank()) return
        viewModelScope.launch {
            when (val result = transactionRepository.getTransactionById(transactionId)) {
                is AppResult.Success -> {
                    val tx = result.data
                    val extractedDriver = getFleetDriverReportUseCase.extractDriverName(tx.notes).orEmpty()
                    val cleanNotes = tx.notes?.let { getFleetDriverReportUseCase.formatDriverTag("", it) }.orEmpty()

                    _formState.update {
                        it.copy(
                            id = tx.id,
                            vehicleId = tx.vehicleId,
                            transactionAt = tx.transactionAt,
                            totalAmountRaw = tx.totalAmountIdr.toString(),
                            volumeLitersRaw = tx.volumeLiters?.toString().orEmpty(),
                            pricePerLiterRaw = tx.pricePerLiterIdr?.let { p -> "%.2f".format(p) }.orEmpty(),
                            stationName = tx.stationName.orEmpty(),
                            fuelType = tx.fuelType.orEmpty(),
                            odometerKmRaw = tx.odometerKm?.toString().orEmpty(),
                            notes = cleanNotes,
                            driverName = extractedDriver,
                            scanStatus = tx.scanStatus,
                            existingStoragePath = tx.storagePath,
                            existingReceiptUrl = tx.signedReceiptUrl
                        )
                    }
                    verifyDiscrepancy()
                }
                is AppResult.Error -> {
                    _formState.update { it.copy(errorMessage = result.exception.message) }
                }
            }
        }
    }

    fun onEvent(event: TransactionFormEvent) {
        when (event) {
            is TransactionFormEvent.VehicleSelected -> {
                val vehicle = _formState.value.availableVehicles.firstOrNull { it.id == event.vehicleId }
                val defaultFuel = vehicle?.defaultFuelType.orEmpty()
                val targetFuel = if (_formState.value.fuelType.isBlank()) defaultFuel else _formState.value.fuelType
                val autoPrice = if (_formState.value.pricePerLiterRaw.isBlank() && targetFuel.isNotBlank()) {
                    getFuelPricesUseCase.findPriceForFuel(targetFuel, _formState.value.activeFuelPrices)?.toString()
                } else null

                _formState.update {
                    it.copy(
                        vehicleId = event.vehicleId,
                        fuelType = targetFuel,
                        pricePerLiterRaw = autoPrice ?: it.pricePerLiterRaw,
                        latestRecordedOdometer = vehicle?.lastOdometerKm,
                        vehicleError = null
                    )
                }
                verifyDiscrepancy()
            }
            is TransactionFormEvent.TransactionAtChanged -> {
                _formState.update { it.copy(transactionAt = event.instant) }
            }
            is TransactionFormEvent.TotalAmountChanged -> {
                val cleanDigits = event.raw.filter { it.isDigit() }
                _formState.update { it.copy(totalAmountRaw = cleanDigits, totalError = null) }
                verifyDiscrepancy()
            }
            is TransactionFormEvent.VolumeLitersChanged -> {
                _formState.update { it.copy(volumeLitersRaw = event.raw) }
                verifyDiscrepancy()
            }
            is TransactionFormEvent.PricePerLiterChanged -> {
                _formState.update { it.copy(pricePerLiterRaw = event.raw) }
                verifyDiscrepancy()
            }
            is TransactionFormEvent.StationNameChanged -> {
                _formState.update { it.copy(stationName = event.name) }
            }
            is TransactionFormEvent.FuelTypeChanged -> {
                val autoPrice = getFuelPricesUseCase.findPriceForFuel(event.type, _formState.value.activeFuelPrices)
                _formState.update {
                    it.copy(
                        fuelType = event.type,
                        pricePerLiterRaw = if (autoPrice != null && (it.pricePerLiterRaw.isBlank() || it.pricePerLiterRaw.toLongOrNull() != null)) {
                            autoPrice.toString()
                        } else {
                            it.pricePerLiterRaw
                        }
                    )
                }
                verifyDiscrepancy()
            }
            is TransactionFormEvent.OdometerChanged -> {
                _formState.update { it.copy(odometerKmRaw = event.raw) }
            }
            is TransactionFormEvent.NotesChanged -> {
                _formState.update { it.copy(notes = event.notes) }
            }
            is TransactionFormEvent.DriverNameChanged -> {
                _formState.update { it.copy(driverName = event.driverName) }
            }
            is TransactionFormEvent.ConfirmLowerOdometer -> {
                _formState.update { it.copy(showOdometerWarningDialog = false) }
                submit(ignoreOdometerWarning = true)
            }
            is TransactionFormEvent.DismissOdometerWarning -> {
                _formState.update { it.copy(showOdometerWarningDialog = false) }
            }
            is TransactionFormEvent.ConfirmDuplicateSubmission -> {
                _formState.update { it.copy(showDuplicateWarningDialog = false) }
                submit(ignoreDuplicateWarning = true)
            }
            is TransactionFormEvent.DismissDuplicateWarning -> {
                _formState.update { it.copy(showDuplicateWarningDialog = false) }
            }
            is TransactionFormEvent.ReceiptPicked -> {
                val validation = storageManager.validateReceiptFile(event.bytes.size.toLong(), event.mimeType)
                if (validation is AppResult.Error) {
                    _formState.update { it.copy(errorMessage = validation.exception.message) }
                } else {
                    _formState.update { it.copy(isScanningOcr = true, errorMessage = null) }
                    viewModelScope.launch {
                        try {
                            val bitmap = BitmapFactory.decodeByteArray(event.bytes, 0, event.bytes.size)
                            if (bitmap != null) {
                                val ocrResult = processReceiptOcrUseCase(bitmap)
                                bitmap.recycle()
                                if (ocrResult is AppResult.Success) {
                                    val candidate = ocrResult.data
                                    val candidateInstant = candidate.transactionDate?.let { d ->
                                        val t = candidate.transactionTime ?: LocalTime.now()
                                        d.atTime(t).atZone(ZoneId.systemDefault()).toInstant()
                                    }
                                    _formState.update { current ->
                                        current.copy(
                                            isScanningOcr = false,
                                            totalAmountRaw = candidate.totalAmountIdr?.toString() ?: current.totalAmountRaw,
                                            volumeLitersRaw = candidate.volumeLiters?.toString() ?: current.volumeLitersRaw,
                                            pricePerLiterRaw = candidate.pricePerLiterIdr?.let { p -> "%.2f".format(p) } ?: current.pricePerLiterRaw,
                                            stationName = candidate.stationName ?: current.stationName,
                                            fuelType = candidate.fuelType ?: current.fuelType,
                                            transactionAt = candidateInstant ?: current.transactionAt,
                                            scanStatus = "ocr_review",
                                            receiptBytes = null, // Foto tidak disimpan untuk upload ke server
                                            receiptMimeType = null,
                                            actionMessage = "Data struk berhasil diekstrak otomatis on-device! (Foto tidak disimpan di server)"
                                        )
                                    }
                                    verifyDiscrepancy()
                                } else {
                                    _formState.update {
                                        it.copy(
                                            isScanningOcr = false,
                                            errorMessage = "Gagal membaca teks struk. Anda dapat mengisi nominal secara manual."
                                        )
                                    }
                                }
                            } else {
                                _formState.update {
                                    it.copy(isScanningOcr = false, errorMessage = "Gagal memproses gambar struk.")
                                }
                            }
                        } catch (e: Exception) {
                            _formState.update {
                                it.copy(isScanningOcr = false, errorMessage = "Terjadi kesalahan saat memproses struk: ${e.message}")
                            }
                        }
                    }
                }
            }
            is TransactionFormEvent.RemoveReceipt -> {
                _formState.update {
                    it.copy(
                        receiptBytes = null,
                        receiptMimeType = null,
                        removeReceipt = true
                    )
                }
            }
            is TransactionFormEvent.Submit -> {
                submit()
            }
            is TransactionFormEvent.ClearError -> {
                _formState.update { it.copy(errorMessage = null) }
            }
            is TransactionFormEvent.DismissMessage -> {
                _formState.update { it.copy(actionMessage = null, errorMessage = null) }
            }
        }
    }

    private fun verifyDiscrepancy() {
        val s = _formState.value
        val total = s.totalAmountRaw.toLongOrNull() ?: 0L
        val volume = s.volumeLitersRaw.replace(',', '.').toDoubleOrNull()
        val price = s.pricePerLiterRaw.replace(',', '.').toDoubleOrNull()

        if (total > 0 && volume != null && price != null) {
            val res = calculator.checkDiscrepancy(total, volume, price)
            _formState.update {
                it.copy(discrepancyWarning = if (res.hasDiscrepancy) res.message else null)
            }
        } else {
            _formState.update { it.copy(discrepancyWarning = null) }
        }
    }

    private fun submit(
        ignoreOdometerWarning: Boolean = false,
        ignoreDuplicateWarning: Boolean = false
    ) {
        val s = _formState.value
        if (s.isSubmitting) return

        if (s.vehicleId.isBlank()) {
            _formState.update { it.copy(vehicleError = "Pilih kendaraan terlebih dahulu.") }
            return
        }

        val totalAmount = s.totalAmountRaw.toLongOrNull() ?: 0L
        if (totalAmount <= 0) {
            _formState.update { it.copy(totalError = "Total pembayaran harus lebih besar dari Rp 0.") }
            return
        }

        val volume = s.volumeLitersRaw.trim().replace(',', '.').toDoubleOrNull()
        val price = s.pricePerLiterRaw.trim().replace(',', '.').toDoubleOrNull()
        val odo = s.odometerKmRaw.trim().replace(',', '.').toDoubleOrNull()

        // 1. Validasi Odometer Mundur (Monotonic Guard)
        val selectedVehicle = s.availableVehicles.firstOrNull { it.id == s.vehicleId }
        val lastOdo = selectedVehicle?.lastOdometerKm ?: s.latestRecordedOdometer
        if (!ignoreOdometerWarning && odo != null && lastOdo != null && odo < lastOdo) {
            _formState.update {
                it.copy(
                    showOdometerWarningDialog = true,
                    pendingOdometerValue = odo,
                    latestRecordedOdometer = lastOdo
                )
            }
            return
        }

        // 2. Format Nama Pengemudi / Shift ke dalam Notes
        val formattedNotes = getFleetDriverReportUseCase.formatDriverTag(s.driverName, s.notes)

        val transaction = FuelTransaction(
            id = s.id,
            vehicleId = s.vehicleId,
            transactionAt = s.transactionAt,
            totalAmountIdr = totalAmount,
            volumeLiters = volume,
            pricePerLiterIdr = price,
            stationName = s.stationName.trim().takeIf { it.isNotBlank() },
            fuelType = s.fuelType.trim().takeIf { it.isNotBlank() },
            odometerKm = odo,
            notes = formattedNotes.trim().takeIf { it.isNotBlank() },
            scanStatus = s.scanStatus,
            receiptFileId = null // Ephemeral: transaksi disimpan murni teks & angka
        )

        // 3. Detektor Duplikasi Transaksi & Anti-Fraud
        if (!ignoreDuplicateWarning) {
            viewModelScope.launch {
                val recentRes = transactionRepository.getTransactions(
                    filter = id.maripartner.fuel.domain.model.TransactionFilter(vehicleId = s.vehicleId),
                    limit = 10
                )
                if (recentRes is AppResult.Success) {
                    val duplicateCheck = detectDuplicateTransactionUseCase(transaction, recentRes.data)
                    if (duplicateCheck.isDuplicate) {
                        _formState.update {
                            it.copy(
                                showDuplicateWarningDialog = true,
                                duplicateWarningMessage = duplicateCheck.message
                            )
                        }
                        return@launch
                    }
                }
                executeSave(transaction, odo, selectedVehicle)
            }
            return
        }

        executeSave(transaction, odo, selectedVehicle)
    }

    private fun executeSave(
        transaction: FuelTransaction,
        odo: Double?,
        selectedVehicle: id.maripartner.fuel.domain.model.Vehicle?
    ) {
        _formState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val result = saveTransactionUseCase(
                transaction = transaction,
                receiptBytes = null,
                receiptMimeType = null,
                removeReceipt = true
            )

            when (result) {
                is AppResult.Success -> {
                    // Update odometer terbaru kendaraan & perbarui widget
                    if (odo != null && selectedVehicle != null && odo > (selectedVehicle.lastOdometerKm ?: 0.0)) {
                        vehicleRepository.updateVehicle(selectedVehicle.copy(lastOdometerKm = odo))
                    }
                    try {
                        id.maripartner.fuel.core.widget.MariFuelAppWidgetProvider.updateAllWidgets(
                            id.maripartner.fuel.MariPartnerFuelApp.appContext
                        )
                    } catch (_: Exception) {}

                    _formState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
                is AppResult.Error -> {
                    _formState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }
}
