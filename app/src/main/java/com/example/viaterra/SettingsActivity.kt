package com.example.viaterra

import android.os.Bundle
import android.widget.Switch
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.viaterra.util.SettingsManager
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switchEarthquake = findViewById<Switch>(R.id.switchEarthquake)
        val switchTornado = findViewById<Switch>(R.id.switchVolcano) // your XML key
        val switchAutoLocation = findViewById<Switch>(R.id.switchAutoLocation)
        val seekMinMag = findViewById<SeekBar>(R.id.seekBarMagnitude)
        val tvMag = findViewById<TextView>(R.id.tvMagnitudeValue)
        val seekRadius = findViewById<SeekBar>(R.id.seekBarRadius)
        val tvRadius = findViewById<TextView>(R.id.tvRadiusValue)

        // Load saved states
        switchEarthquake.isChecked = SettingsManager.earthquakeAlertsEnabled(this)
        switchTornado.isChecked = SettingsManager.tornadoAlertsEnabled(this)
        switchAutoLocation.isChecked = SettingsManager.autoLocationEnabled(this)
        seekMinMag.progress = (SettingsManager.getMinMagnitude(this) * 10).toInt()
        tvMag.text = SettingsManager.getMinMagnitude(this).toString()
        seekRadius.progress = SettingsManager.getRadius(this).toInt()
        tvRadius.text = "${SettingsManager.getRadius(this)} km"

        // Save when user changes
        switchEarthquake.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setEarthquakeAlerts(this, isChecked)
        }

        switchTornado.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setTornadoAlerts(this, isChecked)
        }

        switchAutoLocation.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setAutoLocation(this, isChecked)
        }

        seekMinMag.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val mag = progress / 10f
                tvMag.text = mag.toString()
                // Convert the Float 'mag' to Double
                SettingsManager.setMinMagnitude(this@SettingsActivity, mag.toDouble())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        seekRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvRadius.text = "$progress km"
                SettingsManager.setRadius(this@SettingsActivity, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
