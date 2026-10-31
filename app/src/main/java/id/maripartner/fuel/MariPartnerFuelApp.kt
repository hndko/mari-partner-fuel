package id.maripartner.fuel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MariPartnerFuelApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannels()
    }

    private fun setupNotificationChannels() {
        // Notification channels are mandatory starting from Android 8.0 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.app_name)
            val channelDesc = getString(R.string.app_tagline)
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(
                FUEL_REMINDER_CHANNEL_ID,
                channelName,
                importance
            ).apply {
                description = channelDesc
            }

            val subChannel = NotificationChannel(
                SUBSCRIPTION_CHANNEL_ID,
                "Status & Pengingat Langganan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat masa aktif Pro dan pembaruan paket"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(subChannel)
        }
    }

    companion object {
        const val FUEL_REMINDER_CHANNEL_ID = "fuel_service_reminders"
        const val SUBSCRIPTION_CHANNEL_ID = "subscription_status_alerts"
    }
}
