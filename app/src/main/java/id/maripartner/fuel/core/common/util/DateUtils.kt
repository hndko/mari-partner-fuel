package id.maripartner.fuel.core.common.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utilitas tanggal dan waktu berbasis java.time yang kompatibel dengan API 26 via desugaring.
 */
object DateUtils {

    val ZONE_JAKARTA: ZoneId = ZoneId.of("Asia/Jakarta")

    private val indonesianLocale = Locale("id", "ID")

    private val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", indonesianLocale)
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", indonesianLocale)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", indonesianLocale)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", indonesianLocale)

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
}
