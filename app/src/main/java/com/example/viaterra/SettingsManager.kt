package com.example.viaterra.util

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "ViaTerraSettings"
    private const val KEY_RADIUS = "detection_radius"
    private const val KEY_MIN_MAGNITUDE = "min_magnitude"
    private const val KEY_AUTO_LOCATION = "auto_location_enabled"
    private const val KEY_EARTHQUAKE_ALERTS = "earthquake_alerts_enabled"
    private const val KEY_TORNADO_ALERTS = "tornado_alerts_enabled"
    private const val KEY_FLOOD_ALERTS = "flood_alerts_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Radius settings (in kilometers)
    fun getRadius(context: Context): Int {
        return getPrefs(context).getInt(KEY_RADIUS, 200) // Default 200 km
    }

    fun setRadius(context: Context, radius: Int) {
        getPrefs(context).edit().putInt(KEY_RADIUS, radius).apply()
    }

    // Minimum magnitude for earthquakes
    fun getMinMagnitude(context: Context): Double {
        return getPrefs(context).getFloat(KEY_MIN_MAGNITUDE, 3.0f).toDouble() // Default 3.0
    }

    fun setMinMagnitude(context: Context, magnitude: Double) {
        getPrefs(context).edit().putFloat(KEY_MIN_MAGNITUDE, magnitude.toFloat()).apply()
    }

    // Auto-location setting
    fun autoLocationEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_LOCATION, true) // Default enabled
    }

    fun setAutoLocation(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_LOCATION, enabled).apply()
    }

    // Alert type settings
    fun earthquakeAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_EARTHQUAKE_ALERTS, true) // Default enabled
    }

    fun setEarthquakeAlerts(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_EARTHQUAKE_ALERTS, enabled).apply()
    }

    fun tornadoAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TORNADO_ALERTS, true) // Default enabled
    }

    fun setTornadoAlerts(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TORNADO_ALERTS, enabled).apply()
    }

    fun floodAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FLOOD_ALERTS, true) // Default enabled
    }

    fun setFloodAlerts(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_FLOOD_ALERTS, enabled).apply()
    }
}
