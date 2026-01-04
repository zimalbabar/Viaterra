package com.example.viaterra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.viaterra.data.EmergencyContact
import com.example.viaterra.data.EmergencyDatabase
import com.example.viaterra.databinding.ActivityEmergencyContactsBinding
import kotlinx.coroutines.launch

import com.example.viaterra.utils.LocationHelper
import com.example.viaterra.ui.EmergencyContactAdapter


class EmergencyContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyContactsBinding
    private lateinit var adapter: EmergencyContactAdapter
    private val database by lazy { EmergencyDatabase.getDatabase(this) }
    private val locationHelper by lazy { LocationHelper(this) }

    private val REQUEST_CALL_PERMISSION = 1
    private var pendingCallNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeContacts()
    }

    private fun setupRecyclerView() {
        adapter = EmergencyContactAdapter(
            onCallClick = { contact -> makeCall(contact.phoneNumber) },
            onSmsClick = { contact -> sendSms(contact.phoneNumber) },
            onWhatsAppClick = { contact -> shareLocationOnWhatsApp(contact.phoneNumber) },
            onEditClick = { contact -> showContactDialog(contact) },
            onDeleteClick = { contact -> showDeleteConfirmation(contact) },
            onPriorityClick = { contact -> togglePriority(contact) }
        )
        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = adapter
    }

    private fun shareLocationOnWhatsApp(phoneNumber: String) {
        lifecycleScope.launch {
            val location = locationHelper.getCurrentLocation()
            val mapsLink = location?.let { locationHelper.getGoogleMapsLink(it) } ?: "Location Unavailable"
            val message = "🚨 EMERGENCY SOS! 🚨\nI need help! My current location is: $mapsLink\nPlease check on me immediately."
            
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                intent.data = Uri.parse(url)
                intent.setPackage("com.whatsapp")
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@EmergencyContactsActivity, "WhatsApp not installed!", Toast.LENGTH_SHORT).show()
                // Fallback to normal share if WhatsApp fails
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(shareIntent, "Share Emergency Alert"))
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddContact.setOnClickListener {
            showContactDialog()
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeContacts() {
        lifecycleScope.launch {
            database.contactDao().getAllContacts().collect { contacts ->
                adapter.submitList(contacts)
                binding.tvEmptyState.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showContactDialog(contact: EmergencyContact? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val etName = dialogView.findViewById<EditText>(R.id.etContactName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etContactPhone)

        contact?.let {
            etName.setText(it.name)
            etPhone.setText(it.phoneNumber)
        }

        AlertDialog.Builder(this)
            .setTitle(if (contact == null) "Add Emergency Contact" else "Edit Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (phone.length < 10) {
                    Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newContact = contact?.copy(name = name, phoneNumber = phone)
                    ?: EmergencyContact(name = name, phoneNumber = phone)
                
                lifecycleScope.launch {
                    if (contact == null) {
                        database.contactDao().insertContact(newContact)
                    } else {
                        database.contactDao().updateContact(newContact)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(contact: EmergencyContact) {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete ${contact.name}?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.contactDao().deleteContact(contact)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun togglePriority(contact: EmergencyContact) {
        lifecycleScope.launch {
            val updatedContact = contact.copy(isPriority = !contact.isPriority)
            database.contactDao().updateContact(updatedContact)
        }
    }

    private fun makeCall(phoneNumber: String) {
        pendingCallNumber = phoneNumber
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
        } else {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }
    }

    private fun sendSms(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
        intent.putExtra("sms_body", "EMERGENCY! I need help. This is an SOS message from Disaster Safe app.")
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pendingCallNumber?.let { makeCall(it) }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
