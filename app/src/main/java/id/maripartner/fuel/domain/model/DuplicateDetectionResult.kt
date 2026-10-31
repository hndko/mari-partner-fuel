package id.maripartner.fuel.domain.model

/**
 * Hasil deteksi duplikasi transaksi BBM untuk mencegah fraud / double submission.
 */
data class DuplicateDetectionResult(
    val isDuplicate: Boolean,
    val matchingTransaction: FuelTransaction? = null,
    val message: String? = null
)
