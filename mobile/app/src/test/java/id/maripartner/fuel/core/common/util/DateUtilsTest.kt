package id.maripartner.fuel.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun formatFullDate_formatsWithFullIndonesianMonthName() {
        val instant = Instant.parse("2026-10-31T03:00:00Z") // 10:00 WIB
        val formatted = DateUtils.formatFullDate(instant)
        assertEquals("31 Oktober 2026", formatted)
    }

    @Test
    fun formatDateTime_formatsWithFullMonthAndHoursMinutes() {
        val instant = Instant.parse("2026-10-31T07:30:00Z") // 14:30 WIB
        val formatted = DateUtils.formatDateTime(instant)
        assertEquals("31 Oktober 2026, 14:30", formatted)
    }

    @Test
    fun formatMonthYear_formatsMonthAndYearInIndonesian() {
        val date = LocalDate.of(2026, 10, 31)
        val formatted = DateUtils.formatMonthYear(date)
        assertEquals("Oktober 2026", formatted)
    }

    @Test
    fun formatLocalDate_formatsLocalDateWithFullMonthName() {
        val date = LocalDate.of(2026, 10, 31)
        val formatted = DateUtils.formatLocalDate(date)
        assertEquals("31 Oktober 2026", formatted)
    }

    @Test
    fun formatIsoDate_handlesInstantAndLocalDateStrings() {
        // Instant ISO
        assertEquals("31 Oktober 2026", DateUtils.formatIsoDate("2026-10-31T03:00:00Z"))
        // LocalDate ISO
        assertEquals("31 Oktober 2026", DateUtils.formatIsoDate("2026-10-31"))
        // Null or blank
        assertEquals("-", DateUtils.formatIsoDate(null))
        assertEquals("-", DateUtils.formatIsoDate(""))
    }

    @Test
    fun formatIsoDateTime_handlesInstantAndLocalDateTimeStrings() {
        // Instant ISO
        assertEquals("31 Oktober 2026, 14:30", DateUtils.formatIsoDateTime("2026-10-31T07:30:00Z"))
        // Null or blank
        assertEquals("-", DateUtils.formatIsoDateTime(null))
        assertEquals("-", DateUtils.formatIsoDateTime(""))
    }
}
