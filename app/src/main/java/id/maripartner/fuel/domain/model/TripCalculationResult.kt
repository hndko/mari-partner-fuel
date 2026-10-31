package id.maripartner.fuel.domain.model

/**
 * Hasil perhitungan kalkulasi estimasi kebutuhan dan biaya perjalanan bahan bakar.
 */
data class TripCalculationResult(
    val distanceKm: Double = 0.0,
    val fuelEfficiencyKmPerLiter: Double = 12.0,
    val fuelPricePerLiterIdr: Long = 10_000L,
    val passengerCount: Int = 1,
    val requiredLiters: Double = 0.0,
    val totalFuelCostIdr: Long = 0L,
    val costPerPassengerIdr: Long = 0L,
    val estimatedCo2EmissionKg: Double = 0.0,
    val vehicleName: String = "",
    val fuelTypeName: String = ""
)
