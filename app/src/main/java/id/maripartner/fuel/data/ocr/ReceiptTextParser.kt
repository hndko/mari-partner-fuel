package id.maripartner.fuel.data.ocr

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Tingkat keyakinan hasil ekstraksi data OCR.
 */
enum class FieldConfidence {
    HIGH,
    LOW,
    MISSING
}

/**
 * Nilai kandidat hasil ekstraksi beserta tingkat keyakinan dan label aslinya.
 */
data class ExtractedField<T>(
    val value: T?,
    val confidence: FieldConfidence = if (value != null) FieldConfidence.HIGH else FieldConfidence.MISSING,
    val rawMatchedText: String? = null
)

/**
 * Hasil ekstraksi terstruktur dari teks struk SPBU.
 */
data class ParsedReceiptData(
    val rawText: String,
    val totalAmountIdr: ExtractedField<Long>,
    val volumeLiters: ExtractedField<Double>,
    val pricePerLiterIdr: ExtractedField<Double>,
    val fuelType: ExtractedField<String>,
    val stationName: ExtractedField<String>,
    val transactionDate: ExtractedField<LocalDate>,
    val transactionTime: ExtractedField<LocalTime>,
    val receiptNumber: ExtractedField<String>,
    val warnings: List<String> = emptyList(),
    val needsReview: Boolean = true
)

/**
 * Parser berbasis aturan (Rule-Based Regex) murni Kotlin untuk ekstraksi data struk SPBU Indonesia.
 * Tidak memiliki ketergantungan pada Android Framework sehingga dapat diuji cepat pada JVM.
 */
class ReceiptTextParser {

    companion object {
        private val FUEL_KEYWORDS = listOf(
            "PERTALITE", "PERTAMAX TURBO", "PERTAMAX RACING", "PERTAMAX",
            "PERTAMINA DEX", "DEXLITE", "SOLAR", "BIO SOLAR", "BIOSOLAR",
            "SHELL SUPER", "SHELL V-POWER DIESEL", "SHELL V-POWER NITRO+", "SHELL V-POWER", "SHELL DIESEL",
            "BP 90", "BP 92", "BP 95", "BP ULTIMATE", "BP DIESEL",
            "REVVO 90", "REVVO 92", "REVVO 95"
        )

        private val TOTAL_LABELS = listOf(
            "TOTAL BAYAR", "TOTAL HARGA", "HARGA TOTAL", "TOTAL RUPIAH", "TOTAL RP",
            "TOTAL PENJUALAN", "JUMLAH RUPIAH", "JUMLAH RP", "TOTAL", "JUMLAH", "BAYAR"
        )

        private val CASH_CHANGE_LABELS = listOf(
            "TUNAI", "CASH", "KEMBALI", "KEMBALIAN", "CHANGE", "KARTU", "DEBIT", "QRIS"
        )

        private val VOLUME_LABELS = listOf(
            "VOLUME", "VOL", "LITER", "LTR", "QTY", "JUMLAH LITER"
        )

        private val PRICE_PER_LITER_LABELS = listOf(
            "HARGA/LITER", "HARGA/LTR", "HARGA / LTR", "HARGA/L", "HARGA / L", "@RP", "@/L", "@"
        )
    }

    fun parse(rawText: String): ParsedReceiptData {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val totalField = extractTotalAmount(lines)
        val volumeField = extractVolume(lines)
        val priceField = extractPricePerLiter(lines)
        val fuelField = extractFuelType(lines)
        val stationField = extractStationName(lines)
        val dateField = extractDate(lines)
        val timeField = extractTime(lines)
        val receiptNumberField = extractReceiptNumber(lines)

        val warnings = mutableListOf<String>()

        // Pemeriksaan konsistensi volume x harga vs total pembayaran
        if (totalField.value != null && volumeField.value != null && priceField.value != null) {
            val expectedTotal = (volumeField.value * priceField.value).roundToLong()
            val diff = abs(totalField.value - expectedTotal)
            if (diff > totalField.value * 0.05 && diff > 1000) {
                warnings.add("Perhitungan volume x harga (${expectedTotal}) berselisih Rp ${diff} dari total pembayaran.")
            }
        }

        if (totalField.value == null) {
            warnings.add("Total pembayaran tidak terdeteksi secara pasti. Silakan periksa kembali.")
        }

        if (volumeField.value == null && priceField.value == null) {
            warnings.add("Volume BBM tidak ditemukan pada struk.")
        }

        return ParsedReceiptData(
            rawText = rawText,
            totalAmountIdr = totalField,
            volumeLiters = volumeField,
            pricePerLiterIdr = priceField,
            fuelType = fuelField,
            stationName = stationField,
            transactionDate = dateField,
            transactionTime = timeField,
            receiptNumber = receiptNumberField,
            warnings = warnings,
            needsReview = true
        )
    }

