package com.example.viaterra

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlertActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        // Get references to TextViews in XML
        val tvAlertTitle = findViewById<TextView>(R.id.tvAlertTitle)
        val tvAlertSeverity = findViewById<TextView>(R.id.tvAlertSeverity)
        val tvDetailLocation = findViewById<TextView>(R.id.tvDetailLocation)
        val tvDetailMagnitude = findViewById<TextView>(R.id.tvDetailMagnitude)
        val tvDetailDepth = findViewById<TextView>(R.id.tvDetailDepth)
        val tvDetailTime = findViewById<TextView>(R.id.tvDetailTime)

        // Receive earthquake data from intent
        val magnitude = intent.getStringExtra("magnitude") ?: "N/A"
        val location = intent.getStringExtra("location") ?: "Unknown"
        val time = intent.getStringExtra("time") ?: "Unknown"

        // Populate views
        tvAlertTitle.text = "Magnitude $magnitude Earthquake"
        tvAlertSeverity.text = "Severity: Moderate" // You can calculate later
        tvDetailLocation.text = location
        tvDetailMagnitude.text = "Magnitude: $magnitude"
        tvDetailTime.text = "Time: $time"
        tvDetailDepth.text = "Depth: 10 km" // Placeholder, can add real depth later
    }
}
