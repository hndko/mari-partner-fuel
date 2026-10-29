package id.maripartner.fuel.domain.usecase.transaction

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Hasil pengecekan kesesuaian antara total pembayaran, volume, dan harga per liter.
 */
data class DiscrepancyResult(
    val hasDiscrepancy: Boolean,
    val expectedTotalIdr: Long? = null,
    val differenceIdr: Long = 0L,
    val message: String? = null
)

/**
 * Use case untuk perhitungan estimasi BBM dan verifikasi selisih nominal.
 */
class EstimateFuelCalculationUseCase {

    /**
     * Menghitung harga per liter dari total pembayaran dan volume liter.
     */
    fun calculatePricePerLiter(totalAmountIdr: Long, volumeLiters: Double): Double? {
        if (totalAmountIdr <= 0 || volumeLiters <= 0.0) return null
        return totalAmountIdr.toDouble() / volumeLiters
    }

    /**
     * Menghitung volume liter dari total pembayaran dan harga per liter.
     */
    fun calculateVolumeLiters(totalAmountIdr: Long, pricePerLiterIdr: Double): Double? {
        if (totalAmountIdr <= 0 || pricePerLiterIdr <= 0.0) return null
        return totalAmountIdr.toDouble() / pricePerLiterIdr
    }

    /**
     * Menghitung estimasi total pembayaran dari volume liter dan harga per liter.
     */
    fun calculateExpectedTotal(volumeLiters: Double, pricePerLiterIdr: Double): Long {
        if (volumeLiters <= 0.0 || pricePerLiterIdr <= 0.0) return 0L
        return (volumeLiters * pricePerLiterIdr).roundToLong()
    }

    /**
     * Memeriksa selisih antara total pembayaran dengan hasil perkalian (volume * harga/liter).
     * Menggunakan batas toleransi wajar (default 5% dari total).
     */
    fun checkDiscrepancy(
        totalAmountIdr: Long,
        volumeLiters: Double?,
        pricePerLiterIdr: Double?,
        toleranceRatio: Double = 0.05
    ): DiscrepancyResult {
        if (totalAmountIdr <= 0 || volumeLiters == null || volumeLiters <= 0.0 || pricePerLiterIdr == null || pricePerLiterIdr <= 0.0) {
            return DiscrepancyResult(hasDiscrepancy = false)
        }

        val expectedTotal = calculateExpectedTotal(volumeLiters, pricePerLiterIdr)
        val diff = abs(totalAmountIdr - expectedTotal)
        val maxAllowedDiff = (totalAmountIdr * toleranceRatio).roundToLong()

        return if (diff > maxAllowedDiff && diff > 1000L) {
            DiscrepancyResult(
                hasDiscrepancy = true,
                expectedTotalIdr = expectedTotal,
                differenceIdr = diff,
                message = "Perhitungan volume x harga (${expectedTotal}) berselisih Rp ${diff} dari total pembayaran."
            )
        } else {
            DiscrepancyResult(
                hasDiscrepancy = false,
                expectedTotalIdr = expectedTotal,
                differenceIdr = diff
            )
        }
    }
}
