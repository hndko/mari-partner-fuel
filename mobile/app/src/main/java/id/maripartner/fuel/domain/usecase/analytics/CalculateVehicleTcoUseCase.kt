package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.VehicleTcoResult

class CalculateVehicleTcoUseCase {

    operator fun invoke(
        transactions: List<FuelTransaction>,
        totalMaintenanceCostIdr: Long = 0L,
        driverRevenueRatePerKm: Long = 0L
    ): VehicleTcoResult {
        val totalFuelCost: Long = transactions.sumOf { it.totalAmountIdr }
        val totalOwnershipCost: Long = totalFuelCost + totalMaintenanceCostIdr

        // Hitung jarak tempuh dari perbedaan odometer maksimum dan minimum pada transaksi
        val sortedByOdo = transactions.mapNotNull { it.odometerKm }.sorted()
        val totalDistanceKm: Double = if (sortedByOdo.size >= 2) {
            (sortedByOdo.last() - sortedByOdo.first()).coerceAtLeast(1.0)
        } else {
            // Estimasi dari efisiensi liter * 12 km/l jika log odometer tunggal
            val totalLiters = transactions.sumOf { it.volumeLiters ?: 0.0 }
            (totalLiters * 12.0).coerceAtLeast(1.0)
        }

        val fuelCostPerKm = if (totalDistanceKm > 0) totalFuelCost.toDouble() / totalDistanceKm else 0.0
        val maintenanceCostPerKm = if (totalDistanceKm > 0) totalMaintenanceCostIdr.toDouble() / totalDistanceKm else 0.0
        val trueCostPerKm = fuelCostPerKm + maintenanceCostPerKm

        val driverNetMargin = if (driverRevenueRatePerKm > 0) {
            driverRevenueRatePerKm.toDouble() - trueCostPerKm
        } else {
            0.0
        }

        val profitMarginPercent = if (driverRevenueRatePerKm > 0) {
            (driverNetMargin / driverRevenueRatePerKm.toDouble() * 100.0).coerceIn(-100.0, 100.0)
        } else {
            0.0
        }

        return VehicleTcoResult(
            totalFuelCostIdr = totalFuelCost,
            totalMaintenanceCostIdr = totalMaintenanceCostIdr,
            totalOwnershipCostIdr = totalOwnershipCost,
            totalDistanceKm = totalDistanceKm,
            fuelCostPerKm = fuelCostPerKm,
            maintenanceCostPerKm = maintenanceCostPerKm,
            trueCostPerKm = trueCostPerKm,
            driverRevenueRatePerKm = driverRevenueRatePerKm,
            driverNetMarginPerKm = driverNetMargin,
            driverProfitMarginPercent = profitMarginPercent
        )
    }
}
