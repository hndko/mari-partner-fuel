package id.maripartner.fuel.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import id.maripartner.fuel.domain.model.MaintenanceReminder
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Utility penjadwalan alarm notifikasi lokal Android untuk pengingat servis berbasis tanggal.
 */
class ReminderNotificationScheduler(
    private val context: Context
) {

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    /**
     * Menjadwalkan alarm notifikasi jika pengingat memiliki pemicu tanggal yang belum lewat.
     */
    fun scheduleReminder(reminder: MaintenanceReminder) {
        val targetDate = reminder.targetDate ?: return
        if (reminder.isCompleted) {
            cancelReminder(reminder.id)
            return
        }

        val now = LocalDate.now()
        if (targetDate.isBefore(now)) {
            // Sudah lewat, jangan jadwalkan alarm di masa lalu
            return
        }

        val triggerTime = targetDate.atTime(LocalTime.of(8, 0)) // Pukul 08:00 pagi
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerTime <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderNotificationReceiver.EXTRA_TITLE, "Pengingat: ${reminder.title}")
            val vehicleLabel = reminder.vehicleName?.let { " untuk $it" } ?: ""
            putExtra(ReminderNotificationReceiver.EXTRA_MESSAGE, "Jadwal perawatan${vehicleLabel} telah jatuh tempo hari ini.")
        }

        val requestCode = reminder.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.let { manager ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    manager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (e: SecurityException) {
                // Izin alarm ditolak sistem, fallback graceful tanpa crash
            }
        }
    }

    /**
     * Membatalkan jadwal alarm notifikasi untuk pengingat tertentu.
     */
    fun cancelReminder(reminderId: String) {
        val intent = Intent(context, ReminderNotificationReceiver::class.java)
        val requestCode = reminderId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
