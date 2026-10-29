package id.maripartner.fuel.feature.vehicles

import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType

enum class VehicleFilterTab(val displayName: String) {
    ACTIVE("Aktif"),
    ARCHIVED("Diarsipkan"),
    ALL("Semua")
}

data class VehiclesUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val filterTab: VehicleFilterTab = VehicleFilterTab.ACTIVE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null
) {
    val filteredVehicles: List<Vehicle>
        get() = when (filterTab) {
            VehicleFilterTab.ACTIVE -> vehicles.filter { !it.isArchived }
            VehicleFilterTab.ARCHIVED -> vehicles.filter { it.isArchived }
            VehicleFilterTab.ALL -> vehicles
        }
}

data class VehicleFormState(
    val id: String = "",
    val name: String = "",
    val vehicleType: VehicleType = VehicleType.MOTORCYCLE,
    val make: String = "",
    val model: String = "",
    val productionYear: String = "",
    val licensePlate: String = "",
    val defaultFuelType: String = "",
    val tankCapacityLiters: String = "",
    val lastOdometerKm: String = "",
    val notes: String = "",
    val isArchived: Boolean = false,
    val nameError: String? = null,
    val yearError: String? = null,
    val tankError: String? = null,
    val odometerError: String? = null,
    val isLoadingData: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isEditMode: Boolean
        get() = id.isNotBlank()
}

sealed interface VehicleUiEvent {
    data class SetFilterTab(val tab: VehicleFilterTab) : VehicleUiEvent
    data class ArchiveVehicle(val id: String, val archive: Boolean) : VehicleUiEvent
    data class DeleteVehicle(val id: String) : VehicleUiEvent
    data object Refresh : VehicleUiEvent
    data object DismissMessage : VehicleUiEvent
}

sealed interface VehicleFormEvent {
    data class NameChanged(val name: String) : VehicleFormEvent
    data class TypeChanged(val type: VehicleType) : VehicleFormEvent
    data class MakeChanged(val make: String) : VehicleFormEvent
    data class ModelChanged(val model: String) : VehicleFormEvent
    data class YearChanged(val year: String) : VehicleFormEvent
    data class LicensePlateChanged(val plate: String) : VehicleFormEvent
    data class DefaultFuelChanged(val fuel: String) : VehicleFormEvent
    data class TankCapacityChanged(val tank: String) : VehicleFormEvent
    data class OdometerChanged(val odo: String) : VehicleFormEvent
    data class NotesChanged(val notes: String) : VehicleFormEvent
    data object Submit : VehicleFormEvent
    data object ClearError : VehicleFormEvent
}
