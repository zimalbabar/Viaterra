package com.example.viaterra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.viaterra.data.EmergencyContact
import com.example.viaterra.databinding.ActivitySosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.content.Context
import android.os.Build
import com.example.viaterra.data.EmergencyDatabase
import com.example.viaterra.utils.LocationHelper

class SosActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySosBinding
    private val database by lazy { EmergencyDatabase.getDatabase(this) }
    private val locationHelper by lazy { LocationHelper(this) }
    
    private val PERMISSIONS_REQUEST_CODE = 100
    private val PREFS_NAME = "SosPrefs"
    private val PREF_SHAKE_ENABLED = "shake_sos_enabled"
    private val PREF_VOICE_ENABLED = "voice_sos_enabled"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        startRippleAnimation()
        loadPreferences()

        // Check if triggered from Shake or Voice Service
        if (intent.getBooleanExtra("TRIGGER_SOS_AUTO", false)) {
            checkPermissionsAndTriggerSOS()
        }
    }

    private fun setupListeners() {
        binding.btnSos.setOnClickListener {
            checkPermissionsAndTriggerSOS()
        }

        binding.btnManageContacts.setOnClickListener {
            startActivity(Intent(this, EmergencyContactsActivity::class.java))
        }

        binding.btnViewProfile.setOnClickListener {
            startActivity(Intent(this, EmergencyProfileActivity::class.java))
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.switchShake.setOnCheckedChangeListener { _, isChecked ->
            savePreference(PREF_SHAKE_ENABLED, isChecked)
            if (isChecked) {
                startSosService()
            } else {
                stopSosService()
            }
        }

        binding.switchVoice.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    savePreference(PREF_VOICE_ENABLED, true)
                    startVoiceService()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
                    binding.switchVoice.isChecked = false
                }
            } else {
                savePreference(PREF_VOICE_ENABLED, false)
                stopVoiceService()
            }
        }
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        binding.switchShake.isChecked = prefs.getBoolean(PREF_SHAKE_ENABLED, false)
        binding.switchVoice.isChecked = prefs.getBoolean(PREF_VOICE_ENABLED, false)
        if (binding.switchShake.isChecked) startSosService()
        if (binding.switchVoice.isChecked) startVoiceService()
    }

    private fun savePreference(key: String, enabled: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, enabled).apply()
    }

    private fun startVoiceService() {
        val intent = Intent(this, VoiceSosService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopVoiceService() {
        stopService(Intent(this, VoiceSosService::class.java))
    }

    private fun startSosService() {
        val intent = Intent(this, SosService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopSosService() {
        stopService(Intent(this, SosService::class.java))
    }

    private fun startRippleAnimation() {
        val anim = ScaleAnimation(
            1f, 1.4f, 1f, 1.4f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim.duration = 1000
        anim.repeatCount = Animation.INFINITE
        anim.repeatMode = Animation.REVERSE
        binding.sosRipple1.startAnimation(anim)
    }

    private fun checkPermissionsAndTriggerSOS() {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        } else {
            showSosConfirmation()
        }
    }

    private fun showSosConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Confirm SOS")
            .setMessage("Are you sure you want to trigger SOS? This will call and message your emergency contacts with your live location.")
            .setPositiveButton("YES, TRIGGER") { _, _ ->
                triggerSosActions()
            }
            .setNeutralButton("WhatsApp Share") { _, _ ->
                triggerSosActions(shareWhatsAppToo = true)
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun triggerSosActions(shareWhatsAppToo: Boolean = false) {
        lifecycleScope.launch {
            val location = locationHelper.getCurrentLocation()
            val allContacts = withContext(Dispatchers.IO) {
                database.contactDao().getStaticAllContacts()
            }
            
            if (allContacts.isEmpty()) {
                Toast.makeText(this@SosActivity, "No emergency contacts found! Please add them first.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@SosActivity, EmergencyContactsActivity::class.java))
                return@launch
            }

            processSos(allContacts, location, shareWhatsAppToo)
            startBatterySaver()
        }
    }

    private fun startBatterySaver() {
        val intent = Intent(this, SosService::class.java).apply {
            putExtra("ENABLE_BATTERY_SAVER", true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    private fun processSos(contacts: List<EmergencyContact>, location: android.location.Location?, shareWhatsAppToo: Boolean) {
        // Sort contacts to ensure priority ones are first (Dao already does this, but being explicit)
        val sortedContacts = contacts.sortedByDescending { it.isPriority }
        
        // 1. Send SMS to all with location link
        sendSmsToAll(sortedContacts, location)

        // 2. Call the first priority contact (or first available contact)
        makeEmergencyCall(sortedContacts[0].phoneNumber)
        
        // 3. Optional WhatsApp Share to the primary contact
        if (shareWhatsAppToo) {
            shareOnWhatsApp(sortedContacts[0].phoneNumber, location)
        }
        
        val priorityName = if (sortedContacts[0].isPriority) "Priority Contact (${sortedContacts[0].name})" else sortedContacts[0].name
        Toast.makeText(this, "SOS Activated! Contacting $priorityName and ${sortedContacts.size-1} others.", Toast.LENGTH_LONG).show()
    }

    private fun sendSmsToAll(contacts: List<EmergencyContact>, location: android.location.Location?) {
        val smsManager: SmsManager = SmsManager.getDefault()
        val mapsLink = location?.let { locationHelper.getGoogleMapsLink(it) } ?: "Unavailable"
        val message = "🚨 EMERGENCY SOS! 🚨\nI need help immediately. My current location: $mapsLink\nPlease check on me."
        
        for (contact in contacts) {
            try {
                // Use divideMessage for safety with long strings
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(contact.phoneNumber, null, parts, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to single message if multipart fails
                try {
                    smsManager.sendTextMessage(contact.phoneNumber, null, "SOS! I need help. Location: $mapsLink", null, null)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun shareOnWhatsApp(phoneNumber: String, location: android.location.Location?) {
        val message = "EMERGENCY SOS! I need help. My location: ${location?.let { locationHelper.getGoogleMapsLink(it) } ?: "Unavailable"}"
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
            intent.data = Uri.parse(url)
            intent.setPackage("com.whatsapp")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeEmergencyCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showSosConfirmation()
            } else {
                Toast.makeText(this, "Permissions required for SOS", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
