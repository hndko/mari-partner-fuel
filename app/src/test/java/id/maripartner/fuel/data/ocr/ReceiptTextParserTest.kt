package id.maripartner.fuel.data.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ReceiptTextParserTest {

    private val parser = ReceiptTextParser()

    @Test
    fun parse_standardPertaminaReceipt_extractsAllFieldsAccurately() {
        val rawReceipt = """
            SPBU PERTAMINA 31.12345
            JL. TB SIMATUPANG NO. 10
            JAKARTA SELATAN
            
            NO. STRUK: TRX-987654
            TANGGAL: 29/10/2026 14:35:10
            
            PRODUK: PERTALITE
            HARGA / L: Rp 10.000
            VOLUME: 5.000 LITER
            
            TOTAL HARGA: Rp 50.000
            TUNAI: Rp 100.000
            KEMBALI: Rp 50.000
            
            TERIMA KASIH DAN SELAMAT JALAN
        """.trimIndent()

        val result = parser.parse(rawReceipt)

        assertEquals(50000L, result.totalAmountIdr.value)
        assertEquals(FieldConfidence.HIGH, result.totalAmountIdr.confidence)

        assertEquals(5.0, result.volumeLiters.value ?: 0.0, 0.001)
        assertEquals(FieldConfidence.HIGH, result.volumeLiters.confidence)

        assertEquals(10000.0, result.pricePerLiterIdr.value ?: 0.0, 0.001)
        assertEquals(FieldConfidence.HIGH, result.pricePerLiterIdr.confidence)

        assertEquals("Pertalite", result.fuelType.value)
        assertTrue(result.stationName.value?.contains("PERTAMINA", ignoreCase = true) == true)
        assertEquals(LocalDate.of(2026, 10, 29), result.transactionDate.value)
        assertEquals(LocalTime.of(14, 35), result.transactionTime.value)
        assertEquals("TRX-987654", result.receiptNumber.value)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun parse_shellReceipt_extractsFuelAndVolume() {
        val rawReceipt = """
            SHELL GATOT SUBROTO
            JAKARTA
            
            29-10-2026 09:15
            SHELL SUPER
            VOL: 12.50 L
            @ 14.500
            
            TOTAL BAYAR: 181.250
            KARTU DEBIT: 181.250
        """.trimIndent()

        val result = parser.parse(rawReceipt)

        assertEquals(181250L, result.totalAmountIdr.value)
        assertEquals(12.50, result.volumeLiters.value ?: 0.0, 0.001)
        assertEquals(14500.0, result.pricePerLiterIdr.value ?: 0.0, 0.001)
        assertEquals("Shell Super", result.fuelType.value)
        assertTrue(result.stationName.value?.contains("SHELL", ignoreCase = true) == true)
        assertEquals(LocalDate.of(2026, 10, 29), result.transactionDate.value)
        assertEquals(LocalTime.of(9, 15), result.transactionTime.value)
    }

    @Test
    fun parse_distinguishesTotalFromCashAndChange() {
        val rawReceipt = """
            SPBU 34.123
            TOTAL: Rp 75.000
            TUNAI: Rp 100.000
            KEMBALI: Rp 25.000
        """.trimIndent()

        val result = parser.parse(rawReceipt)

        // Harus memilih 75000, bukan 100000 (tunai) atau 25000 (kembali)
        assertEquals(75000L, result.totalAmountIdr.value)
    }

    @Test
    fun parse_detectsMathematicalDiscrepancy() {
        // Total 90.000 padahal 5L * Rp 10.000 = Rp 50.000 (ada selisih Rp 40.000)
        val rawReceipt = """
            SPBU PERTAMINA
            PRODUK: PERTAMAX
            VOLUME: 5.0 LTR
            HARGA/L: 10.000
            TOTAL: Rp 90.000
        """.trimIndent()

        val result = parser.parse(rawReceipt)

        assertEquals(90000L, result.totalAmountIdr.value)
        assertTrue(result.warnings.any { it.contains("berselisih", ignoreCase = true) })
    }

    @Test
    fun parse_emptyOrGarbledReceipt_handlesGracefullyWithoutCrashing() {
        val garbled = """
            ~~~~~
            random noise
            !@#${'$'}%^&*()
            no numbers
        """.trimIndent()

        val result = parser.parse(garbled)

        assertNull(result.totalAmountIdr.value)
        assertEquals(FieldConfidence.MISSING, result.totalAmountIdr.confidence)
        assertNull(result.volumeLiters.value)
        assertNull(result.fuelType.value)
        assertNull(result.transactionDate.value)
        assertTrue(result.warnings.isNotEmpty())
    }
}