    private fun extractTotalAmount(lines: List<String>): ExtractedField<Long> {
        // Prioritas 1: Baris yang mengandung label TOTAL atau JUMLAH
        for (line in lines) {
            val upper = line.uppercase()
            // Abaikan jika baris tunai/kembalian
            if (CASH_CHANGE_LABELS.any { upper.startsWith(it) }) continue

            for (label in TOTAL_LABELS) {
                if (upper.contains(label)) {
                    val candidate = parseAmountFromLine(line, label)
                    if (candidate != null && candidate > 0) {
                        return ExtractedField(candidate, FieldConfidence.HIGH, line)
                    }
                }
            }
        }

        // Prioritas 2: Cari label Rp diikuti angka nominal yang masuk akal (> 1000)
        val rpRegex = Regex("""(?:RP|TOTAL)\.?\s*[:=]?\s*([0-9.,]+)""", RegexOption.IGNORE_CASE)
        for (line in lines) {
            val upper = line.uppercase()
            if (CASH_CHANGE_LABELS.any { upper.contains(it) }) continue

            val match = rpRegex.find(line)
            if (match != null) {
                val numStr = cleanNumberString(match.groupValues[1])
                val num = numStr.toLongOrNull()
                if (num != null && num in 5000..5000000) {
                    return ExtractedField(num, FieldConfidence.LOW, line)
                }
            }
        }

        return ExtractedField(null, FieldConfidence.MISSING)
    }

    private fun extractVolume(lines: List<String>): ExtractedField<Double> {
        val volumeRegex = Regex("""([0-9]+[.,][0-9]{1,3})\s*(?:L|LTR|LITER)""", RegexOption.IGNORE_CASE)

        for (line in lines) {
            // Cek pola: "12.50 L" atau "3,450 Ltr"
            val match = volumeRegex.find(line)
            if (match != null) {
                val parsed = parseDecimal(match.groupValues[1])
                if (parsed != null && parsed in 0.5..300.0) {
                    return ExtractedField(parsed, FieldConfidence.HIGH, line)
                }
            }

            // Cek baris dengan label VOLUME atau LITER
            val upper = line.uppercase()
            if (VOLUME_LABELS.any { upper.contains(it) }) {
                val decimalMatch = Regex("""([0-9]+[.,][0-9]{1,3})""").find(line)
                if (decimalMatch != null) {
                    val parsed = parseDecimal(decimalMatch.groupValues[1])
                    if (parsed != null && parsed in 0.5..300.0) {
                        return ExtractedField(parsed, FieldConfidence.HIGH, line)
                    }
                }
            }
        }

        return ExtractedField(null, FieldConfidence.MISSING)
    }

    private fun extractPricePerLiter(lines: List<String>): ExtractedField<Double> {
        for (line in lines) {
            val upper = line.uppercase()
            for (label in PRICE_PER_LITER_LABELS) {
                if (upper.contains(label)) {
                    val numbers = Regex("""[0-9]+(?:[.,][0-9]+)*""").findAll(line)
                    for (m in numbers) {
                        val clean = cleanNumberString(m.value)
                        val num = clean.toDoubleOrNull()
                        // Rentang harga per liter BBM Indonesia (Rp 6.000 - Rp 35.000)
                        if (num != null && num in 6000.0..35000.0) {
                            return ExtractedField(num, FieldConfidence.HIGH, line)
                        }
                    }
                }
            }
        }
        return ExtractedField(null, FieldConfidence.MISSING)
    }

