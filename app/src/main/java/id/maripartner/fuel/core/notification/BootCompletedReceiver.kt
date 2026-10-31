package id.maripartner.fuel.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.MaintenanceReminderRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver untuk menjadwalkan ulang alarm notifikasi pengingat servis setelah perangkat Android di-reboot.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val scheduler = ReminderNotificationScheduler(context.applicationContext)
            val repository = MaintenanceReminderRepositoryImpl()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    when (val result = repository.getReminders(isCompleted = false)) {
                        is AppResult.Success -> {
                            result.data.forEach { reminder ->
                                scheduler.scheduleReminder(reminder)
                            }
                        }
                        is AppResult.Error -> {
                            // Abaikan bila belum login atau offline saat perangkat boot
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
