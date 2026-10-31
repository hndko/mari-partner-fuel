package id.maripartner.fuel.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import id.maripartner.fuel.MainActivity
import id.maripartner.fuel.R
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.navigation.NavRoute
import id.maripartner.fuel.data.local.FuelBudgetPreferences

/**
 * AppWidget Provider untuk menampilkan Ringkasan Pagu BBM & Tombol Aksi Cepat 1-Tap di Beranda Android.
 */
class MariFuelAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val budgetAmount = FuelBudgetPreferences.getBudget(context)

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, budgetAmount)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MariFuelAppWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val budgetAmount = FuelBudgetPreferences.getBudget(context)

            for (widgetId in allWidgetIds) {
                updateWidget(context, appWidgetManager, widgetId, budgetAmount)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            budgetAmount: Long
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_mari_fuel)

            views.setTextViewText(
                R.id.widget_budget_remaining,
                if (budgetAmount > 0L) CurrencyFormatter.formatRupiah(budgetAmount) else "Pagu BBM"
            )

            // Intent Scan Struk Cepat
            val scanIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("start_route", NavRoute.ReceiptScan.route)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val scanPendingIntent = PendingIntent.getActivity(
                context,
                101,
                scanIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_scan, scanPendingIntent)

            // Intent Catat Manual Cepat
            val recordIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("start_route", NavRoute.TransactionForm.route)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val recordPendingIntent = PendingIntent.getActivity(
                context,
                102,
                recordIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_record, recordPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
