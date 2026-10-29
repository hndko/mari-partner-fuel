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
}
