package com.example.viaterra

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.viaterra.data.EmergencyContact
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvGreeting: TextView
    private lateinit var ivProfile: ImageView

    private val PICK_IMAGE_REQUEST = 1001

    // SharedPreferences
    private val PREFS_NAME = "user_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvGreeting = findViewById(R.id.tv_greeting)
        ivProfile = findViewById(R.id.iv_profile)

        loadUserData()

        ivProfile.setOnClickListener {
            selectProfileImage()
        }

//        findViewById<ImageView>(R.id.btn_settings).setOnClickListener {
//            val intent = Intent(this, SettingsActivity::class.java)
//            startActivity(intent)
//        }

        // Dashboard cards
        findViewById<MaterialCardView>(R.id.card_track_disasters).setOnClickListener {
            startActivity(Intent(this, DisasterSelectionActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_maps).setOnClickListener {
            Toast.makeText(this, "Opening Maps...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.card_sos).setOnClickListener {
//            Toast.makeText(this, "Opening Emergency Contacts...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.card_what_to_do).setOnClickListener {
            Toast.makeText(this, "Opening Safety Guidelines...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.card_chatbot).setOnClickListener {
//            Toast.makeText(this, "Opening Chatbot...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_faqs).setOnClickListener {
//            Toast.makeText(this, "Opening Chatbot...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, FaqActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_post_disaster).setOnClickListener {
//            Toast.makeText(this, "Opening Chatbot...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RecoveryActivity::class.java))
        }

        findViewById<TextView>(R.id.tv_logout_link).setOnClickListener {
            logoutUser()
        }

        findViewById<ImageView>(R.id.btn_settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
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

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


    // ---------------- USER DATA ----------------

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "User"
                tvGreeting.text = username

                val localImageUri = getProfileImageUri()
                if (localImageUri != null) {
                    Glide.with(this)
                        .load(localImageUri)
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfile)
                } else {
                    ivProfile.setImageResource(R.drawable.ic_profile)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- IMAGE PICKING ----------------

    private fun selectProfileImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(
            Intent.createChooser(intent, "Select Profile Picture"),
            PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == Activity.RESULT_OK &&
            data?.data != null
        ) {
            val imageUri = data.data!!

            saveProfileImageUri(imageUri)

            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)

            Toast.makeText(this, "Profile picture updated locally", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- SHARED PREFS (USER-SPECIFIC) ----------------

    private fun saveProfileImageUri(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        prefs.edit()
            .putString("profile_image_$userId", uri.toString())
            .apply()
    }

    private fun getProfileImageUri(): Uri? {
        val userId = auth.currentUser?.uid ?: return null
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val uriString = prefs.getString("profile_image_$userId", null)
        return uriString?.let { Uri.parse(it) }
    }
}
