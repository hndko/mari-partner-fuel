package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.DriverShiftSummary
import id.maripartner.fuel.domain.model.FleetDriverReport
import id.maripartner.fuel.domain.model.FuelTransaction
import java.util.regex.Pattern

/**
 * UseCase untuk mengekstrak dan merekapitulasi logbook per pengemudi / sopir armada (Fleet Shift Logbook).
 */
class GetFleetDriverReportUseCase {

    private val driverPattern = Pattern.compile("\\[(?:Driver|Sopir|Pengemudi):\\s*([^\\]]+)\\]", Pattern.CASE_INSENSITIVE)

    operator fun invoke(transactions: List<FuelTransaction>): FleetDriverReport {
        if (transactions.isEmpty()) {
            return FleetDriverReport(driverSummaries = emptyList(), totalDrivers = 0)
        }

        data class DriverStats(
            val name: String,
            var count: Int = 0,
            var liters: Double = 0.0,
            var expense: Long = 0L,
            val efficiencies: MutableList<Double> = mutableListOf()
        )

        val map = mutableMapOf<String, DriverStats>()

        // Urutkan transaksi untuk kalkulasi sequential
        val sorted = transactions.sortedBy { it.transactionAt }

        for (i in sorted.indices) {
            val tx = sorted[i]
            val driverName = extractDriverName(tx.notes) ?: "Pengemudi Utama"

            val stats = map.getOrPut(driverName) { DriverStats(name = driverName) }
            stats.count++
            stats.liters += (tx.volumeLiters ?: 0.0)
            stats.expense += tx.totalAmountIdr

            if (i > 0) {
                val prev = sorted[i - 1]
                val prevOdo = prev.odometerKm
                val currOdo = tx.odometerKm
                val vol = prev.volumeLiters ?: 0.0
                if (prevOdo != null && currOdo != null && currOdo > prevOdo && vol > 0.0) {
                    val kmL = (currOdo - prevOdo) / vol
                    if (kmL in 3.0..80.0) {
                        stats.efficiencies.add(kmL)
                    }
                }
            }
        }

        val summaries = map.values.map { s ->
            val avgKmL = if (s.efficiencies.isNotEmpty()) s.efficiencies.average() else null
            DriverShiftSummary(
                driverName = s.name,
                totalTransactions = s.count,
                totalVolumeLiters = s.liters,
                totalExpenseIdr = s.expense,
                avgEfficiencyKmPerLiter = avgKmL
            )
        }.sortedByDescending { it.totalExpenseIdr }

        return FleetDriverReport(
            driverSummaries = summaries,
            totalDrivers = summaries.size
        )
    }

    fun extractDriverName(notes: String?): String? {
        if (notes.isNullOrBlank()) return null
        val matcher = driverPattern.matcher(notes)
        return if (matcher.find()) {
            matcher.group(1)?.trim()?.takeIf { it.isNotBlank() }
        } else {
            null
        }
    }

    fun formatDriverTag(driverName: String, originalNotes: String?): String {
        val trimmedDriver = driverName.trim()
        val cleanNotes = originalNotes?.let {
            driverPattern.matcher(it).replaceAll("").trim()
        }.orEmpty()

        return if (trimmedDriver.isNotBlank()) {
            if (cleanNotes.isNotBlank()) {
                "[Driver: $trimmedDriver] $cleanNotes"
            } else {
                "[Driver: $trimmedDriver]"
            }
        } else {
            cleanNotes
        }
    }
}