    private fun extractFuelType(lines: List<String>): ExtractedField<String> {
        for (line in lines) {
            val upper = line.uppercase()
            for (fuel in FUEL_KEYWORDS) {
                if (upper.contains(fuel)) {
                    // Normalisasi nama BBM
                    val formatted = fuel.split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }
                    return ExtractedField(formatted, FieldConfidence.HIGH, line)
                }
            }
        }
        return ExtractedField(null, FieldConfidence.MISSING)
    }

    private fun extractStationName(lines: List<String>): ExtractedField<String> {
        val spbuRegex = Regex("""(?:SPBU|PERTAMINA|SHELL|BP|VIVO)\s*(?:NO\.?|KODE)?\s*([0-9]{2}\.[0-9]{3,5}|[A-Za-z0-9\s-]+)""", RegexOption.IGNORE_CASE)

        for (line in lines.take(6)) { // Biasanya SPBU berada di 6 baris teratas struk
            val match = spbuRegex.find(line)
            if (match != null) {
                val name = line.replace(Regex("""[^A-Za-z0-9\s.-]"""), "").trim()
                if (name.length >= 4) {
                    return ExtractedField(name, FieldConfidence.HIGH, line)
                }
            }
        }
        return ExtractedField(null, FieldConfidence.MISSING)
    }

    private fun extractDate(lines: List<String>): ExtractedField<LocalDate> {
        val datePatterns = listOf(
            Regex("""\b([0-3]?[0-9])[/-]([0-1]?[0-9])[/-](20[2-3][0-9])\b"""), // DD/MM/YYYY
            Regex("""\b(20[2-3][0-9])[/-]([0-1]?[0-9])[/-]([0-3]?[0-9])\b""")  // YYYY-MM-DD
        )

        for (line in lines) {
            for (pattern in datePatterns) {
                val match = pattern.find(line)
                if (match != null) {
                    try {
                        val g = match.groupValues
                        val date = if (g[3].length == 4) {
                            // Format DD/MM/YYYY
                            LocalDate.of(g[3].toInt(), g[2].toInt(), g[1].toInt())
                        } else {
                            // Format YYYY-MM-DD
                            LocalDate.of(g[1].toInt(), g[2].toInt(), g[3].toInt())
                        }
                        return ExtractedField(date, FieldConfidence.HIGH, line)
                    } catch (_: Exception) { }
                }
            }
        }
        return ExtractedField(null, FieldConfidence.MISSING)
    }

    private fun extractTime(lines: List<String>): ExtractedField<LocalTime> {
        val timeRegex = Regex("""\b([0-2]?[0-9])[:.]([0-5][0-9])(?:[:.]([0-5][0-9]))?\b""")

        for (line in lines) {
            val match = timeRegex.find(line)
            if (match != null) {
                try {
                    val hour = match.groupValues[1].toInt()
                    val minute = match.groupValues[2].toInt()
                    if (hour in 0..23 && minute in 0..59) {
                        return ExtractedField(LocalTime.of(hour, minute), FieldConfidence.HIGH, line)
                    }
                } catch (_: Exception) { }
            }
        }
        return ExtractedField(null, FieldConfidence.MISSING)
    }

    private fun extractReceiptNumber(lines: List<String>): ExtractedField<String> {
        val receiptRegex = Regex("""(?:NO\.?\s*(?:STRUK|TRX|REF|TRANS)?|RECEIPT\s*#?)\s*[:=]?\s*([A-Za-z0-9-]+)""", RegexOption.IGNORE_CASE)

        for (line in lines) {
            val match = receiptRegex.find(line)
            if (match != null) {
                val num = match.groupValues[1].trim()
                if (num.length >= 3) {
                    return ExtractedField(num, FieldConfidence.HIGH, line)
                }
            }
        }
        return ExtractedField(null, FieldConfidence.MISSING)
    }

    private fun parseAmountFromLine(line: String, label: String): Long? {
        val afterLabel = line.substring(line.uppercase().indexOf(label) + label.length)
        val numberMatches = Regex("""[0-9]+(?:[.,][0-9]+)*""").findAll(afterLabel)

        for (m in numberMatches) {
            val cleaned = cleanNumberString(m.value)
            val num = cleaned.toLongOrNull()
            if (num != null && num in 1000..50000000) {
                return num
            }
        }
        return null
    }

    private fun cleanNumberString(raw: String): String {
        var str = raw.trim().replace("Rp", "", ignoreCase = true).trim()
        if (str.endsWith(",00") || str.endsWith(".00")) {
            str = str.substring(0, str.length - 3)
        }
        return str.replace(".", "").replace(",", "")
    }

    private fun parseDecimal(raw: String): Double? {
        val clean = raw.trim().replace(',', '.')
        return clean.toDoubleOrNull()
    }
}
