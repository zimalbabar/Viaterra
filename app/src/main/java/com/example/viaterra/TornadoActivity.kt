package com.example.viaterra


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.adapter.AlertAdapter
import com.example.viaterra.api.RetrofitClient
import com.example.viaterra.model.NoaaResponse
import com.example.viaterra.model.TornadoProperties
import com.example.viaterra.util.SettingsManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TornadoActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    private lateinit var rvTornadoes: RecyclerView
    private lateinit var tvAlertStatus: TextView
    private lateinit var tvLastUpdate: TextView
    private lateinit var tvLocation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tornado)

        // Initialize views
        rvTornadoes = findViewById(R.id.rvTornadoes)
        tvAlertStatus = findViewById(R.id.tvAlertStatus)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        tvLocation = findViewById(R.id.tvLocation)

        rvTornadoes.layoutManager = LinearLayoutManager(this)

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Refresh button
        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            Toast.makeText(this, "Fetching tornado data...", Toast.LENGTH_SHORT).show()
            loadTornadoData()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()
        loadTornadoData()
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
                val userLat = location.latitude
                val userLon = location.longitude
                tvLocation.text = String.format("Your Location: %.4f, %.4f", userLat, userLon)
            } else {
                tvLocation.text = "Could not get location"
            }
        }
    }

    private fun loadTornadoData() {
        val tornadoesEnabled = SettingsManager.tornadoAlertsEnabled(this)

        if (!tornadoesEnabled) {
            tvAlertStatus.text = "Tornado alerts are disabled"
            rvTornadoes.adapter = AlertAdapter(emptyList())
            return
        }

        tvLastUpdate.text = "Fetching data..."

        RetrofitClient.Tornadoapi.getActiveAlerts().enqueue(object : Callback<NoaaResponse> {
            override fun onResponse(call: Call<NoaaResponse>, response: Response<NoaaResponse>) {
                if (response.isSuccessful) {
                    val tornadoAlerts: List<TornadoProperties> = response.body()?.features
                        ?.map { it.properties } ?: emptyList()

                    rvTornadoes.adapter = AlertAdapter(tornadoAlerts)

                    tvAlertStatus.text =
                        if (tornadoAlerts.isEmpty()) "No active tornado alerts" else "Active Tornado Alerts (${tornadoAlerts.size})"
                    tvLastUpdate.text = "Last Updated: Just now"
                } else {
                    tvAlertStatus.text = "Failed to load data"
                    tvLastUpdate.text = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<NoaaResponse>, t: Throwable) {
                tvAlertStatus.text = "Failed to load tornado alerts"
                tvLastUpdate.text = "Error: ${t.message}"
                Toast.makeText(this@TornadoActivity, "Failed to load tornado alerts", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
