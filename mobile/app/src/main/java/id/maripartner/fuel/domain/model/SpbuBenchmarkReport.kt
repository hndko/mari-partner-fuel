package id.maripartner.fuel.domain.model

/**
 * Model item pemeringkatan performa dan efisiensi konsumsi BBM per SPBU / Merek.
 */
data class SpbuBenchmarkItem(
    val stationName: String,
    val brand: String,
    val totalRefills: Int,
    val totalLiters: Double,
    val totalSpentIdr: Long,
    val avgKmPerLiter: Double,
    val efficiencyDeltaPercent: Double,
    val ratingGrade: String
)

/**
 * Laporan komparasi indeks performa SPBU terhadap baseline efisiensi kendaraan.
 */
data class SpbuBenchmarkReport(
    val baselineKmPerLiter: Double,
    val items: List<SpbuBenchmarkItem>,
    val bestStation: SpbuBenchmarkItem? = null,
    val recommendation: String = ""
)
