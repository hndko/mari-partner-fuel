package id.maripartner.fuel.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.data.remote.storage.ReceiptStorageManager
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.transaction.EstimateFuelCalculationUseCase
import id.maripartner.fuel.domain.usecase.transaction.SaveTransactionUseCase
import id.maripartner.fuel.domain.usecase.transaction.ValidateTransactionInputUseCase
import id.maripartner.fuel.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionFormViewModel(
    private val transactionRepository: FuelTransactionRepository = FuelTransactionRepositoryImpl(),
    private val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(),
    private val saveTransactionUseCase: SaveTransactionUseCase = SaveTransactionUseCase(transactionRepository),
    private val getVehiclesUseCase: GetVehiclesUseCase = GetVehiclesUseCase(vehicleRepository),
    private val calculator: EstimateFuelCalculationUseCase = EstimateFuelCalculationUseCase(),
    private val storageManager: ReceiptStorageManager = ReceiptStorageManager()
) : ViewModel() {

    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    init {
        loadActiveVehicles()
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
                        val initialFuel = if (current.fuelType.isBlank()) {
                            activeVehicles.firstOrNull { it.id == initialVehicleId }?.defaultFuelType.orEmpty()
                        } else {
                            current.fuelType
                        }
                        current.copy(
                            availableVehicles = activeVehicles,
                            vehicleId = initialVehicleId,
                            fuelType = initialFuel
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
                            notes = tx.notes.orEmpty(),
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
                _formState.update {
                    it.copy(
                        vehicleId = event.vehicleId,
                        fuelType = if (it.fuelType.isBlank()) defaultFuel else it.fuelType,
                        vehicleError = null
                    )
                }
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
                _formState.update { it.copy(fuelType = event.type) }
            }
            is TransactionFormEvent.OdometerChanged -> {
                _formState.update { it.copy(odometerKmRaw = event.raw) }
            }
            is TransactionFormEvent.NotesChanged -> {
                _formState.update { it.copy(notes = event.notes) }
            }
            is TransactionFormEvent.ReceiptPicked -> {
                val validation = storageManager.validateReceiptFile(event.bytes.size.toLong(), event.mimeType)
                if (validation is AppResult.Error) {
                    _formState.update { it.copy(errorMessage = validation.exception.message) }
                } else {
                    _formState.update {
                        it.copy(
                            receiptBytes = event.bytes,
                            receiptMimeType = event.mimeType,
                            removeReceipt = false
                        )
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

    private fun submit() {
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
            notes = s.notes.trim().takeIf { it.isNotBlank() },
            receiptFileId = if (s.removeReceipt) null else null // repository handles receipt linkage
        )

        _formState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val result = saveTransactionUseCase(
                transaction = transaction,
                receiptBytes = s.receiptBytes,
                receiptMimeType = s.receiptMimeType,
                removeReceipt = s.removeReceipt
            )

            when (result) {
                is AppResult.Success -> {
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
