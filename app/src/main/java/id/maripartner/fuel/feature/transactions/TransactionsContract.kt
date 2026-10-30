package id.maripartner.fuel.feature.transactions

import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.SortOrder
import id.maripartner.fuel.domain.model.TransactionFilter
import id.maripartner.fuel.domain.model.Vehicle
import java.time.Instant
import java.time.LocalDate

data class TransactionsUiState(
    val transactions: List<FuelTransaction> = emptyList(),
    val availableVehicles: List<Vehicle> = emptyList(),
    val filter: TransactionFilter = TransactionFilter(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val isExporting: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null
)

data class TransactionFormState(
    val id: String = "",
    val vehicleId: String = "",
    val availableVehicles: List<Vehicle> = emptyList(),
    val transactionAt: Instant = Instant.now(),
    val totalAmountRaw: String = "",
    val volumeLitersRaw: String = "",
    val pricePerLiterRaw: String = "",
    val stationName: String = "",
    val fuelType: String = "",
    val odometerKmRaw: String = "",
    val notes: String = "",
    val scanStatus: String = "manual",
    val isScanningOcr: Boolean = false,
    val receiptBytes: ByteArray? = null,
    val receiptMimeType: String? = null,
    val existingStoragePath: String? = null,
    val existingReceiptUrl: String? = null,
    val removeReceipt: Boolean = false,
    val discrepancyWarning: String? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val vehicleError: String? = null,
    val totalError: String? = null,
    val errorMessage: String? = null,
    val actionMessage: String? = null
) {
    val isEditMode: Boolean
        get() = id.isNotBlank()

    val hasReceiptAttachment: Boolean
        get() = (receiptBytes != null && !removeReceipt) || (!existingStoragePath.isNullOrBlank() && !removeReceipt)
}

data class TransactionDetailUiState(
    val transaction: FuelTransaction? = null,
    val signedReceiptUrl: String? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

sealed interface TransactionUiEvent {
    data class QueryChanged(val query: String) : TransactionUiEvent
    data class VehicleFilterChanged(val vehicleId: String?) : TransactionUiEvent
    data class FuelTypeFilterChanged(val fuelType: String?) : TransactionUiEvent
    data class DateRangeChanged(val startDate: LocalDate?, val endDate: LocalDate?) : TransactionUiEvent
    data class SortOrderChanged(val sortOrder: SortOrder) : TransactionUiEvent
    data object LoadNextPage : TransactionUiEvent
    data object Refresh : TransactionUiEvent
    data object DismissMessage : TransactionUiEvent
}

sealed interface TransactionFormEvent {
    data class VehicleSelected(val vehicleId: String) : TransactionFormEvent
    data class TransactionAtChanged(val instant: Instant) : TransactionFormEvent
    data class TotalAmountChanged(val raw: String) : TransactionFormEvent
    data class VolumeLitersChanged(val raw: String) : TransactionFormEvent
    data class PricePerLiterChanged(val raw: String) : TransactionFormEvent
    data class StationNameChanged(val name: String) : TransactionFormEvent
    data class FuelTypeChanged(val type: String) : TransactionFormEvent
    data class OdometerChanged(val raw: String) : TransactionFormEvent
    data class NotesChanged(val notes: String) : TransactionFormEvent
    data class ReceiptPicked(val bytes: ByteArray, val mimeType: String) : TransactionFormEvent
    data object RemoveReceipt : TransactionFormEvent
    data object Submit : TransactionFormEvent
    data object ClearError : TransactionFormEvent
    data object DismissMessage : TransactionFormEvent
}
