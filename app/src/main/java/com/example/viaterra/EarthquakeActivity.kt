package com.example.viaterra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.adapter.EarthquakeAdapter
import com.example.viaterra.api.RetrofitClient
import com.example.viaterra.model.EarthquakeResponse
import com.example.viaterra.util.SettingsManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EarthquakeActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    private lateinit var rvEarthquakes: RecyclerView
    private lateinit var tvAlertStatus: TextView
    private lateinit var tvLastUpdate: TextView
    private lateinit var tvLocation: TextView
    private lateinit var seekBarRadius: SeekBar
    private lateinit var tvRadiusValue: TextView
    private lateinit var emptyStateLayout: LinearLayout

    private var currentUserLat: Double = 0.0
    private var currentUserLon: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earthquake)

        // Initialize views
        rvEarthquakes = findViewById(R.id.rvEarthquakes)
        tvAlertStatus = findViewById(R.id.tvAlertStatus)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        tvLocation = findViewById(R.id.tvLocation)
        seekBarRadius = findViewById(R.id.seekBarRadius)
        tvRadiusValue = findViewById(R.id.tvRadiusValue)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)

        rvEarthquakes.layoutManager = LinearLayoutManager(this)

        val currentRadius = SettingsManager.getRadius(this)
        seekBarRadius.progress = currentRadius - 50 // Offset for 50km min
        tvRadiusValue.text = "Current: $currentRadius km"

        seekBarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val radiusKm = progress + 50 // Min 50km, max 500km
                tvRadiusValue.text = "Current: $radiusKm km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val radiusKm = (seekBar?.progress ?: 0) + 50
                SettingsManager.setRadius(this@EarthquakeActivity, radiusKm)
                Toast.makeText(this@EarthquakeActivity, "Radius updated to $radiusKm km", Toast.LENGTH_SHORT).show()
                if (currentUserLat != 0.0 && currentUserLon != 0.0) {
                    loadEarthquakeData(currentUserLat, currentUserLon)
                }
            }
        })

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Refresh button
        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            Toast.makeText(this, "Fetching earthquake data...", Toast.LENGTH_SHORT).show()
            checkLocationPermission()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getDeviceLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getDeviceLocation()
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDeviceLocation() {
        if (!SettingsManager.autoLocationEnabled(this)) {
            tvLocation.text = "Auto-location disabled"
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentUserLat = location.latitude
                currentUserLon = location.longitude
                tvLocation.text = String.format("Your Location: %.4f, %.4f", currentUserLat, currentUserLon)
                loadEarthquakeData(currentUserLat, currentUserLon)
            } else {
                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadEarthquakeData(userLat: Double, userLon: Double) {
        val minMag = SettingsManager.getMinMagnitude(this)
        val maxRadiusKm = SettingsManager.getRadius(this)
        val earthquakesEnabled = SettingsManager.earthquakeAlertsEnabled(this)

        if (!earthquakesEnabled) {
            tvAlertStatus.text = "Earthquake alerts are disabled"
            rvEarthquakes.adapter = EarthquakeAdapter(emptyList()) {}
            showEmptyState(true)
            return
        }

        tvLastUpdate.text = "Fetching data..."

        RetrofitClient.api.getEarthquakes()
            .enqueue(object : Callback<EarthquakeResponse> {
                override fun onResponse(
                    call: Call<EarthquakeResponse>,
                    response: Response<EarthquakeResponse>
                ) {
                    val earthquakes = response.body()?.features?.mapNotNull {
                        val quakeLat = it.geometry.coordinates[1]
                        val quakeLon = it.geometry.coordinates[0]
                        val dist = distanceInMiles(userLat, userLon, quakeLat, quakeLon)
                        val distanceKm = dist * 1.609

                        if (distanceKm <= maxRadiusKm &&
                            (it.properties.mag ?: 0.0) >= minMag) {

                            Earthquake(
                                magnitude = it.properties.mag?.toString() ?: "N/A",
                                location = it.properties.place ?: "Unknown",
                                time = formatTime(it.properties.time),
                                distance = String.format("%.1f KM", distanceKm)
                            )
                        } else null
                    } ?: emptyList()

                    if (earthquakes.isEmpty()) {
                        showEmptyState(true)
                        tvAlertStatus.text = "No nearby earthquakes"
                    } else {
                        showEmptyState(false)
                        rvEarthquakes.adapter = EarthquakeAdapter(earthquakes) { earthquake ->
                            val intent = Intent(this@EarthquakeActivity, AlertActivity::class.java)
                            intent.putExtra("magnitude", earthquake.magnitude)
                            intent.putExtra("location", earthquake.location)
                            intent.putExtra("time", earthquake.time)
                            startActivity(intent)
                        }
                        tvAlertStatus.text = "Nearby Earthquakes (${earthquakes.size})"
                    }

                    tvLastUpdate.text = "Last Updated: Just now"
                }

                override fun onFailure(call: Call<EarthquakeResponse>, t: Throwable) {
                    tvAlertStatus.text = "Failed to load data"
                    tvLastUpdate.text = "Error: ${t.message}"
                    showEmptyState(false)
                }
            })
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            emptyStateLayout.visibility = View.VISIBLE
            rvEarthquakes.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            rvEarthquakes.visibility = View.VISIBLE
        }
    }

    private fun distanceInMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 3958.8 // Radius of Earth in miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun formatTime(time: Long?): String {
        if (time == null) return "Unknown time"
        val date = java.util.Date(time)
        return android.text.format.DateFormat
            .format("dd MMM, hh:mm a", date)
            .toString()
    }
}
