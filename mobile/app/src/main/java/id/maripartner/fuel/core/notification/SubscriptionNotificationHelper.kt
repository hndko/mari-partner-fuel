package id.maripartner.fuel.core.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import id.maripartner.fuel.MainActivity
import id.maripartner.fuel.MariPartnerFuelApp
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.domain.model.UserEntitlement

/**
 * Manajer notifikasi sistem Android untuk pengingat masa aktif Pro dan transisi paket.
 */
object SubscriptionNotificationHelper {

    private const val PREFS_NAME = "sub_notification_tracker"
    private const val KEY_LAST_NOTIFIED_DATE = "last_notified_date"
    private const val NOTIFICATION_ID_EXPIRING = 8801
    private const val NOTIFICATION_ID_EXPIRED = 8802

    fun checkAndNotifySubscriptionStatus(context: Context, entitlement: UserEntitlement) {
        val daysLeft = entitlement.effectiveDaysUntilExpiry ?: Int.MAX_VALUE
        val formattedDate = entitlement.expiresAt?.let { DateUtils.formatFullDate(it) } ?: "segera"
        checkAndNotifySubscriptionStatus(
            context = context,
            isPro = entitlement.isPro,
            isExpiringSoon = entitlement.isExpiringSoon || (entitlement.isPro && daysLeft <= 7),
            daysLeft = if (daysLeft != Int.MAX_VALUE) daysLeft else 0,
            isRecentlyExpired = entitlement.isRecentlyExpired,
            planName = entitlement.planName.ifBlank { if (entitlement.isPro) "Mari Pro" else "Mari Free" },
            expiresAtFormatted = formattedDate
        )
    }

    fun checkAndNotifySubscriptionStatus(
        context: Context,
        isPro: Boolean,
        isExpiringSoon: Boolean,
        daysLeft: Int,
        isRecentlyExpired: Boolean,
        planName: String,
        expiresAtFormatted: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) return
        }

        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayStr = java.time.LocalDate.now().toString()
        val lastNotified = prefs.getString(KEY_LAST_NOTIFIED_DATE, null)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Kasus 1: Masa Aktif Pro Segera Berakhir (<= 7 hari)
        if (isPro && (isExpiringSoon || daysLeft in 1..7)) {
            val keyToday = "$todayStr-expiring-$daysLeft"
            if (lastNotified != keyToday) {
                val notification = NotificationCompat.Builder(context, MariPartnerFuelApp.SUBSCRIPTION_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Masa Aktif Pro Berakhir dalam $daysLeft Hari ⚠️")
                    .setContentText("Paket $planName Anda jatuh tempo pada $expiresAtFormatted. Ketuk untuk memperpanjang.")
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(
                            "Paket $planName Anda akan berakhir dalam $daysLeft hari (pada $expiresAtFormatted). " +
                                "Perpanjang sekarang agar kuota scan AI dan kapasitas armada tidak terhenti."
                        )
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(NOTIFICATION_ID_EXPIRING, notification)
                prefs.edit().putString(KEY_LAST_NOTIFIED_DATE, keyToday).apply()
            }
        } else if (isRecentlyExpired) {
            // Kasus 2: Baru saja kedaluwarsa & beralih ke Free
            val keyExpired = "$todayStr-expired"
            if (lastNotified != keyExpired) {
                val notification = NotificationCompat.Builder(context, MariPartnerFuelApp.SUBSCRIPTION_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Paket Pro Anda Telah Berakhir")
                    .setContentText("Akun Anda kini berada pada Paket Free. Seluruh data transaksi BBM Anda tetap 100% aman.")
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(
                            "Masa aktif paket Pro Anda telah berakhir dan akun beralih ke Paket Free (maks 2 armada aktif & 5 scan AI/bln). " +
                                "Seluruh data riwayat transaksi Anda tersimpan aman."
                        )
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(NOTIFICATION_ID_EXPIRED, notification)
                prefs.edit().putString(KEY_LAST_NOTIFIED_DATE, keyExpired).apply()
            }
        }
    }
}
