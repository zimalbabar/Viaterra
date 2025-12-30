package com.example.viaterra

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.util.*

import androidx.work.WorkRequest
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class FloodActivity : AppCompatActivity() {

    private val gdacsUrl = "https://www.gdacs.org/contentdata/xml/rss_fl_7d.xml"
    private val NOTIFICATION_PERMISSION_CODE = 1001

    // UI Elements
    private lateinit var btnRefresh: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var tvAlertCount: TextView
    private lateinit var recyclerView: RecyclerView

    private lateinit var alertAdapter: FloodAlertAdapter
    private val alertsList = mutableListOf<GDACSAlert>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_floods)

        // Bind views
        btnRefresh = findViewById(R.id.btnRefreshData)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvAlertCount = findViewById(R.id.tvAlertCount)
        recyclerView = findViewById(R.id.recyclerViewAlerts)

        // Setup RecyclerView
        alertAdapter = FloodAlertAdapter(alertsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = alertAdapter

        // FIXED: Create notification channel FIRST
        createNotificationChannel()

        // FIXED: Request permission before scheduling
        requestNotificationPermission()

        // Refresh button click
        btnRefresh.setOnClickListener { fetchGDACSAlerts() }

        // Initial fetch
        fetchGDACSAlerts()

        // testing notifications

//        val btnTest = findViewById<Button>(R.id.btnTestNotification)
//        btnTest.setOnClickListener {
//            // Clear seen alerts to trigger notifications
//            getSharedPreferences("flood_alerts_prefs", MODE_PRIVATE)
//                .edit()
//                .remove("seen_alert_ids")
//                .apply()
//
//            // Trigger immediate work
//            val workRequest = OneTimeWorkRequestBuilder<FloodAlertWorker>()
//                .build()
//            WorkManager.getInstance(applicationContext).enqueue(workRequest)
//
//            Toast.makeText(this, "Checking for alerts...", Toast.LENGTH_SHORT).show()
//        }
    }

    // FIXED: Moved notification channel creation to be called earlier
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "flood_alerts",
                "Flood Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for global flood alerts"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d("GDACS", "Notification channel created")
        }
    }

    private fun scheduleFloodAlertChecks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<FloodAlertWorker>(
            15, TimeUnit.MINUTES  // Check every 15 minutes (minimum allowed)
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "flood_alert_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Log.d("GDACS", "Background monitoring scheduled")
    }

    private fun fetchGDACSAlerts() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE

        val client = OkHttpClient()
        val request = Request.Builder().url(gdacsUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    tvEmptyState.visibility = View.VISIBLE
                    tvEmptyState.text = "Failed to load data: ${e.message}"
                    updateAlertCount(0)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val xml = response.body?.string()
                if (xml == null) {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        tvEmptyState.visibility = View.VISIBLE
                        tvEmptyState.text = "No data received"
                        updateAlertCount(0)
                    }
                    return
                }

                val alerts = parseGDACSRSS(xml)

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (alerts.isNotEmpty()) {
                        alertsList.clear()
                        alertsList.addAll(alerts)
                        alertAdapter.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                        tvEmptyState.visibility = View.GONE
                        updateAlertCount(alerts.size)

                        // FIXED: Initialize seen alerts on first run to prevent spam
                        initializeSeenAlertsIfNeeded(alerts)
                    } else {
                        tvEmptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        tvEmptyState.text = "No flood alerts found"
                        updateAlertCount(0)
                    }
                }
            }
        })
    }

    // FIXED: New method to prevent notifying about existing alerts on first run
    private fun initializeSeenAlertsIfNeeded(alerts: List<GDACSAlert>) {
        val prefs = getSharedPreferences("flood_alerts_prefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

        if (isFirstRun) {
            // Mark all current alerts as seen so we don't spam on first run
            val seenIds = alerts.map { it.eventId }.toSet()
            prefs.edit()
                .putStringSet("seen_alert_ids", seenIds)
                .putBoolean("is_first_run", false)
                .apply()
            Log.d("GDACS", "First run: Initialized ${seenIds.size} alerts as seen")
        }
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
                                Log.d("GDACS", "Flood alert added: $tempEventName")
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("GDACS", "Error parsing XML", e)
        }

        Log.d("GDACS", "Total flood alerts found: ${alerts.size}")
        return alerts
    }

    private fun updateAlertCount(count: Int) {
        tvAlertCount.text = count.toString()
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

    inner class FloodAlertAdapter(private val alerts: List<GDACSAlert>) :
        RecyclerView.Adapter<FloodAlertAdapter.AlertViewHolder>() {

        inner class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvEventName: TextView = view.findViewById(R.id.tvEventName)
            val tvCountry: TextView = view.findViewById(R.id.tvCountry)
            val tvEventId: TextView = view.findViewById(R.id.tvEventId)
            val tvDateRange: TextView = view.findViewById(R.id.tvDateRange)
            val tvCoordinates: TextView = view.findViewById(R.id.tvCoordinates)
            val tvDescriptionPreview: TextView = view.findViewById(R.id.tvDescriptionPreview)
            val btnViewOnMap: Button = view.findViewById(R.id.btnViewOnMap)
            val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)
            val headerLayout: LinearLayout = view.findViewById(R.id.alertHeaderLayout)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_flood_alert, parent, false)
            return AlertViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
            val alert = alerts[position]

            holder.tvEventName.text = alert.eventName.ifEmpty { "Flood Event ${position + 1}" }
            holder.tvCountry.text = alert.country.ifEmpty { "Unknown Location" }
            holder.tvEventId.text = alert.eventId.ifEmpty { "--" }
            holder.tvDateRange.text = if (alert.fromDate.isNotEmpty() && alert.toDate.isNotEmpty()) {
                "${alert.fromDate} → ${alert.toDate}"
            } else "Date not available"
            holder.tvCoordinates.text = if (alert.latitude.isNotEmpty() && alert.longitude.isNotEmpty()) {
                "${alert.latitude}, ${alert.longitude}"
            } else "Coordinates not available"
            holder.tvDescriptionPreview.text = alert.description.ifEmpty { "No description available" }

            when (alert.alertlevel.uppercase()) {
                "RED" -> holder.headerLayout.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.pastel_red)
                )
                "ORANGE" -> holder.headerLayout.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.pastel_orange)
                )
                "GREEN" -> holder.headerLayout.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.pastel_green)
                )
                else -> holder.headerLayout.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.pastel_grey)
                )
            }



            holder.btnViewOnMap.setOnClickListener {
                if (alert.latitude.isNotEmpty() && alert.longitude.isNotEmpty()) {
                    val uri = "geo:${alert.latitude},${alert.longitude}?q=${alert.latitude},${alert.longitude}(${alert.eventName})"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    startActivity(intent)
                }
            }

            holder.btnViewDetails.setOnClickListener {
                val url = "https://www.gdacs.org/report.aspx?eventtype=FL&eventid=${alert.eventId}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }

        override fun getItemCount() = alerts.size
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE)
            } else {
                // FIXED: Only schedule work after permission is confirmed
                scheduleFloodAlertChecks()
            }
        } else {
            // FIXED: For older Android versions, schedule directly
            scheduleFloodAlertChecks()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("GDACS", "Notification permission granted")
                // FIXED: Schedule work after permission granted
                scheduleFloodAlertChecks()
            } else {
                Log.d("GDACS", "Notification permission denied - background monitoring disabled")
            }
        }
    }
}