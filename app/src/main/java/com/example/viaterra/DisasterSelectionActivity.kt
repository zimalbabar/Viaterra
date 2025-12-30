package com.example.viaterra

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import android.widget.ImageButton
import android.widget.Toast

class DisasterSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_selection)

        // Initialize views
        val backButton: ImageButton = findViewById(R.id.backButton)
        val cardEarthquakes: MaterialCardView = findViewById(R.id.cardEarthquakes)
        val cardTornadoes: MaterialCardView = findViewById(R.id.cardTornadoes)
        val cardFloods: MaterialCardView = findViewById(R.id.cardFloods)

        // Back button click listener
        backButton.setOnClickListener {
            finish() // Close this activity and return to previous screen
        }

        // Earthquakes card click listener
        cardEarthquakes.setOnClickListener {
            val intent= Intent(this, EarthquakeActivity::class.java)
            startActivity(intent)
        }

        // Tornadoes card click listener
        cardTornadoes.setOnClickListener {
            val intent= Intent(this, TornadoActivity::class.java)
            startActivity(intent)
        }

        // Floods card click listener
        cardFloods.setOnClickListener {
            val intent= Intent(this, FloodActivity::class.java)
            startActivity(intent)
        }
    }

}
