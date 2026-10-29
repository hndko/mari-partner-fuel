package id.maripartner.fuel.feature.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.vehicle.SaveVehicleUseCase
import id.maripartner.fuel.domain.usecase.vehicle.ValidateVehicleInputUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VehicleFormViewModel(
    private val repository: VehicleRepository = VehicleRepositoryImpl(),
    private val saveVehicleUseCase: SaveVehicleUseCase = SaveVehicleUseCase(repository),
    private val validator: ValidateVehicleInputUseCase = ValidateVehicleInputUseCase()
) : ViewModel() {

    private val _formState = MutableStateFlow(VehicleFormState())
    val formState: StateFlow<VehicleFormState> = _formState.asStateFlow()

    fun loadVehicle(vehicleId: String) {
        if (vehicleId.isBlank()) return
        _formState.update { it.copy(isLoadingData = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.getVehicleById(vehicleId)) {
                is AppResult.Success -> {
                    val v = result.data
                    _formState.update {
                        it.copy(
                            id = v.id,
                            name = v.name,
                            vehicleType = v.vehicleType,
                            make = v.make.orEmpty(),
                            model = v.model.orEmpty(),
                            productionYear = v.productionYear?.toString().orEmpty(),
                            licensePlate = v.licensePlate.orEmpty(),
                            defaultFuelType = v.defaultFuelType.orEmpty(),
                            tankCapacityLiters = v.tankCapacityLiters?.toString().orEmpty(),
                            lastOdometerKm = v.lastOdometerKm?.toString().orEmpty(),
                            notes = v.notes.orEmpty(),
                            isArchived = v.isArchived,
                            isLoadingData = false
                        )
                    }
                }
                is AppResult.Error -> {
                    _formState.update {
                        it.copy(
                            isLoadingData = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: VehicleFormEvent) {
        when (event) {
            is VehicleFormEvent.NameChanged -> {
                _formState.update { it.copy(name = event.name, nameError = null) }
            }
            is VehicleFormEvent.TypeChanged -> {
                _formState.update { it.copy(vehicleType = event.type) }
            }
            is VehicleFormEvent.MakeChanged -> {
                _formState.update { it.copy(make = event.make) }
            }
            is VehicleFormEvent.ModelChanged -> {
                _formState.update { it.copy(model = event.model) }
            }
            is VehicleFormEvent.YearChanged -> {
                _formState.update { it.copy(productionYear = event.year, yearError = null) }
            }
            is VehicleFormEvent.LicensePlateChanged -> {
                _formState.update { it.copy(licensePlate = event.plate.uppercase()) }
            }
            is VehicleFormEvent.DefaultFuelChanged -> {
                _formState.update { it.copy(defaultFuelType = event.fuel) }
            }
            is VehicleFormEvent.TankCapacityChanged -> {
                _formState.update { it.copy(tankCapacityLiters = event.tank, tankError = null) }
            }
            is VehicleFormEvent.OdometerChanged -> {
                _formState.update { it.copy(lastOdometerKm = event.odo, odometerError = null) }
            }
            is VehicleFormEvent.NotesChanged -> {
                _formState.update { it.copy(notes = event.notes) }
            }
            is VehicleFormEvent.Submit -> {
                submit()
            }
            is VehicleFormEvent.ClearError -> {
                _formState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun submit() {
        val s = _formState.value
        if (s.isSubmitting) return

        val yearInt = s.productionYear.trim().toIntOrNull()
        val tankDouble = s.tankCapacityLiters.trim().toDoubleOrNull()
        val odoDouble = s.lastOdometerKm.trim().toDoubleOrNull()

        val vehicle = Vehicle(
            id = s.id,
            name = s.name.trim(),
            vehicleType = s.vehicleType,
            make = s.make.trim().takeIf { it.isNotBlank() },
            model = s.model.trim().takeIf { it.isNotBlank() },
            productionYear = yearInt,
            licensePlate = s.licensePlate.trim().uppercase().takeIf { it.isNotBlank() },
            defaultFuelType = s.defaultFuelType.trim().takeIf { it.isNotBlank() },
            tankCapacityLiters = tankDouble,
            lastOdometerKm = odoDouble,
            isArchived = s.isArchived,
            notes = s.notes.trim().takeIf { it.isNotBlank() }
        )

        val validation = validator(vehicle)
        if (!validation.isSuccess) {
            val err = (validation as AppResult.Error).exception.message
            _formState.update { it.copy(errorMessage = err) }
            return
        }

        _formState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = saveVehicleUseCase(vehicle)) {
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
