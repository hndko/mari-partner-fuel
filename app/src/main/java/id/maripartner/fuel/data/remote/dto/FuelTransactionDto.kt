package id.maripartner.fuel.data.remote.dto

import id.maripartner.fuel.domain.model.FuelTransaction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Data Transfer Object untuk tabel 'fuel_transactions' di Supabase.
 */
@Serializable
data class FuelTransactionDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("vehicle_id") val vehicleId: String,
    @SerialName("receipt_file_id") val receiptFileId: String? = null,
    @SerialName("transaction_at") val transactionAt: String,
    @SerialName("station_name") val stationName: String? = null,
    @SerialName("fuel_type") val fuelType: String? = null,
    @SerialName("volume_liters") val volumeLiters: Double? = null,
    @SerialName("total_amount_idr") val totalAmountIdr: Long,
    @SerialName("price_per_liter_idr") val pricePerLiterIdr: Double? = null,
    @SerialName("odometer_km") val odometerKm: Double? = null,
    @SerialName("scan_status") val scanStatus: String = "manual",
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // Relasi embedded dari Postgrest (opsional saat select)
    val vehicles: VehicleDto? = null,
    @SerialName("receipt_files") val receiptFiles: ReceiptFileDto? = null
) {
    fun toDomain(signedReceiptUrl: String? = null): FuelTransaction {
        return FuelTransaction(
            id = id.orEmpty(),
            userId = userId.orEmpty(),
            vehicleId = vehicleId,
            vehicleName = vehicles?.name,
            vehiclePlate = vehicles?.licensePlate,
            receiptFileId = receiptFileId,
            storagePath = receiptFiles?.storagePath,
            signedReceiptUrl = signedReceiptUrl,
            transactionAt = parseInstant(transactionAt),
            stationName = stationName,
            fuelType = fuelType,
            volumeLiters = volumeLiters,
            totalAmountIdr = totalAmountIdr,
            pricePerLiterIdr = pricePerLiterIdr,
            odometerKm = odometerKm,
            scanStatus = scanStatus,
            notes = notes,
            createdAt = parseInstant(createdAt),
            updatedAt = parseInstant(updatedAt)
        )
    }

    companion object {
        fun fromDomain(transaction: FuelTransaction, userId: String? = null): FuelTransactionDto {
            return FuelTransactionDto(
                id = transaction.id.ifBlank { null },
                userId = userId ?: transaction.userId.ifBlank { null },
                vehicleId = transaction.vehicleId,
                receiptFileId = transaction.receiptFileId?.ifBlank { null },
                transactionAt = transaction.transactionAt.toString(),
                stationName = transaction.stationName?.trim()?.takeIf { it.isNotBlank() },
                fuelType = transaction.fuelType?.trim()?.takeIf { it.isNotBlank() },
                volumeLiters = transaction.volumeLiters,
                totalAmountIdr = transaction.totalAmountIdr,
                pricePerLiterIdr = transaction.pricePerLiterIdr,
                odometerKm = transaction.odometerKm,
                scanStatus = transaction.scanStatus,
                notes = transaction.notes?.trim()?.takeIf { it.isNotBlank() }
            )
        }

        private fun parseInstant(str: String?): Instant {
            if (str.isNullOrBlank()) return Instant.now()
            return try {
                Instant.parse(str)
            } catch (_: Exception) {
                Instant.now()
            }
        }
    }
}
