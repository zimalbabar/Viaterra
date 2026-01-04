package com.example.viaterra

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.adapter.FloodAlertAdapter
import com.example.viaterra.model.GDACSAlert
import okhttp3.*
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.util.*

class FloodActivity : AppCompatActivity() {

    private val gdacsUrl = "https://www.gdacs.org/contentdata/xml/rss_fl_7d.xml"

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

        // Refresh button click
        btnRefresh.setOnClickListener { fetchGDACSAlerts() }

        // Initial fetch
        fetchGDACSAlerts()

        // Back button
        findViewById<ImageView>(R.id.BackBtn).setOnClickListener {
            startActivity(Intent(this, DisasterSelectionActivity::class.java))
        }
    }

    // ---------------- Fetch & Parse Flood Alerts ----------------
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
            Log.e("GDACS", "Error parsing XML", e)
        }

        return alerts
    }

    private fun updateAlertCount(count: Int) {
        tvAlertCount.text = count.toString()
    }



}
