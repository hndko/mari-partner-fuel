package id.maripartner.fuel.feature.trip

import id.maripartner.fuel.domain.model.FuelPricePreset
import id.maripartner.fuel.domain.model.IndonesianFuelPrices
import id.maripartner.fuel.domain.model.TripCalculationResult
import id.maripartner.fuel.domain.model.Vehicle

/**
 * UI State untuk Layar Kalkulator Biaya Perjalanan & Split Bill.
 */
data class TripCalculatorUiState(
    val distanceKmInput: String = "50",
    val distanceKm: Double = 50.0,
    val selectedVehicleId: String? = null,
    val availableVehicles: List<Vehicle> = emptyList(),
    val efficiencyInput: String = "12.0",
    val efficiencyKmPerLiter: Double = 12.0,
    val selectedPreset: FuelPricePreset = IndonesianFuelPrices.presets[0],
    val customPriceInput: String = "10000",
    val isCustomPrice: Boolean = false,
    val passengerCount: Int = 1,
    val calculationResult: TripCalculationResult? = null,
    val isProUser: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * User Intent / Events untuk Layar Kalkulator Perjalanan.
 */
sealed interface TripCalculatorUiEvent {
    data class DistanceChanged(val input: String) : TripCalculatorUiEvent
    data class VehicleSelected(val vehicleId: String?) : TripCalculatorUiEvent
    data class EfficiencyChanged(val input: String) : TripCalculatorUiEvent
    data class PresetSelected(val preset: FuelPricePreset) : TripCalculatorUiEvent
    data class CustomPriceChanged(val input: String) : TripCalculatorUiEvent
    data class PassengerCountChanged(val count: Int) : TripCalculatorUiEvent
    data object Calculate : TripCalculatorUiEvent
    data object DismissMessage : TripCalculatorUiEvent
}
