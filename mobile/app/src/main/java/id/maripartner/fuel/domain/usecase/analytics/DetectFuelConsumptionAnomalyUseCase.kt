package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.AnomalySeverity
import id.maripartner.fuel.domain.model.FuelAnomalyResult
import id.maripartner.fuel.domain.model.FuelTransaction
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Use case cerdas untuk mendeteksi deviasi/anomali konsumsi bahan bakar (km/L)
 * dan indikasi dini kebocoran atau kerusakan mekanis kendaraan tanpa API berbayar.
 */
class DetectFuelConsumptionAnomalyUseCase {

    /**
     * Menganalisis riwayat transaksi pengisian BBM untuk mendeteksi anomali konsumsi.
     */
    fun detectAnomaly(
        vehicleId: String,
        transactions: List<FuelTransaction>
    ): FuelAnomalyResult {
        // Ambil transaksi berurutan kendaraan ini dengan odometer dan volume valid
        val validOdoTxs = transactions
            .filter { it.vehicleId == vehicleId && it.odometerKm != null && it.odometerKm > 0 }
            .sortedWith(compareBy({ it.odometerKm }, { it.transactionAt }))

        if (validOdoTxs.size < 3) {
            // Butuh minimal 3 transaksi (2 interval berurutan)
            return FuelAnomalyResult(
                hasAnomaly = false,
                severity = AnomalySeverity.NORMAL,
                title = "Data Belum Memadai",
                message = "Catat minimal 3 pengisian BBM berurutan dengan odometer lengkap untuk membuka deteksi anomali konsumsi.",
                analyzedIntervalsCount = 0
            )
        }

        val intervals = mutableListOf<Double>()
        for (i in 1 until validOdoTxs.size) {
            val prev = validOdoTxs[i - 1]
            val curr = validOdoTxs[i]
            val deltaKm = (curr.odometerKm ?: 0.0) - (prev.odometerKm ?: 0.0)
            val vol = curr.volumeLiters ?: 0.0
            if (deltaKm > 0.0 && deltaKm <= 5000.0 && vol > 0.0) {
                intervals.add(deltaKm / vol)
            }
        }

        if (intervals.size < 2) {
            return FuelAnomalyResult(
                hasAnomaly = false,
                severity = AnomalySeverity.NORMAL,
                title = "Interval Belum Cukup",
                message = "Memerlukan minimal 2 rentang pengisian BBM dengan volume valid untuk mengukur anomali.",
                analyzedIntervalsCount = intervals.size
            )
        }

        val latestKmL = intervals.last()
        // Baseline dihitung dari rata-rata interval sebelum pengisian terakhir
        val baselineIntervals = intervals.dropLast(1)
        val baselineKmL = baselineIntervals.average()

        if (baselineKmL <= 0.0) {
            return FuelAnomalyResult(hasAnomaly = false, severity = AnomalySeverity.NORMAL)
        }

        // Persentase perubahan: ((latest - baseline) / baseline) * 100
        val changePct = ((latestKmL - baselineKmL) / baselineKmL) * 100.0
        val absDrop = abs(changePct).roundToInt()

        val latestFormatted = "%.1f".format(Locale.US, latestKmL).replace('.', ',')
        val baselineFormatted = "%.1f".format(Locale.US, baselineKmL).replace('.', ',')

        return when {
            changePct <= -35.0 -> {
                // Drop parah > 35%: Anomali Kritis (Potensi Kebocoran / Injektor Rusak)
                FuelAnomalyResult(
                    hasAnomaly = true,
                    severity = AnomalySeverity.CRITICAL,
                    latestKmPerLiter = latestKmL,
                    baselineKmPerLiter = baselineKmL,
                    percentageChange = changePct,
                    title = "Anomali Kritis: Efisiensi Anjlok $absDrop%!",
                    message = "Konsumsi BBM turun drastis dari rata-rata $baselineFormatted km/L menjadi $latestFormatted km/L. Terdapat indikasi anomali mekanis serius.",
                    technicalRecommendations = listOf(
                        "Periksa apakah ada rembesan atau aroma bensin menyengat di sekitar tangki dan saluran bahan bakar.",
                        "Cek kondisi injektor/karburator, busi, dan saringan pompa bensin (fuel pump).",
                        "Pastikan angka odometer pada pencatatan struk terakhir sudah dimasukkan dengan benar."
                    ),
                    analyzedIntervalsCount = intervals.size
                )
            }
            changePct <= -20.0 -> {
                // Drop 20% - 35%: Warning (Tekanan ban / Oli jenuh / Stop and go)
                FuelAnomalyResult(
                    hasAnomaly = true,
                    severity = AnomalySeverity.WARNING,
                    latestKmPerLiter = latestKmL,
                    baselineKmPerLiter = baselineKmL,
                    percentageChange = changePct,
                    title = "Peringatan: Pemborosan BBM $absDrop%",
                    message = "Efisiensi pengisian terakhir ($latestFormatted km/L) $absDrop% lebih boros dibanding kebiasaan kendaraan ($baselineFormatted km/L).",
                    technicalRecommendations = listOf(
                        "Periksa tekanan angin ban (ban kempes 20% meningkatkan hambatan gulir hingga 5%).",
                        "Periksa jadwal penggantian oli mesin dan filter udara yang berdebu.",
                        "Kurangi akselerasi mendadak di jalur macet atau stop-and-go."
                    ),
                    analyzedIntervalsCount = intervals.size
                )
            }
            changePct >= 20.0 -> {
                // Efisiensi meningkat >= 20%: Eco-driving
                val absGain = changePct.roundToInt()
                FuelAnomalyResult(
                    hasAnomaly = true,
                    severity = AnomalySeverity.EFFICIENT,
                    latestKmPerLiter = latestKmL,
                    baselineKmPerLiter = baselineKmL,
                    percentageChange = changePct,
                    title = "Konsumsi Sangat Efisien (+$absGain%)",
                    message = "Luar biasa! Efisiensi pengisian terakhir mencapai $latestFormatted km/L, meningkat $absGain% dibanding rata-rata kebiasaan ($baselineFormatted km/L).",
                    technicalRecommendations = listOf(
                        "Pertahankan teknik kemudi halus dan menjaga putaran mesin konstan (eco-driving).",
                        "Rute bebas hambatan atau jalan tol terbukti memangkas pemborosan bahan bakar secara optimal."
                    ),
                    analyzedIntervalsCount = intervals.size
                )
            }
            else -> {
                // Variasi Normal
                FuelAnomalyResult(
                    hasAnomaly = false,
                    severity = AnomalySeverity.NORMAL,
                    latestKmPerLiter = latestKmL,
                    baselineKmPerLiter = baselineKmL,
                    percentageChange = changePct,
                    title = "Konsumsi BBM Stabil",
                    message = "Efisiensi konsumsi BBM kendaraan Anda berada dalam rentang normal ($latestFormatted km/L).",
                    analyzedIntervalsCount = intervals.size
                )
            }
        }
    }
}
