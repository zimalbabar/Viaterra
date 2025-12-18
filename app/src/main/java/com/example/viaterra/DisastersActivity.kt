package com.example.viaterra

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.adapter.EarthquakeAdapter
import com.example.viaterra.adapter.VolcanoAdapter
import android.Manifest

import android.widget.Toast
import com.example.viaterra.Earthquake
import com.example.viaterra.Volcano
import com.example.viaterra.api.RetrofitClient
import com.example.viaterra.model.EarthquakeResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class DisastersActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 100


    lateinit var rvEarthquakes: RecyclerView
    lateinit var tvAlertStatus: TextView
    lateinit var tvLastUpdate: TextView
    lateinit var rvVolcanoes: RecyclerView

    lateinit var tvLocation: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disasters)

        rvEarthquakes = findViewById(R.id.rvEarthquakes)
        tvAlertStatus = findViewById(R.id.tvAlertStatus)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)

        rvEarthquakes.layoutManager = LinearLayoutManager(this)

        rvVolcanoes = findViewById(R.id.rvVolcanoes)
        rvVolcanoes.layoutManager = LinearLayoutManager(this)

        tvLocation = findViewById(R.id.tvLocation)



        loadVolcanoData()

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            val intent= Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

    }

//    private fun loadDashboardData() {
//        RetrofitClient.api.getEarthquakes()
//            .enqueue(object : retrofit2.Callback<EarthquakeResponse> {
//
//                override fun onResponse(
//                    call: retrofit2.Call<EarthquakeResponse>,
//                    response: retrofit2.Response<EarthquakeResponse>
//                ) {
//                    val earthquakes = response.body()?.features?.map {
//                        Earthquake(
//                            magnitude = it.properties.mag?.toString() ?: "N/A",
//                            location = it.properties.place ?: "Unknown",
//                            time = formatTime(it.properties.time)
//                        )
//                    } ?: emptyList()
//
//                    rvEarthquakes.adapter =
//                        EarthquakeAdapter(earthquakes) { earthquake ->
//                            val intent = Intent(this@MainActivity, AlertActivity::class.java)
//                            intent.putExtra("magnitude", earthquake.magnitude)
//                            intent.putExtra("location", earthquake.location)
//                            intent.putExtra("time", earthquake.time)
//                            startActivity(intent)
//                        }
//
//
//                    tvAlertStatus.text = "Live Earthquake Data"
//                    tvLastUpdate.text = "Last Updated: Just now"
//                }
//
//                override fun onFailure(
//                    call: retrofit2.Call<EarthquakeResponse>,
//                    t: Throwable
//                ) {
//                    tvAlertStatus.text = "Failed to load data"
//                }
//            })
//    }



    private fun loadDashboardData(userLat: Double, userLon: Double) {
        RetrofitClient.api.getEarthquakes()
            .enqueue(object : retrofit2.Callback<EarthquakeResponse> {
                override fun onResponse(
                    call: retrofit2.Call<EarthquakeResponse>,
                    response: retrofit2.Response<EarthquakeResponse>
                ) {
                    val earthquakes = response.body()?.features?.mapNotNull {
                        val quakeLat = it.geometry.coordinates[1]
                        val quakeLon = it.geometry.coordinates[0]
                        val dist = distanceInMiles(userLat, userLon, quakeLat, quakeLon)

                        if (dist <= 1000) { // within 100 miles
                            Earthquake(
                                magnitude = it.properties.mag?.toString() ?: "N/A",
                                location = it.properties.place ?: "Unknown",
                                time = formatTime(it.properties.time),
                                distance = String.format("%.1f miles", dist)
                            )
                        } else null
                    } ?: emptyList()

                    rvEarthquakes.adapter = EarthquakeAdapter(earthquakes) { earthquake ->
                        val intent = Intent(this@DisastersActivity, AlertActivity::class.java)
                        intent.putExtra("magnitude", earthquake.magnitude)
                        intent.putExtra("location", earthquake.location)
                        intent.putExtra("time", earthquake.time)
                        startActivity(intent)
                    }

                    tvAlertStatus.text =
                        if (earthquakes.isEmpty()) "No nearby quakes" else "Nearby Earthquakes"
                    tvLastUpdate.text = "Last Updated: Just now"
                }

                override fun onFailure(call: retrofit2.Call<EarthquakeResponse>, t: Throwable) {
                    tvAlertStatus.text = "Failed to load data"
                }
            })
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

    private fun loadVolcanoData() {
        val volcanoes = listOf(
            Volcano(
                "Mount St. Helens",
                "Washington, USA",
                "120 km",
                "Seismic activity detected",
                "3 days ago",
                "NORMAL"
            ),
            Volcano("Mount Fuji", "Japan", "250 km", "No activity", "7 days ago", "NORMAL"),
            Volcano("Eyjafjallajökull", "Iceland", "300 km", "Minor tremors", "1 day ago", "WATCH")
        )

        rvVolcanoes.adapter = VolcanoAdapter(volcanoes) { volcano ->
            // TODO: Open Volcano Detail Activity if you want
        }


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
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLat = location.latitude
                val userLon = location.longitude
                tvLocation.text = "Your Location: $userLat, $userLon"

                // Load earthquakes with location
                loadDashboardData(userLat, userLon) // ✅ This is correct
            } else {
                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

}


data class Earthquake(
    val magnitude: String,
    val location: String,
    val time: String,
    val distance: String
)

data class Volcano(
    val name: String,
    val location: String,
    val distance: String,
    val status: String,
    val lastActivity: String,
    val alertLevel: String
)


data class EarthquakeGeometry(
    val coordinates: List<Double> // [longitude, latitude, depth]
)