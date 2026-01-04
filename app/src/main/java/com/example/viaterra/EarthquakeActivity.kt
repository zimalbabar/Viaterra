package com.example.viaterra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.adapter.EarthquakeAdapter
import com.example.viaterra.api.RetrofitClient
import com.example.viaterra.data.Earthquake
import com.example.viaterra.model.EarthquakeResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.*

class EarthquakeActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var rvEarthquakes: RecyclerView
    private lateinit var tvAlertStatus: TextView
    private lateinit var tvLastUpdate: TextView
    private lateinit var tvLocation: TextView
    private lateinit var seekBarRadius: SeekBar
    private lateinit var tvRadiusValue: TextView
    private lateinit var emptyStateLayout: LinearLayout

    private var currentUserLat = 0.0
    private var currentUserLon = 0.0

    private val minMag = 2.5 // Minimum earthquake magnitude
    private val minRadiusKm = 50
    private val maxRadiusKm = 2000


    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) getDeviceLocation()
            else Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earthquake)

        rvEarthquakes = findViewById(R.id.rvEarthquakes)
        tvAlertStatus = findViewById(R.id.tvAlertStatus)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        tvLocation = findViewById(R.id.tvLocation)
        seekBarRadius = findViewById(R.id.seekBarRadius)
        tvRadiusValue = findViewById(R.id.tvRadiusValue)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)

        rvEarthquakes.layoutManager = LinearLayoutManager(this)

        // Initialize SeekBar
        seekBarRadius.max = maxRadiusKm - minRadiusKm // progress 0 → 50 km
        val initialRadius = 100
        seekBarRadius.progress = initialRadius - minRadiusKm
        tvRadiusValue.text = "Current: $initialRadius km"

        seekBarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvRadiusValue.text = "Current: ${progress + minRadiusKm} km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val radius = (seekBar?.progress ?: 0) + minRadiusKm
                Toast.makeText(this@EarthquakeActivity, "Radius updated to $radius km", Toast.LENGTH_SHORT).show()
                if (currentUserLat != 0.0 && currentUserLon != 0.0) {
                    loadEarthquakeData(currentUserLat, currentUserLon, radius)
                }
            }
        })

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            Toast.makeText(this, "Fetching earthquake data...", Toast.LENGTH_SHORT).show()
            requestLocation()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocation()
    }

    // ---------------- Location ----------------
    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentUserLat = location.latitude
                currentUserLon = location.longitude
                tvLocation.text = "Your Location: %.4f, %.4f".format(currentUserLat, currentUserLon)

                val radius = seekBarRadius.progress + minRadiusKm
                loadEarthquakeData(currentUserLat, currentUserLon, radius)
            } else {
                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------- Earthquake Data ----------------
    private fun loadEarthquakeData(userLat: Double, userLon: Double, maxRadius: Int) {
        tvLastUpdate.text = "Fetching data..."

        RetrofitClient.api.getEarthquakes().enqueue(object : Callback<EarthquakeResponse> {
            override fun onResponse(call: Call<EarthquakeResponse>, response: Response<EarthquakeResponse>) {
                val earthquakes = filterNearbyEarthquakes(response.body()?.features, userLat, userLon, maxRadius, minMag)
                updateRecyclerView(earthquakes)
            }

            override fun onFailure(call: Call<EarthquakeResponse>, t: Throwable) {
                tvAlertStatus.text = "Failed to load data"
                tvLastUpdate.text = "Error: ${t.message}"
                showEmptyState(false)
            }
        })
    }

    private fun filterNearbyEarthquakes(
        features: List<com.example.viaterra.model.Feature>?,
        userLat: Double,
        userLon: Double,
        maxRadius: Int,
        minMag: Double
    ): List<Earthquake> = features?.mapNotNull {
        val quakeLat = it.geometry.coordinates[1]
        val quakeLon = it.geometry.coordinates[0]
        val distanceKm = distanceInMiles(userLat, userLon, quakeLat, quakeLon) * 1.609

        if (distanceKm <= maxRadius && (it.properties.mag ?: 0.0) >= minMag)
            Earthquake(
                magnitude = it.properties.mag?.toString() ?: "N/A",
                location = it.properties.place ?: "Unknown",
                time = formatTime(it.properties.time),
                distance = "%.1f KM".format(distanceKm)
            )
        else null
    } ?: emptyList()

    private fun updateRecyclerView(earthquakes: List<Earthquake>) {
        if (earthquakes.isEmpty()) {
            showEmptyState(true)
            tvAlertStatus.text = "No nearby earthquakes"
        } else {
            showEmptyState(false)
            rvEarthquakes.adapter = EarthquakeAdapter(earthquakes) { earthquake ->
                val intent = Intent(this, AlertActivity::class.java).apply {
                    putExtra("magnitude", earthquake.magnitude)
                    putExtra("location", earthquake.location)
                    putExtra("time", earthquake.time)
                }
                startActivity(intent)
            }
            tvAlertStatus.text = "Nearby Earthquakes (${earthquakes.size})"
        }
        tvLastUpdate.text = "Last Updated: Just now"
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        rvEarthquakes.visibility = if (show) View.GONE else View.VISIBLE
    }

    // ---------------- Utility ----------------
    //uses haversince formula to calculate distance between two points on earth
    private fun distanceInMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 3958.8 // Radius of Earth in miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun formatTime(time: Long?): String {
        if (time == null) return "Unknown time"
        val date = java.util.Date(time)
        return android.text.format.DateFormat.format("dd MMM, hh:mm a", date).toString()
    }

    private fun Double.pow(exp: Double) = Math.pow(this, exp)
}
