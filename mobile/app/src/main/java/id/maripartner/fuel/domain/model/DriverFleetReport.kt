package id.maripartner.fuel.domain.model

/**
 * Ringkasan konsumsi dan pengeluaran per sopir / pengemudi armada.
 */
data class DriverShiftSummary(
    val driverName: String,
    val totalTransactions: Int,
    val totalVolumeLiters: Double,
    val totalExpenseIdr: Long,
    val avgEfficiencyKmPerLiter: Double?
)

/**
 * Laporan rekapitulasi shift armada pengemudi.
 */
data class FleetDriverReport(
    val driverSummaries: List<DriverShiftSummary>,
    val totalDrivers: Int
)
