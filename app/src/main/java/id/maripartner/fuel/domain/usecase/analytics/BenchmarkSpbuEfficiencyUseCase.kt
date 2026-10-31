package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.SpbuBenchmarkItem
import id.maripartner.fuel.domain.model.SpbuBenchmarkReport
import java.util.Locale

/**
 * UseCase untuk menganalisis dan memeringkat efisiensi konsumsi BBM per SPBU.
 * Menghitung efisiensi km/L nyata dari bahan bakar yang diisi di masing-masing SPBU.
 */
class BenchmarkSpbuEfficiencyUseCase {

    operator fun invoke(
        transactions: List<FuelTransaction>,
        baselineKmPerLiter: Double = 0.0
    ): SpbuBenchmarkReport {
        if (transactions.size < 2) {
            return SpbuBenchmarkReport(
                baselineKmPerLiter = baselineKmPerLiter,
                items = emptyList(),
                bestStation = null,
                recommendation = "Dibutuhkan minimal 2 transaksi berturut-turut dengan odometer untuk mengukur efisiensi SPBU."
            )
        }

        // Urutkan transaksi secara kronologis naik (tertua ke terbaru)
        val sorted = transactions.sortedBy { it.transactionAt }

        // Kelompokkan data per SPBU: nama SPBU di transaksi i-1 menghasilkan jarak tempuh hingga odometer di transaksi i
        data class StationData(
            val stationName: String,
            val brand: String,
            var refillCount: Int = 0,
            var totalLiters: Double = 0.0,
            var totalExpenseIdr: Long = 0L,
            val kmPerLiterList: MutableList<Double> = mutableListOf()
        )

        val stationMap = mutableMapOf<String, StationData>()

        for (i in 0 until sorted.size - 1) {
            val curr = sorted[i]
            val next = sorted[i + 1]

            val stationName = curr.stationName?.trim()?.takeIf { it.isNotBlank() } ?: "SPBU Tanpa Nama"
            val brand = detectBrand(stationName)
            val volume = curr.volumeLiters ?: 0.0
            val expense = curr.totalAmountIdr

            val entry = stationMap.getOrPut(stationName.lowercase(Locale.ROOT)) {
                StationData(stationName = stationName, brand = brand)
            }
            entry.refillCount++
            entry.totalLiters += volume
            entry.totalExpenseIdr += expense

            val currOdo = curr.odometerKm
            val nextOdo = next.odometerKm
            if (currOdo != null && nextOdo != null && nextOdo > currOdo && volume > 0.0) {
                val distance = nextOdo - currOdo
                val kmL = distance / volume
                // Filter outlier realistis (misal 3.0 s/d 80.0 km/L)
                if (kmL in 3.0..80.0) {
                    entry.kmPerLiterList.add(kmL)
                }
            }
        }

        // Hitung juga transaksi terakhir untuk volume & biaya
        val lastTx = sorted.last()
        val lastStation = lastTx.stationName?.trim()?.takeIf { it.isNotBlank() } ?: "SPBU Tanpa Nama"
        val lastEntry = stationMap.getOrPut(lastStation.lowercase(Locale.ROOT)) {
            StationData(stationName = lastStation, brand = detectBrand(lastStation))
        }
        lastEntry.refillCount++
        lastEntry.totalLiters += lastTx.volumeLiters ?: 0.0
        lastEntry.totalExpenseIdr += lastTx.totalAmountIdr

        // Hitung baseline rata-rata jika belum disuplai
        val allValidKmL = stationMap.values.flatMap { it.kmPerLiterList }
        val effectiveBaseline = if (baselineKmPerLiter > 0.0) {
            baselineKmPerLiter
        } else if (allValidKmL.isNotEmpty()) {
            allValidKmL.average()
        } else {
            0.0
        }

        val benchmarkItems = stationMap.values.map { data ->
            val avgKmL = if (data.kmPerLiterList.isNotEmpty()) {
                data.kmPerLiterList.average()
            } else {
                effectiveBaseline
            }

            val deltaPercent = if (effectiveBaseline > 0.0) {
                ((avgKmL - effectiveBaseline) / effectiveBaseline) * 100.0
            } else {
                0.0
            }

            val grade = when {
                deltaPercent >= 5.0 -> "A+"
                deltaPercent >= 0.0 -> "A"
                deltaPercent >= -5.0 -> "B"
                else -> "C"
            }

            SpbuBenchmarkItem(
                stationName = data.stationName,
                brand = data.brand,
                totalRefills = data.refillCount,
                totalLiters = data.totalLiters,
                totalSpentIdr = data.totalExpenseIdr,
                avgKmPerLiter = avgKmL,
                efficiencyDeltaPercent = deltaPercent,
                ratingGrade = grade
            )
        }.sortedByDescending { it.avgKmPerLiter }

        val best = benchmarkItems.firstOrNull { it.totalRefills >= 1 && it.avgKmPerLiter > 0 }
        val recommendation = when {
            best != null && best.efficiencyDeltaPercent > 2.0 -> {
                "SPBU ${best.stationName} memberikan jarak tempuh tertinggi (+${"%.1f".format(best.efficiencyDeltaPercent)}% di atas rata-rata armada Anda)."
            }
            best != null -> {
                "Efisiensi SPBU Anda saat ini berada dalam rentang wajar dan seimbang."
            }
            else -> {
                "Terus catat odometer pengisian untuk melihat peringkat akurasi dan efisiensi tiap SPBU."
            }
        }

        return SpbuBenchmarkReport(
            baselineKmPerLiter = effectiveBaseline,
            items = benchmarkItems,
            bestStation = best,
            recommendation = recommendation
        )
    }

    private fun detectBrand(stationName: String): String {
        val lower = stationName.lowercase(Locale.ROOT)
        return when {
            lower.contains("shell") -> "Shell"
            lower.contains("bp") || lower.contains("british petroleum") -> "BP"
            lower.contains("vivo") -> "Vivo"
            lower.contains("pertamina") || lower.contains("spbu") || lower.contains("coco") || lower.contains("dodo") -> "Pertamina"
            else -> "Lainnya"
        }
    }
}
