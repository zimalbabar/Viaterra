package com.example.viaterra

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.adapter.AlertAdapter
import com.example.viaterra.api.RetrofitClient
import com.example.viaterra.model.NoaaResponse
import com.example.viaterra.model.TornadoProperties
import com.example.viaterra.util.SettingsManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TornadoActivity : AppCompatActivity() {

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


        if (SettingsManager.autoLocationEnabled(this)) {
            tvLocation.text = "Auto-location enabled"
        } else {
            tvLocation.text = "Auto-location disabled"
        }

        // Load data initially
        loadTornadoData()
    }

    private fun loadTornadoData() {
        if (!SettingsManager.tornadoAlertsEnabled(this)) {
            tvAlertStatus.text = "Tornado alerts are disabled"
            rvTornadoes.adapter = AlertAdapter(emptyList())
            tvLastUpdate.text = ""
            return
        }

        tvLastUpdate.text = "Fetching data..."
        tvAlertStatus.text = "Loading tornado alerts..."

        // Retrofit API call
        RetrofitClient.Tornadoapi.getActiveAlerts().enqueue(object : Callback<NoaaResponse> {
            override fun onResponse(call: Call<NoaaResponse>, response: Response<NoaaResponse>) {
                if (response.isSuccessful) {
                    val tornadoAlerts: List<TornadoProperties> = response.body()?.features
                        ?.map { it.properties } ?: emptyList()

                    // Update RecyclerView
                    rvTornadoes.adapter = AlertAdapter(tornadoAlerts)

                    tvAlertStatus.text =
                        if (tornadoAlerts.isEmpty()) "No active tornado alerts"
                        else "Active Tornado Alerts (${tornadoAlerts.size})"

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
