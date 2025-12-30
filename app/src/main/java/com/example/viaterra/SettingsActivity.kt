//package com.example.viaterra
//
//import android.os.Bundle
//import android.widget.Switch
//import android.widget.SeekBar
//import androidx.appcompat.app.AppCompatActivity
//import com.example.viaterra.util.SettingsManager
//import android.widget.TextView
//
//class SettingsActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_settings)
//
//        val switchEarthquake = findViewById<Switch>(R.id.switchEarthquake)
//        val switchTornado = findViewById<Switch>(R.id.switchTornado) // your XML key
//        val switchAutoLocation = findViewById<Switch>(R.id.switchAutoLocation)
//        val seekMinMag = findViewById<SeekBar>(R.id.seekBarMagnitude)
//        val tvMag = findViewById<TextView>(R.id.tvMagnitudeValue)
//        val seekRadius = findViewById<SeekBar>(R.id.seekBarRadius)
//        val tvRadius = findViewById<TextView>(R.id.tvRadiusValue)
//
//        // Load saved states
//        switchEarthquake.isChecked = SettingsManager.earthquakeAlertsEnabled(this)
//        switchTornado.isChecked = SettingsManager.tornadoAlertsEnabled(this)
//        switchAutoLocation.isChecked = SettingsManager.autoLocationEnabled(this)
//        seekMinMag.progress = (SettingsManager.getMinMagnitude(this) * 10).toInt()
//        tvMag.text = SettingsManager.getMinMagnitude(this).toString()
//        seekRadius.progress = SettingsManager.getRadius(this).toInt()
//        tvRadius.text = "${SettingsManager.getRadius(this)} km"
//
//        // Save when user changes
//        switchEarthquake.setOnCheckedChangeListener { _, isChecked ->
//            SettingsManager.setEarthquakeAlerts(this, isChecked)
//        }
//
//        switchTornado.setOnCheckedChangeListener { _, isChecked ->
//            SettingsManager.setTornadoAlerts(this, isChecked)
//        }
//
//        switchAutoLocation.setOnCheckedChangeListener { _, isChecked ->
//            SettingsManager.setAutoLocation(this, isChecked)
//        }
//
//        seekMinMag.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                val mag = progress / 10f
//                tvMag.text = mag.toString()
//                // Convert the Float 'mag' to Double
//                SettingsManager.setMinMagnitude(this@SettingsActivity, mag.toDouble())
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//
//
//        seekRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                tvRadius.text = "$progress km"
//                SettingsManager.setRadius(this@SettingsActivity, progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//    }
//}
package com.example.viaterra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.viaterra.util.SettingsManager
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switchEarthquake = findViewById<Switch>(R.id.switchEarthquake)
        val switchTornado = findViewById<Switch>(R.id.switchTornado)
        val switchFlood = findViewById<Switch>(R.id.switchFlood)

        // Load saved states
        switchEarthquake.isChecked = SettingsManager.earthquakeAlertsEnabled(this)
        switchTornado.isChecked = SettingsManager.tornadoAlertsEnabled(this)
        switchFlood.isChecked = SettingsManager.floodAlertsEnabled(this)

        // Save when user changes
        switchEarthquake.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setEarthquakeAlerts(this, isChecked)
        }

        switchTornado.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setTornadoAlerts(this, isChecked)
        }

        switchFlood.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setFloodAlerts(this, isChecked)
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}
