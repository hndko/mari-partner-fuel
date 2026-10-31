package id.maripartner.fuel.core.common.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utilitas pemformatan mata uang Rupiah dan volume BBM.
 */
object CurrencyFormatter {

    private val indonesianSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val rupiahFormat = DecimalFormat("#,###", indonesianSymbols)
    private val literFormat = DecimalFormat("#,##0.000", indonesianSymbols)
    private val compactLiterFormat = DecimalFormat("#,##0.##", indonesianSymbols)

    /**
     * Memformat nilai integer ke format Rupiah standar, misal: `Rp 50.000`
     */
    fun formatIdr(amount: Long): String {
        return "Rp ${rupiahFormat.format(amount)}"
    }

    /**
     * Memformat angka ke pemisah ribuan titik tanpa prefix 'Rp '.
     * Contoh: 25000L -> "25.000", 1500000L -> "1.500.000"
     */
    fun formatThousands(amount: Long): String {
        return rupiahFormat.format(amount)
    }

    /**
     * Memformat string digit mentah menjadi string berformat pemisah ribuan.
     * Contoh: "25000" -> "25.000", "500" -> "500", "" -> ""
     */
    fun formatThousands(rawDigits: String): String {
        val clean = rawDigits.filter { it.isDigit() }
        if (clean.isEmpty()) return ""
        val num = clean.toLongOrNull() ?: return clean
        return rupiahFormat.format(num)
    }

    /**
     * Mengambil digit angka murni dari string berformat untuk penyimpanan normal ke database.
     * Contoh: "25.000" -> "25000", "Rp 50.000" -> "50000"
     */
    fun cleanDigits(formattedText: String): String {
        return formattedText.filter { it.isDigit() }
    }

    /**
     * Memformat volume ke format liter, misal: `25,450 L`
     */
    fun formatLiters(liters: Double, compact: Boolean = false): String {
        val formatted = if (compact) {
            compactLiterFormat.format(liters)
        } else {
            literFormat.format(liters)
        }
        return "$formatted L"
    }

    /**
     * Membersihkan teks hasil OCR atau input teks pengguna menjadi angka Long Rupiah.
     * Contoh: "Rp 50.000,-" -> 50000L, "150.500" -> 150500L
     */
    fun parseIdr(rawText: String): Long? {
        val clean = rawText
            .replace("Rp", "", ignoreCase = true)
            .replace("IDR", "", ignoreCase = true)
            .replace(",00", "")
            .replace(",-", "")
            .replace(".", "")
            .replace(",", "")
            .replace(" ", "")
            .trim()
        return clean.toLongOrNull()
    }

    /**
     * Parsing angka volume liter dari string.
     * Mengganti tanda koma (desimal Indonesia) menjadi titik untuk Double parsing.
     */
    fun parseLiters(rawText: String): Double? {
        val clean = rawText
            .replace("Ltr", "", ignoreCase = true)
            .replace("Liter", "", ignoreCase = true)
            .replace("L", "", ignoreCase = true)
            .trim()
            .replace(",", ".")
        return clean.toDoubleOrNull()
    }

    /**
     * Memformat angka persentase ke format bersih tanpa desimal (misal 100.0% -> 100%).
     * Dibulatkan ke bilangan bulat terdekat untuk tampilan UI yang rapi dan profesional.
     * Mencegah error jika nilai input bernilai NaN atau Infinite.
     * Contoh:
     * 100.0 -> "100"
     * 50.0 -> "50"
     * 0.0 -> "0"
     * 16.7 -> "17"
     */
    fun formatPercent(percentage: Double): String {
        if (percentage.isNaN() || percentage.isInfinite()) return "0"
        return kotlin.math.round(percentage).toLong().toString()
    }

    /**
     * Memformat angka persentase dengan akhiran '%' secara bersih tanpa desimal.
     * Contoh:
     * 100.0 -> "100%"
     * 0.0 -> "0%"
     */
    fun formatPercentWithSymbol(percentage: Double): String {
        return "${formatPercent(percentage)}%"
    }
}
