package id.maripartner.fuel.domain.usecase.transaction

import id.maripartner.fuel.domain.model.DuplicateDetectionResult
import id.maripartner.fuel.domain.model.FuelTransaction
import java.time.ZoneId
import kotlin.math.abs

/**
 * UseCase untuk mendeteksi transaksi BBM ganda / duplikat (Anti-Fraud & Double Submission Guard).
 */
class DetectDuplicateTransactionUseCase {

    operator fun invoke(
        candidate: FuelTransaction,
        existingTransactions: List<FuelTransaction>
    ): DuplicateDetectionResult {
        if (candidate.totalAmountIdr <= 0L) {
            return DuplicateDetectionResult(isDuplicate = false)
        }

        val candidateDate = candidate.transactionAt.atZone(ZoneId.systemDefault()).toLocalDate()

        val matching = existingTransactions.firstOrNull { existing ->
            // Jangan cocokkan dengan transaksi itu sendiri jika sedang mode edit
            if (candidate.id.isNotBlank() && existing.id == candidate.id) {
                return@firstOrNull false
            }

            // Harus kendaraan yang sama
            if (candidate.vehicleId.isNotBlank() && existing.vehicleId != candidate.vehicleId) {
                return@firstOrNull false
            }

            // Cek nominal yang persis sama
            val sameAmount = existing.totalAmountIdr == candidate.totalAmountIdr

            // Cek volume yang sangat mirip (selisih <= 0.05 liter)
            val sameVolume = candidate.volumeLiters != null && existing.volumeLiters != null &&
                abs(candidate.volumeLiters - existing.volumeLiters) <= 0.05

            // Cek kedekatan waktu (selisih <= 3 jam) atau tanggal yang sama persis
            val diffSeconds = abs(existing.transactionAt.epochSecond - candidate.transactionAt.epochSecond)
            val existingDate = existing.transactionAt.atZone(ZoneId.systemDefault()).toLocalDate()
            val closeTime = diffSeconds <= (3 * 3600) || existingDate == candidateDate

            // Cek kesamaan SPBU jika keduanya terisi
            val stationMatch = if (!candidate.stationName.isNullOrBlank() && !existing.stationName.isNullOrBlank()) {
                candidate.stationName.trim().equals(existing.stationName.trim(), ignoreCase = true)
            } else {
                true
            }

            (sameAmount || sameVolume) && closeTime && stationMatch
        }

        return if (matching != null) {
            val stationLabel = matching.stationName?.let { "di $it" } ?: ""
            DuplicateDetectionResult(
                isDuplicate = true,
                matchingTransaction = matching,
                message = "Transaksi serupa sebesar Rp ${matching.totalAmountIdr} $stationLabel sudah tercatat pada waktu yang berdekatan. Apakah Anda yakin ingin tetap menyimpan transaksi ini?"
            )
        } else {
            DuplicateDetectionResult(isDuplicate = false)
        }
    }
}
