package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.domain.model.EcoDriverReport
import id.maripartner.fuel.domain.model.FuelTransaction
import java.time.Instant
import kotlin.math.roundToInt
import kotlin.random.Random

class GenerateEcoScoreUseCase {

    operator fun invoke(
        driverName: String,
        vehicleName: String,
        vehicleType: String,
        transactions: List<FuelTransaction>
    ): EcoDriverReport {
        val benchmarkKmL = when (vehicleType.lowercase()) {
            "motor", "motorcycle", "sepeda motor" -> 45.0
            "truk", "truck", "bus" -> 6.0
            else -> 12.0 // Mobil penumpang standar
        }

        val totalLiters = transactions.sumOf { it.volumeLiters ?: 0.0 }
        val sortedByOdo = transactions.mapNotNull { it.odometerKm }.sorted()
        val totalDistanceKm = if (sortedByOdo.size >= 2) {
            (sortedByOdo.last() - sortedByOdo.first()).coerceAtLeast(1.0)
        } else {
            (totalLiters * benchmarkKmL).coerceAtLeast(1.0)
        }

        val actualKmL = if (totalLiters > 0) totalDistanceKm / totalLiters else benchmarkKmL
        val ratio = actualKmL / benchmarkKmL

        // Skor 0 - 100 (jika pas dengan benchmark = 75, di atasnya mendekati 100)
        val score = (ratio * 75.0).roundToInt().coerceIn(10, 100)

        val badge = when {
            score >= 90 -> "Eco Master Legend"
            score >= 80 -> "Green Elite Cruiser"
            score >= 70 -> "Balanced Eco Driver"
            score >= 50 -> "Active Commuter"
            else -> "Fuel Enthusiast"
        }

        // Emisi CO2 yang berhasil dihemat (kg CO2 = liter terhemat * 2.31)
        val benchmarkLitersExpected = totalDistanceKm / benchmarkKmL
        val litersSaved = (benchmarkLitersExpected - totalLiters).coerceAtLeast(0.0)
        val co2SavedKg = (litersSaved * 2.31 * 10.0).roundToInt() / 10.0

        val certHash = (Random.nextInt(1000, 9999))
        val certId = "ECO-2026-MPF-$certHash"
        val issueDate = DateUtils.formatFullDate(Instant.now())

        return EcoDriverReport(
            certificateId = certId,
            driverName = driverName.ifBlank { "Pengemudi Mari Partner Fuel" },
            vehicleName = vehicleName.ifBlank { "Kendaraan Utama" },
            vehicleType = vehicleType,
            averageKmL = (actualKmL * 10.0).roundToInt() / 10.0,
            benchmarkKmL = benchmarkKmL,
            ecoScore = score,
            badgeTitle = badge,
            co2SavedKg = co2SavedKg,
            totalDistanceKm = totalDistanceKm,
            totalLitersUsed = totalLiters,
            issueDate = issueDate
        )
    }
}
