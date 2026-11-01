package id.maripartner.fuel.domain.model

import java.time.LocalDate

/**
 * Pilihan urutan pengurutan data riwayat transaksi.
 */
enum class SortOrder(val displayName: String) {
    NEWEST_FIRST("Terbaru"),
    OLDEST_FIRST("Terlama")
}

/**
 * Filter pencarian dan penyaringan data riwayat transaksi BBM.
 */
data class TransactionFilter(
    val query: String = "",
    val vehicleId: String? = null,
    val fuelType: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST
) {
    val isFilterActive: Boolean
        get() = query.isNotBlank() || vehicleId != null || fuelType != null || startDate != null || endDate != null
}
