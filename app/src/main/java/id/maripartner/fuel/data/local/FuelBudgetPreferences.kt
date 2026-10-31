package id.maripartner.fuel.data.local

import android.content.Context
import android.content.SharedPreferences

object FuelBudgetPreferences {

    private const val PREF_NAME = "mari_partner_fuel_budget_prefs"
    private const val KEY_GLOBAL_BUDGET = "monthly_budget_global"
    private const val KEY_PREFIX_VEHICLE_BUDGET = "monthly_budget_vehicle_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getBudget(context: Context, vehicleId: String? = null): Long {
        val key = if (vehicleId.isNullOrBlank()) KEY_GLOBAL_BUDGET else "$KEY_PREFIX_VEHICLE_BUDGET$vehicleId"
        return getPrefs(context).getLong(key, 0L)
    }

    fun setBudget(context: Context, amountIdr: Long, vehicleId: String? = null) {
        val key = if (vehicleId.isNullOrBlank()) KEY_GLOBAL_BUDGET else "$KEY_PREFIX_VEHICLE_BUDGET$vehicleId"
        getPrefs(context).edit().putLong(key, amountIdr).apply()
    }
}
