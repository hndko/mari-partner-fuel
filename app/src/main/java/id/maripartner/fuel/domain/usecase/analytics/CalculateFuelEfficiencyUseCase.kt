package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.VehicleEfficiencySummary

/**
 * Use case murni untuk menghitung estimasi efisiensi konsumsi BBM (km/liter)
 * berbasis pergerakan odometer berurutan per kendaraan.
 */
class CalculateFuelEfficiencyUseCase {

    /**
     * Menghitung efisiensi untuk satu kendaraan berdasarkan daftar transaksi.
     */
    fun calculateForVehicle(
        vehicleId: String,
        vehicleName: String,
        transactions: List<FuelTransaction>
    ): VehicleEfficiencySummary {
        // Filter transaksi milik kendaraan ini yang memiliki odometer valid
        val validOdoTransactions = transactions
            .filter { it.vehicleId == vehicleId && it.odometerKm != null && it.odometerKm > 0 }
            .sortedWith(compareBy({ it.odometerKm }, { it.transactionAt }))

        if (validOdoTransactions.size < 2) {
            return VehicleEfficiencySummary(
                vehicleId = vehicleId,
                vehicleName = vehicleName,
                totalDistanceKm = 0.0,
                totalVolumeLiters = 0.0,
                averageKmPerLiter = null,
                validPairsCount = 0,
                isSufficientData = false,
                explanationNote = "Dibutuhkan minimal 2 pencatatan transaksi berurutan dengan odometer dan volume lengkap untuk mengestimasi efisiensi km/L."
            )
        }

        var totalValidDistance = 0.0
        var totalValidVolume = 0.0
        var validPairs = 0
        var anomalyCount = 0

        for (i in 1 until validOdoTransactions.size) {
            val prev = validOdoTransactions[i - 1]
            val curr = validOdoTransactions[i]

            val prevOdo = prev.odometerKm ?: continue
            val currOdo = curr.odometerKm ?: continue
            val currVol = curr.volumeLiters

            val deltaKm = currOdo - prevOdo

            // Abaikan atau tandai jika odometer menurun (reset atau kesalahan input)
            if (deltaKm <= 0.0) {
                anomalyCount++
                continue
            }

            // Validasi jarak realistis per pengisian (misal: maksimal 5.000 km per satu siklus tangki)
            if (deltaKm > 5000.0) {
                anomalyCount++
                continue
            }

            // Volume pada pengisian berikutnya harus ada dan valid
            if (currVol != null && currVol > 0.0) {
                totalValidDistance += deltaKm
                totalValidVolume += currVol
                validPairs++
            }
        }

        if (validPairs == 0 || totalValidVolume <= 0.0) {
            return VehicleEfficiencySummary(
                vehicleId = vehicleId,
                vehicleName = vehicleName,
                totalDistanceKm = totalValidDistance,
                totalVolumeLiters = totalValidVolume,
                averageKmPerLiter = null,
                validPairsCount = 0,
                isSufficientData = false,
                explanationNote = if (anomalyCount > 0) {
                    "Terdapat anomali angka odometer (odometer menurun atau selisih jarak tidak wajar)."
                } else {
                    "Volume bahan bakar pada transaksi berurutan belum lengkap."
                }
            )
        }

        val avgKmPerLiter = totalValidDistance / totalValidVolume

        val note = if (anomalyCount > 0) {
            "Estimasi berbasis $validPairs rentang pengisian valid ($anomalyCount anomali odometer diabaikan)."
        } else {
            "Estimasi berbasis $validPairs pengisian berurutan."
        }

        return VehicleEfficiencySummary(
            vehicleId = vehicleId,
            vehicleName = vehicleName,
            totalDistanceKm = totalValidDistance,
            totalVolumeLiters = totalValidVolume,
            averageKmPerLiter = avgKmPerLiter,
            validPairsCount = validPairs,
            isSufficientData = true,
            explanationNote = note
        )
    }

    /**
     * Menghitung efisiensi untuk seluruh kendaraan yang ada dalam kumpulan transaksi.
     */
    fun calculateAll(
        transactions: List<FuelTransaction>
    ): List<VehicleEfficiencySummary> {
        val grouped = transactions.groupBy { it.vehicleId }
        return grouped.map { (vId, txList) ->
            val vName = txList.firstOrNull()?.vehicleName ?: "Kendaraan ($vId)"
            calculateForVehicle(vId, vName, txList)
        }
    }
}
