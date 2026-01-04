package com.example.viaterra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.viaterra.databinding.ActivityNearbyServicesBinding

class NearbyServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNearbyServicesBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearbyServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNearbyHospitals.setOnClickListener {
            checkPermissionAndOpenMaps("hospital")
        }

        binding.btnNearbyPolice.setOnClickListener {
            checkPermissionAndOpenMaps("police station")
        }
    }

    private fun checkPermissionAndOpenMaps(query: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            openGoogleMaps(query)
        }
    }

    private fun openGoogleMaps(query: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Fallback for when Google Maps app is not installed
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            startActivity(browserIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, but we don't know which button was clicked.
                // User will need to click again.
                Toast.makeText(this, "Location permission granted. Please select a service again.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.location_permission_required), Toast.LENGTH_LONG).show()
            }
        }
    }
}
