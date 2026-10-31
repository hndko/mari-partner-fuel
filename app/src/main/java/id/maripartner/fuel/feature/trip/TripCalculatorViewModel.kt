package id.maripartner.fuel.feature.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.FuelPricePreset
import id.maripartner.fuel.domain.model.IndonesianFuelPrices
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.subscription.GetMyEntitlementsUseCase
import id.maripartner.fuel.domain.usecase.trip.CalculateTripCostUseCase
import id.maripartner.fuel.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripCalculatorViewModel(
    private val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(),
    private val getVehiclesUseCase: GetVehiclesUseCase = GetVehiclesUseCase(vehicleRepository),
    private val getMyEntitlementsUseCase: GetMyEntitlementsUseCase = GetMyEntitlementsUseCase(),
    val calculateTripCostUseCase: CalculateTripCostUseCase = CalculateTripCostUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripCalculatorUiState())
    val uiState: StateFlow<TripCalculatorUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        recalculate()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // 1. Cek Entitlement Pro
            try {
                when (val entResult = getMyEntitlementsUseCase()) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(isProUser = entResult.data.isPro) }
                    }
                    else -> {}
                }
            } catch (_: Exception) {}

            // 2. Load Kendaraan
            getVehiclesUseCase(includeArchived = false).collect { result ->
                if (result is AppResult.Success) {
                    val vehicles = result.data
                    _uiState.update { current ->
                        val selected = vehicles.firstOrNull()
                        val initialEfficiency = when (selected?.vehicleType) {
                            VehicleType.MOTORCYCLE -> 40.0
                            VehicleType.CAR -> 12.0
                            else -> 12.0
                        }
                        current.copy(
                            availableVehicles = vehicles,
                            selectedVehicleId = selected?.id,
                            efficiencyKmPerLiter = if (vehicles.isNotEmpty()) initialEfficiency else current.efficiencyKmPerLiter,
                            efficiencyInput = if (vehicles.isNotEmpty()) "%.1f".format(initialEfficiency) else current.efficiencyInput
                        )
                    }
                    recalculate()
                }
            }
        }
    }

    fun onEvent(event: TripCalculatorUiEvent) {
        when (event) {
            is TripCalculatorUiEvent.DistanceChanged -> {
                val clean = event.input.filter { it.isDigit() || it == '.' }
                val dist = clean.toDoubleOrNull() ?: 0.0
                _uiState.update { it.copy(distanceKmInput = clean, distanceKm = dist) }
                recalculate()
            }

            is TripCalculatorUiEvent.VehicleSelected -> {
                val vehicle = _uiState.value.availableVehicles.firstOrNull { it.id == event.vehicleId }
                val efficiency = when (vehicle?.vehicleType) {
                    VehicleType.MOTORCYCLE -> 40.0
                    VehicleType.CAR -> 12.0
                    else -> 12.0
                }
                // Preset BBM jika kendaraan memiliki defaultFuelType
                val matchingPreset = IndonesianFuelPrices.presets.firstOrNull {
                    vehicle?.defaultFuelType != null && it.name.contains(vehicle.defaultFuelType, ignoreCase = true)
                } ?: _uiState.value.selectedPreset

                _uiState.update {
                    it.copy(
                        selectedVehicleId = event.vehicleId,
                        efficiencyKmPerLiter = efficiency,
                        efficiencyInput = "%.1f".format(efficiency),
                        selectedPreset = matchingPreset
                    )
                }
                recalculate()
            }

            is TripCalculatorUiEvent.EfficiencyChanged -> {
                val clean = event.input.filter { it.isDigit() || it == '.' }
                val eff = clean.toDoubleOrNull() ?: 12.0
                _uiState.update { it.copy(efficiencyInput = clean, efficiencyKmPerLiter = eff) }
                recalculate()
            }

            is TripCalculatorUiEvent.PresetSelected -> {
                _uiState.update {
                    it.copy(
                        selectedPreset = event.preset,
                        isCustomPrice = false
                    )
                }
                recalculate()
            }

            is TripCalculatorUiEvent.CustomPriceChanged -> {
                val clean = event.input.filter { it.isDigit() }
                _uiState.update {
                    it.copy(
                        customPriceInput = clean,
                        isCustomPrice = true
                    )
                }
                recalculate()
            }

            is TripCalculatorUiEvent.PassengerCountChanged -> {
                val safeCount = event.count.coerceIn(1, 20)
                _uiState.update { it.copy(passengerCount = safeCount) }
                recalculate()
            }

            is TripCalculatorUiEvent.Calculate -> {
                recalculate()
            }

            is TripCalculatorUiEvent.DismissMessage -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun recalculate() {
        val state = _uiState.value
        val fuelPrice = if (state.isCustomPrice) {
            state.customPriceInput.toLongOrNull() ?: state.selectedPreset.priceIdr
        } else {
            state.selectedPreset.priceIdr
        }

        val selectedVehicle = state.availableVehicles.firstOrNull { it.id == state.selectedVehicleId }
        val vehicleName = selectedVehicle?.name ?: "Kendaraan Umum"
        val fuelName = if (state.isCustomPrice) "BBM Kustom" else state.selectedPreset.name

        val result = calculateTripCostUseCase.calculate(
            distanceKm = state.distanceKm,
            fuelEfficiencyKmPerLiter = state.efficiencyKmPerLiter,
            fuelPricePerLiterIdr = fuelPrice,
            passengerCount = state.passengerCount,
            vehicleName = vehicleName,
            fuelTypeName = fuelName
        )

        _uiState.update { it.copy(calculationResult = result) }
    }

    fun getShareableSummary(): String {
        val res = _uiState.value.calculationResult ?: return ""
        return calculateTripCostUseCase.generateShareableSummary(res)
    }
}
