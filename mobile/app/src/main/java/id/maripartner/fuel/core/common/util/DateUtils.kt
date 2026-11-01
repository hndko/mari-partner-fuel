package id.maripartner.fuel.core.common.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utilitas tanggal dan waktu berbasis java.time yang kompatibel dengan API 26 via desugaring.
 * Standar format lokal Indonesia (contoh: "31 Oktober 2026").
 */
object DateUtils {

    val ZONE_JAKARTA: ZoneId = ZoneId.of("Asia/Jakarta")

    private val indonesianLocale = Locale("id", "ID")

    private val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", indonesianLocale)
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", indonesianLocale)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", indonesianLocale)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", indonesianLocale)
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", indonesianLocale)

    fun formatMonthYear(date: LocalDate): String {
        return date.format(monthYearFormatter)
    }

    fun formatFullDate(instant: Instant, zoneId: ZoneId = ZONE_JAKARTA): String {
        return instant.atZone(zoneId).format(fullDateFormatter)
    }

    fun formatShortDate(instant: Instant, zoneId: ZoneId = ZONE_JAKARTA): String {
        return instant.atZone(zoneId).format(shortDateFormatter)
    }

    fun formatDateTime(instant: Instant, zoneId: ZoneId = ZONE_JAKARTA): String {
        return instant.atZone(zoneId).format(dateTimeFormatter)
    }

    fun formatTime(instant: Instant, zoneId: ZoneId = ZONE_JAKARTA): String {
        return instant.atZone(zoneId).format(timeFormatter)
    }

    fun formatLocalDate(date: LocalDate): String {
        return date.format(fullDateFormatter)
    }

    /**
     * Memformat string ISO (Instant, LocalDateTime, atau LocalDate) menjadi tanggal Indonesia lengkap ("31 Oktober 2026").
     */
    fun formatIsoDate(isoString: String?, zoneId: ZoneId = ZONE_JAKARTA): String {
        if (isoString.isNullOrBlank()) return "-"
        return runCatching {
            val instant = Instant.parse(isoString)
            formatFullDate(instant, zoneId)
        }.recoverCatching {
            val localDate = LocalDate.parse(isoString.take(10))
            formatLocalDate(localDate)
        }.getOrElse {
            isoString
        }
    }

    /**
     * Memformat string ISO (Instant atau LocalDateTime) menjadi tanggal & waktu Indonesia ("31 Oktober 2026, 14:30").
     */
    fun formatIsoDateTime(isoString: String?, zoneId: ZoneId = ZONE_JAKARTA): String {
        if (isoString.isNullOrBlank()) return "-"
        return runCatching {
            val instant = Instant.parse(isoString)
            formatDateTime(instant, zoneId)
        }.recoverCatching {
            val localDateTime = LocalDateTime.parse(isoString.replace(" ", "T").take(19))
            localDateTime.atZone(zoneId).format(dateTimeFormatter)
        }.recoverCatching {
            val localDate = LocalDate.parse(isoString.take(10))
            formatLocalDate(localDate)
        }.getOrElse {
            isoString
        }
    }
}
