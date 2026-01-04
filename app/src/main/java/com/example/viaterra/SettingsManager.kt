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
