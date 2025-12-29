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
            navigateToDisasterDetails("Earthquakes")
            val intent= Intent(this, EarthquakeActivity::class.java)
            startActivity(intent)
        }

        // Tornadoes card click listener
        cardTornadoes.setOnClickListener {
            navigateToDisasterDetails("Tornadoes")
            val intent= Intent(this, TornadoActivity::class.java)
            startActivity(intent)
        }

        // Floods card click listener
        cardFloods.setOnClickListener {
            navigateToDisasterDetails("Floods")
            val intent= Intent(this, FloodActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToDisasterDetails(disasterType: String) {
        // For now, just show a toast message
        // Later, you can create a new activity to show detailed disaster information
        Toast.makeText(
            this,
            "Selected: $disasterType\nOpening tracking details...",
            Toast.LENGTH_SHORT
        ).show()

        // TODO: Uncomment when you create the DisasterDetailsActivity
        // val intent = Intent(this, DisasterDetailsActivity::class.java)
        // intent.putExtra("DISASTER_TYPE", disasterType)
        // startActivity(intent)
    }
}
