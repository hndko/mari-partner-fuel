package id.maripartner.fuel.domain.model

/**
 * Hasil simulasi proyeksi kenaikan harga bahan bakar nasional (Fuel Shock Simulator).
 */
data class FuelShockSimulationResult(
    val priceHikePerLiter: Double,
    val currentMonthlyLiters: Double,
    val currentMonthlyCostIdr: Long,
    val projectedMonthlyCostIdr: Long,
    val monthlyImpactIdr: Long,
    val yearlyImpactIdr: Long,
    val percentIncrease: Double,
    val recommendedKmReduction: Double,
    val actionableTips: List<String>
)
