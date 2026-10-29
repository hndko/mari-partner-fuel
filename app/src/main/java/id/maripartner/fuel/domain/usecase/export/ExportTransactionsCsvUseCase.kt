package id.maripartner.fuel.domain.usecase.export

import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.Vehicle
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Use case untuk mengekspor riwayat transaksi BBM ke format CSV standar RFC 4180
 * dengan proteksi Spreadsheet Formula Injection (OWASP mitigation) dan enkoding UTF-8.
 */
class ExportTransactionsCsvUseCase {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val zoneId = ZoneId.systemDefault()

    /**
     * Header CSV baku sesuai spesifikasi produk Mari Partner Fuel.
     */
    val csvHeaders: List<String> = listOf(
        "Waktu Transaksi",
        "Nama Kendaraan",
        "Merek & Model",
        "Nomor Polisi",
        "Jenis BBM",
        "Volume (Liter)",
        "Harga per Liter (Rp)",
        "Total Pembayaran (Rp)",
        "SPBU / Lokasi",
        "Odometer (km)",
        "Catatan",
        "Bukti Struk"
    )

    /**
     * Menghasilkan isi berkas CSV lengkap dari daftar transaksi dan peta kendaraan.
     */
    fun exportToCsvString(
        transactions: List<FuelTransaction>,
        vehiclesMap: Map<String, Vehicle> = emptyMap()
    ): String {
        val sb = StringBuilder()

        // 1. Tuliskan UTF-8 BOM agar Excel di Windows membaca encoding dengan sempurna
        sb.append('\uFEFF')

        // 2. Baris Header
        sb.append(csvHeaders.joinToString(separator = ",", transform = ::escapeCsvField))
        sb.append("\r\n")

        // 3. Baris Data Transaksi
        for (tx in transactions) {
            val vehicle = vehiclesMap[tx.vehicleId]
            val vehicleName = tx.vehicleName ?: vehicle?.name ?: "Kendaraan"
            val makeModel = listOfNotNull(vehicle?.make, vehicle?.model).joinToString(" ").takeIf { it.isNotBlank() } ?: "-"
            val plate = tx.vehiclePlate ?: vehicle?.licensePlate ?: "-"
            val formattedDate = tx.transactionAt.atZone(zoneId).format(dateTimeFormatter)
            val volumeStr = tx.volumeLiters?.let { String.format(Locale.US, "%.3f", it) } ?: ""
            val priceStr = tx.pricePerLiterIdr?.let { String.format(Locale.US, "%.0f", it) } ?: ""
            val totalStr = tx.totalAmountIdr.toString() // Integer rupiah murni, bukan floating point
            val odoStr = tx.odometerKm?.let { String.format(Locale.US, "%.1f", it) } ?: ""
            val receiptStatus = if (tx.hasReceipt) "Ada" else "Tidak Ada"

            val row = listOf(
                formattedDate,
                vehicleName,
                makeModel,
                plate,
                tx.fuelType ?: "-",
                volumeStr,
                priceStr,
                totalStr,
                tx.stationName ?: "-",
                odoStr,
                tx.notes ?: "",
                receiptStatus
            )

            sb.append(row.joinToString(separator = ",", transform = ::escapeCsvField))
            sb.append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Escape karakter CSV RFC 4180 dan proteksi Formula Injection.
     */
    fun escapeCsvField(value: String): String {
        var sanitized = value

        // Proteksi Formula Injection: jika sel diawali karakter bahaya (=, +, -, @, tab, cr)
        // dan BUKAN angka murni positif/negatif yang sah
        if (isFormulaDangerous(sanitized)) {
            sanitized = "'$sanitized"
        }

        // RFC 4180: Jika mengandung koma, kutip ganda, atau baris baru, bungkus dengan kutip
        val needsQuotes = sanitized.contains(',') ||
                sanitized.contains('"') ||
                sanitized.contains('\n') ||
                sanitized.contains('\r') ||
                sanitized.startsWith(' ') ||
                sanitized.endsWith(' ')

        return if (needsQuotes) {
            val escapedQuotes = sanitized.replace("\"", "\"\"")
            "\"$escapedQuotes\""
        } else {
            sanitized
        }
    }

    /**
     * Memeriksa apakah teks berpotensi sebagai formula injeksi spreadsheet berbahaya.
     */
    fun isFormulaDangerous(input: String): Boolean {
        if (input.isEmpty()) return false
        val firstChar = input[0]

        // Karakter berbahaya pemicu eksekusi DDE / Formula pada spreadsheet
        val isDangerousStart = firstChar == '=' ||
                firstChar == '+' ||
                firstChar == '-' ||
                firstChar == '@' ||
                firstChar == '\t' ||
                firstChar == '\r'

        if (!isDangerousStart) return false

        // Jika diawali + atau - tapi seluruh string adalah angka murni yang sah, maka aman
        if (firstChar == '+' || firstChar == '-') {
            val isPureNumber = input.toDoubleOrNull() != null
            if (isPureNumber) {
                return false
            }
        }

        return true
    }
}
