package id.maripartner.fuel.domain.model

/**
 * Tingkat keparahan anomali konsumsi bahan bakar.
 */
enum class AnomalySeverity {
    NORMAL,
    EFFICIENT,
    WARNING,
    CRITICAL
}

/**
 * Hasil diagnosa cerdas anomali konsumsi bahan bakar dan potensi kebocoran BBM.
 */
data class FuelAnomalyResult(
    val hasAnomaly: Boolean = false,
    val severity: AnomalySeverity = AnomalySeverity.NORMAL,
    val latestKmPerLiter: Double? = null,
    val baselineKmPerLiter: Double? = null,
    val percentageChange: Double = 0.0,
    val title: String = "",
    val message: String = "",
    val technicalRecommendations: List<String> = emptyList(),
    val analyzedIntervalsCount: Int = 0
)
