package id.maripartner.fuel.domain.model

import java.time.LocalDate

/**
 * Filter rentang periode analitik pengeluaran.
 */
enum class AnalyticsPeriod(val label: String) {
    THIS_MONTH("Bulan Ini"),
    LAST_MONTH("Bulan Lalu"),
    LAST_3_MONTHS("3 Bulan"),
    LAST_6_MONTHS("6 Bulan"),
    THIS_YEAR("Tahun Ini"),
    CUSTOM("Kustom")
}

/**
 * Model laporan agregasi analitik pengeluaran dan konsumsi.
 */
data class AnalyticsReport(
    val period: AnalyticsPeriod,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalExpenseIdr: Long = 0L,
    val totalVolumeLiters: Double = 0.0,
    val transactionCount: Int = 0,
    val averageExpensePerTransactionIdr: Long = 0L,
    val averagePricePerLiterIdr: Double? = null,
    val previousPeriodExpenseIdr: Long? = null,
    val periodChangePercentage: Double? = null,
    val timeSeriesExpenses: List<TimePeriodExpense> = emptyList(),
    val vehicleDistribution: List<VehicleSpendingSummary> = emptyList(),
    val efficiencySummaries: List<VehicleEfficiencySummary> = emptyList(),
    val hasIncompleteVolumeData: Boolean = false
)

/**
 * Data titik grafik time-series harian atau bulanan.
 */
data class TimePeriodExpense(
    val label: String,
    val amountIdr: Long,
    val volumeLiters: Double,
    val date: LocalDate? = null
)

/**
 * Estimasi efisiensi bahan bakar per kendaraan (km/liter).
 */
data class VehicleEfficiencySummary(
    val vehicleId: String,
    val vehicleName: String,
    val totalDistanceKm: Double,
    val totalVolumeLiters: Double,
    val averageKmPerLiter: Double?,
    val validPairsCount: Int,
    val isSufficientData: Boolean,
    val explanationNote: String? = null
)
