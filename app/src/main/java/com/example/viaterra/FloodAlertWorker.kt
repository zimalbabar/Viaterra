package com.example.viaterra

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.util.Xml
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.util.*
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.takeLast
import kotlin.collections.toList
import kotlin.collections.toMutableSet
import kotlin.io.reader
import kotlin.jvm.java
import kotlin.text.equals
import kotlin.text.lowercase
import kotlin.text.replace
import kotlin.text.uppercase

class FloodAlertWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val gdacsUrl = "https://www.gdacs.org/contentdata/xml/rss_fl_7d.xml"
    private val TAG = "FloodWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting background check...")

            // FIXED: Verify permission before attempting to show notifications
            if (!hasNotificationPermission()) {
                Log.w(TAG, "No notification permission, skipping check")
                return@withContext Result.success()
            }

            // Fetch alerts
            val alerts = fetchAndParseAlerts()
            Log.d(TAG, "Fetched ${alerts.size} total alerts")

            // Get only new alerts
            val newAlerts = getNewAlerts(alerts)
            Log.d(TAG, "Found ${newAlerts.size} new alerts")

            // Show notifications for new alerts
            newAlerts.forEach { alert ->
                showFloodNotification(alert)
            }

            // FIXED: Save ALL current alerts, not just new ones
            saveSeenAlerts(alerts)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in background check", e)
            Result.retry()
        }
    }

    // FIXED: Added permission check method
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not needed on older versions
        }
    }

    private fun fetchAndParseAlerts(): List<GDACSAlert> {
        val client = OkHttpClient()
        val request = Request.Builder().url(gdacsUrl).build()

        val response = client.newCall(request).execute()
        val xml = response.body?.string() ?: return emptyList()

        return parseGDACSRSS(xml)
    }

    private fun parseGDACSRSS(xml: String): List<GDACSAlert> {
        val alerts = mutableListOf<GDACSAlert>()
        val parser = Xml.newPullParser()
        parser.setInput(xml.reader())

        var insideItem = false
        var itemDepth = 0

        var tempEventType = ""
        var tempSeverity = ""
        var tempAlertLevel = ""
        var tempEventId = ""
        var tempEventName = ""
        var tempCountry = ""
        var tempFromDate = ""
        var tempToDate = ""
        var tempLat = ""
        var tempLong = ""
        var tempDescription = ""

        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val name = parser.name.lowercase(Locale.getDefault())
                        if (name == "item") {
                            insideItem = true
                            itemDepth = parser.depth
                            tempEventType = ""
                            tempSeverity = ""
                            tempAlertLevel = ""
                            tempEventId = ""
                            tempEventName = ""
                            tempCountry = ""
                            tempFromDate = ""
                            tempToDate = ""
                            tempLat = ""
                            tempLong = ""
                            tempDescription = ""
                        } else if (insideItem) {
                            val depth = parser.depth
                            when (name.replace("gdacs:", "")) {
                                "eventtype" -> tempEventType = parser.nextText()
                                "alertscore" -> tempSeverity = parser.nextText()
                                "alertlevel" -> tempAlertLevel = parser.nextText()
                                "eventid" -> tempEventId = parser.nextText()
                                "title" -> if (depth == itemDepth + 1) tempEventName = parser.nextText()
                                "country" -> tempCountry = parser.nextText()
                                "fromdate" -> tempFromDate = parser.nextText()
                                "todate" -> tempToDate = parser.nextText()
                                "lat" -> tempLat = parser.nextText()
                                "long" -> tempLong = parser.nextText()
                                "description" -> if (depth == itemDepth + 1) tempDescription = parser.nextText()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("item", ignoreCase = true)) {
                            insideItem = false
                            if (tempEventType.equals("FL", ignoreCase = true)) {
                                alerts.add(
                                    GDACSAlert(
                                        eventType = tempEventType,
                                        severity = tempSeverity,
                                        alertlevel = tempAlertLevel,
                                        eventId = tempEventId,
                                        eventName = tempEventName,
                                        country = tempCountry,
                                        fromDate = tempFromDate,
                                        toDate = tempToDate,
                                        latitude = tempLat,
                                        longitude = tempLong,
                                        description = tempDescription
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML", e)
        }

        return alerts
    }

    private fun getNewAlerts(currentAlerts: List<GDACSAlert>): List<GDACSAlert> {
        val prefs = context.getSharedPreferences("flood_alerts_prefs", Context.MODE_PRIVATE)
        val seenIds = prefs.getStringSet("seen_alert_ids", emptySet()) ?: emptySet()

        // FIXED: Added logging to debug notification logic
        Log.d(TAG, "Currently tracked seen IDs: ${seenIds.size}")

        val newAlerts = currentAlerts.filter { alert ->
            val isNew = !seenIds.contains(alert.eventId)
            if (isNew) {
                Log.d(TAG, "New alert detected: ${alert.eventId} - ${alert.eventName}")
            }
            isNew
        }

        return newAlerts
    }

    private fun saveSeenAlerts(alerts: List<GDACSAlert>) {
        val prefs = context.getSharedPreferences("flood_alerts_prefs", Context.MODE_PRIVATE)
        val currentSeenIds = prefs.getStringSet("seen_alert_ids", emptySet())?.toMutableSet()
            ?: mutableSetOf()

        // FIXED: Add all current alert IDs
        val beforeSize = currentSeenIds.size
        alerts.forEach { currentSeenIds.add(it.eventId) }
        val afterSize = currentSeenIds.size

        Log.d(TAG, "Updated seen alerts: $beforeSize → $afterSize")

        // Keep only last 100 to prevent unlimited growth
        if (currentSeenIds.size > 100) {
            val list = currentSeenIds.toList()
            currentSeenIds.clear()
            currentSeenIds.addAll(list.takeLast(100))
            Log.d(TAG, "Trimmed seen alerts to 100 most recent")
        }

        prefs.edit().putStringSet("seen_alert_ids", currentSeenIds).apply()
    }

    private fun showFloodNotification(alert: GDACSAlert) {
        // FIXED: Ensure channel exists before showing notification
        createNotificationChannel()

        // FIXED: Double-check permission right before showing
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Cannot show notification: permission not granted")
            return
        }

        // FIXED: Create intent to open app when notification is tapped
        val intent = Intent(context, FloodActivity::class.java).apply {
            // Call the method on the intent instance (this)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.eventId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // FIXED: Improved notification with severity-based priority
        val priority = when (alert.alertlevel.uppercase()) {
            "RED" -> NotificationCompat.PRIORITY_MAX
            "ORANGE" -> NotificationCompat.PRIORITY_HIGH
            "GREEN" -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(context, "flood_alerts")
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .setContentTitle("🌊 ${alert.alertlevel.uppercase()} Flood Alert: ${alert.country}")
            .setContentText(alert.eventName)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${alert.eventName}\n\n${alert.description}")
            )
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(alert.eventId.hashCode(), notification)
            Log.d(TAG, "Notification shown for: ${alert.eventName}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception showing notification", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "flood_alerts",
                "Flood Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for global flood alerts"
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    data class GDACSAlert(
        val eventType: String,
        val severity: String,
        val alertlevel: String,
        val eventId: String,
        val eventName: String,
        val country: String,
        val fromDate: String,
        val toDate: String,
        val latitude: String,
        val longitude: String,
        val description: String
    )
}