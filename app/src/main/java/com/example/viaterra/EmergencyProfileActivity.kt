package com.example.viaterra

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.viaterra.databinding.ActivityEmergencyProfileBinding

class EmergencyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyProfileBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("EmergencyProfile", Context.MODE_PRIVATE)

        loadProfileData()

        binding.btnSaveProfile.setOnClickListener {
            saveProfileData()
        }
    }

    private fun loadProfileData() {
        binding.etFullName.setText(sharedPreferences.getString("full_name", ""))
        binding.etBloodGroup.setText(sharedPreferences.getString("blood_group", ""))
        binding.etAllergies.setText(sharedPreferences.getString("allergies", ""))
        binding.etMedicalConditions.setText(sharedPreferences.getString("medical_conditions", ""))
        binding.etEmergencyNote.setText(sharedPreferences.getString("emergency_note", ""))
    }

    private fun saveProfileData() {
        val editor = sharedPreferences.edit()
        editor.putString("full_name", binding.etFullName.text.toString())
        editor.putString("blood_group", binding.etBloodGroup.text.toString())
        editor.putString("allergies", binding.etAllergies.text.toString())
        editor.putString("medical_conditions", binding.etMedicalConditions.text.toString())
        editor.putString("emergency_note", binding.etEmergencyNote.text.toString())
        
        if (editor.commit()) {
            Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
        }
    }
}
