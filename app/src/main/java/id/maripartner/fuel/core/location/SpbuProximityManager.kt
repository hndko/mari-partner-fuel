package id.maripartner.fuel.core.location

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

object SpbuProximityManager {

    private const val PREF_NAME = "mari_partner_proximity_prefs"
    private const val KEY_SPBU_REMINDER_ENABLED = "spbu_reminder_enabled"
    private const val GEOFENCE_RADIUS_METERS = 250f

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isSpbuReminderEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SPBU_REMINDER_ENABLED, true)
    }

    fun setSpbuReminderEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SPBU_REMINDER_ENABLED, enabled).apply()
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    fun findNearestSpbu(latitude: Double, longitude: Double): NearestSpbuResult? {
        val results = FloatArray(1)
        var closestSpbu: SpbuLocation? = null
        var minDistance = Float.MAX_VALUE

        for (spbu in SpbuDirectory.indonesiaSpbuList) {
            Location.distanceBetween(
                latitude,
                longitude,
                spbu.latitude,
                spbu.longitude,
                results
            )
            val distance = results[0]
            if (distance < minDistance) {
                minDistance = distance
                closestSpbu = spbu
            }
        }

        return closestSpbu?.let {
            NearestSpbuResult(
                spbu = it,
                distanceMeters = minDistance,
                isWithinGeofence = minDistance <= GEOFENCE_RADIUS_METERS
            )
        }
    }

    fun getCurrentDeviceLocation(context: Context): Pair<Double, Double>? {
        if (!hasLocationPermission(context)) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            var bestLocation: Location? = null
            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                        bestLocation = loc
                    }
                }
            }

            return bestLocation?.let { Pair(it.latitude, it.longitude) }
        } catch (_: SecurityException) {
            return null
        }
    }

    fun checkProximity(context: Context): NearestSpbuResult? {
        if (!isSpbuReminderEnabled(context)) return null
        val coords = getCurrentDeviceLocation(context) ?: return null
        return findNearestSpbu(coords.first, coords.second)
    }
}
