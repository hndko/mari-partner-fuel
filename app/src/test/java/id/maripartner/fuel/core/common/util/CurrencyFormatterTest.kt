package id.maripartner.fuel.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun formatIdr_formatsCorrectly() {
        assertEquals("Rp 50.000", CurrencyFormatter.formatIdr(50_000L))
        assertEquals("Rp 1.500.000", CurrencyFormatter.formatIdr(1_500_000L))
        assertEquals("Rp 0", CurrencyFormatter.formatIdr(0L))
    }

    @Test
    fun parseIdr_handlesVariousRawFormats() {
        assertEquals(50000L, CurrencyFormatter.parseIdr("Rp 50.000"))
        assertEquals(50000L, CurrencyFormatter.parseIdr("Rp 50.000,00"))
        assertEquals(50000L, CurrencyFormatter.parseIdr("Rp 50.000,-"))
        assertEquals(150000L, CurrencyFormatter.parseIdr("150000"))
        assertEquals(150000L, CurrencyFormatter.parseIdr("IDR 150.000"))
        assertEquals(27500L, CurrencyFormatter.parseIdr("27.500"))
    }

    @Test
    fun parseIdr_returnsNullOnInvalidInput() {
        assertNull(CurrencyFormatter.parseIdr("abc"))
        assertNull(CurrencyFormatter.parseIdr(""))
    }

    @Test
    fun formatLiters_formatsDecimalsProperly() {
        assertEquals("15,250 L", CurrencyFormatter.formatLiters(15.25))
        assertEquals("30,000 L", CurrencyFormatter.formatLiters(30.0))
    }

    @Test
    fun parseLiters_handlesCommaAndPeriodSeparators() {
        assertEquals(15.25, CurrencyFormatter.parseLiters("15,25 L") ?: 0.0, 0.001)
        assertEquals(15.25, CurrencyFormatter.parseLiters("15.25 Liter") ?: 0.0, 0.001)
        assertEquals(20.0, CurrencyFormatter.parseLiters("20 Ltr") ?: 0.0, 0.001)
    }
}
