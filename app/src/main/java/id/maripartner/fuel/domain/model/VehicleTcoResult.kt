package id.maripartner.fuel.domain.model

data class VehicleTcoResult(
    val totalFuelCostIdr: Long = 0L,
    val totalMaintenanceCostIdr: Long = 0L,
    val totalOwnershipCostIdr: Long = 0L,
    val totalDistanceKm: Double = 0.0,
    val fuelCostPerKm: Double = 0.0,
    val maintenanceCostPerKm: Double = 0.0,
    val trueCostPerKm: Double = 0.0,
    val estimatedMonthlyTcoIdr: Long = 0L,
    val driverRevenueRatePerKm: Long = 0L,
    val driverNetMarginPerKm: Double = 0.0,
    val driverProfitMarginPercent: Double = 0.0
)
