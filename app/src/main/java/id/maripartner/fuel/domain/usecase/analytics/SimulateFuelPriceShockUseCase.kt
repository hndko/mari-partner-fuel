package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.FuelShockSimulationResult
import kotlin.math.roundToLong

/**
 * UseCase untuk memproyeksikan dampak finansial kenaikan harga BBM (What-If Shock Simulator).
 */
class SimulateFuelPriceShockUseCase {

    operator fun invoke(
        monthlyLiters: Double,
        currentMonthlyCostIdr: Long,
        priceHikePerLiter: Double,
        avgCostPerKmIdr: Double = 0.0
    ): FuelShockSimulationResult {
        val safeLiters = if (monthlyLiters > 0.0) monthlyLiters else 50.0 // Default 50 Liter jika riwayat baru
        val safeMonthlyCost = if (currentMonthlyCostIdr > 0L) currentMonthlyCostIdr else (safeLiters * 10000.0).roundToLong()

        val monthlyImpact = (safeLiters * priceHikePerLiter).roundToLong()
        val projectedMonthly = safeMonthlyCost + monthlyImpact
        val yearlyImpact = monthlyImpact * 12L

        val percentIncrease = if (safeMonthlyCost > 0L) {
            (monthlyImpact.toDouble() / safeMonthlyCost.toDouble()) * 100.0
        } else {
            0.0
        }

        val safeCostPerKm = if (avgCostPerKmIdr > 0.0) avgCostPerKmIdr else 750.0 // estimasi standar Rp 750/km
        val recommendedKmReduction = monthlyImpact.toDouble() / safeCostPerKm

        val tips = mutableListOf<String>()
        if (priceHikePerLiter >= 2000.0) {
            tips.add("Kenaikan signifikan: pertimbangkan carpooling atau gabungkan rute perjalanan mingguan.")
            tips.add("Periksa tekanan angin ban berkala: tekanan kurang 2-3 PSI meningkatkan konsumsi BBM hingga 5%.")
        } else if (priceHikePerLiter >= 1000.0) {
            tips.add("Hindari akselerasi mendadak dan rem berlebih (Eco-Driving) untuk menghemat 10-15% bensin.")
            tips.add("Kurangi membawa barang bawaan berat yang tidak diperlukan di bagasi kendaraan.")
        } else {
            tips.add("Dampak kenaikan tergolong ringan, dapat dimitigasi dengan pengisian di SPBU berharga promo.")
        }

        if (recommendedKmReduction > 10.0) {
            tips.add("Target hemat: kurangi jarak perjalanan non-esensial sekitar ${"%.0f".format(recommendedKmReduction)} km/bulan untuk menjaga anggaran tetap stabil.")
        }

        return FuelShockSimulationResult(
            priceHikePerLiter = priceHikePerLiter,
            currentMonthlyLiters = safeLiters,
            currentMonthlyCostIdr = safeMonthlyCost,
            projectedMonthlyCostIdr = projectedMonthly,
            monthlyImpactIdr = monthlyImpact,
            yearlyImpactIdr = yearlyImpact,
            percentIncrease = percentIncrease,
            recommendedKmReduction = recommendedKmReduction,
            actionableTips = tips
        )
    }
}
