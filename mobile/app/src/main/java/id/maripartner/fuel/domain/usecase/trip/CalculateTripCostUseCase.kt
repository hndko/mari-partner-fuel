package id.maripartner.fuel.domain.usecase.trip

import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.domain.model.TripCalculationResult
import java.util.Locale
import kotlin.math.ceil

/**
 * Use case murni untuk menghitung estimasi kebutuhan liter bahan bakar,
 * total biaya perjalanan, emisi karbon, serta pembagian biaya patungan (split bill).
 */
class CalculateTripCostUseCase {

    /**
     * Menghitung rincian biaya perjalanan.
     */
    fun calculate(
        distanceKm: Double,
        fuelEfficiencyKmPerLiter: Double,
        fuelPricePerLiterIdr: Long,
        passengerCount: Int,
        vehicleName: String = "",
        fuelTypeName: String = ""
    ): TripCalculationResult {
        val safeEfficiency = if (fuelEfficiencyKmPerLiter > 0.0) fuelEfficiencyKmPerLiter else 12.0
        val safePassengers = passengerCount.coerceAtLeast(1)
        val safeDistance = distanceKm.coerceAtLeast(0.0)

        val requiredLiters = if (safeDistance > 0.0) safeDistance / safeEfficiency else 0.0
        val totalCost = (requiredLiters * fuelPricePerLiterIdr.toDouble()).toLong()
        val costPerPerson = if (safePassengers > 0) {
            ceil(totalCost.toDouble() / safePassengers.toDouble()).toLong()
        } else {
            totalCost
        }
        val co2 = requiredLiters * 2.31 // Emisi CO2 rata-rata ~2.31 kg per liter bensin

        return TripCalculationResult(
            distanceKm = safeDistance,
            fuelEfficiencyKmPerLiter = safeEfficiency,
            fuelPricePerLiterIdr = fuelPricePerLiterIdr,
            passengerCount = safePassengers,
            requiredLiters = requiredLiters,
            totalFuelCostIdr = totalCost,
            costPerPassengerIdr = costPerPerson,
            estimatedCo2EmissionKg = co2,
            vehicleName = vehicleName,
            fuelTypeName = fuelTypeName
        )
    }

    /**
     * Menghasilkan teks ringkasan rapi siap dibagikan ke WhatsApp, chat, atau catatan perjalanan.
     */
    fun generateShareableSummary(result: TripCalculationResult): String {
        val volStr = "%.2f".format(Locale.US, result.requiredLiters).replace('.', ',')
        val co2Str = "%.1f".format(Locale.US, result.estimatedCo2EmissionKg).replace('.', ',')
        val totalCostStr = CurrencyFormatter.formatThousands(result.totalFuelCostIdr)
        val splitCostStr = CurrencyFormatter.formatThousands(result.costPerPassengerIdr)

        return buildString {
            appendLine("🚗 *Rincian Estimasi Biaya Perjalanan & Patungan BBM*")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
            if (result.vehicleName.isNotBlank()) {
                appendLine("🚘 Kendaraan : ${result.vehicleName}")
            }
            appendLine("📍 Jarak Rute: ${result.distanceKm.toInt()} km (Efisiensi: ${result.fuelEfficiencyKmPerLiter.toInt()} km/L)")
            if (result.fuelTypeName.isNotBlank()) {
                appendLine("⛽ Jenis BBM  : ${result.fuelTypeName} (Rp ${CurrencyFormatter.formatThousands(result.fuelPricePerLiterIdr)}/L)")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("• Kebutuhan BBM  : $volStr Liter")
            appendLine("• Total Biaya BBM: Rp $totalCostStr")
            appendLine("• Jumlah Orang   : ${result.passengerCount} Penumpang")
            appendLine("👉 *Biaya Patungan*: Rp $splitCostStr / orang")
            appendLine("🌱 Estimasi Emisi: $co2Str kg CO₂")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Dihitung otomatis dengan *Mari Partner Fuel*")
        }
    }
}
