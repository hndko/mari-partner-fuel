package id.maripartner.fuel.domain.model

import java.time.LocalDate

/**
 * Ringkasan statistik pengeluaran bahan bakar untuk Dashboard.
 */
data class DashboardSummary(
    val currentMonth: LocalDate = LocalDate.now(),
    val totalExpenseIdr: Long = 0L,
    val totalVolumeLiters: Double = 0.0,
    val transactionCount: Int = 0,
    val averageExpensePerTransactionIdr: Long = 0L,
    val averagePricePerLiterIdr: Double? = null,
    val previousMonthExpenseIdr: Long = 0L,
    val expenseDifferenceIdr: Long = 0L,
    val expenseChangePercentage: Double? = null,
    val topSpendingVehicle: VehicleSpendingSummary? = null,
    val vehicleSummaries: List<VehicleSpendingSummary> = emptyList(),
    val recentTransactions: List<FuelTransaction> = emptyList(),
    val hasIncompleteVolumeData: Boolean = false
) {
    /**
     * Memeriksa apakah terdapat perbandingan dengan periode bulan lalu yang valid.
     */
    val hasComparisonData: Boolean
        get() = previousMonthExpenseIdr > 0L
}

/**
 * Agregasi pengeluaran per kendaraan.
 */
data class VehicleSpendingSummary(
    val vehicleId: String,
    val vehicleName: String,
    val totalAmountIdr: Long,
    val totalVolumeLiters: Double,
    val transactionCount: Int,
    val percentageOfTotal: Double
)
