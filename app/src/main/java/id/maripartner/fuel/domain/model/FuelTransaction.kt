package id.maripartner.fuel.domain.model

import java.time.Instant

/**
 * Model domain bersih untuk transaksi pengisian bahan bakar minyak (BBM).
 */
data class FuelTransaction(
    val id: String = "",
    val userId: String = "",
    val vehicleId: String,
    val vehicleName: String? = null,
    val vehiclePlate: String? = null,
    val receiptFileId: String? = null,
    val storagePath: String? = null,
    val signedReceiptUrl: String? = null,
    val transactionAt: Instant = Instant.now(),
    val stationName: String? = null,
    val fuelType: String? = null,
    val volumeLiters: Double? = null,
    val totalAmountIdr: Long,
    val pricePerLiterIdr: Double? = null,
    val odometerKm: Double? = null,
    val scanStatus: String = "manual",
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    /**
     * Memeriksa apakah transaksi memiliki bukti struk terlampir.
     */
    val hasReceipt: Boolean
        get() = !receiptFileId.isNullOrBlank() || !storagePath.isNullOrBlank()

    /**
     * Menghitung efisiensi estimasi harga per liter jika tidak terisi.
     */
    val effectivePricePerLiter: Double?
        get() {
            if (pricePerLiterIdr != null && pricePerLiterIdr > 0) return pricePerLiterIdr
            if (volumeLiters != null && volumeLiters > 0 && totalAmountIdr > 0) {
                return totalAmountIdr.toDouble() / volumeLiters
            }
            return null
        }
}
