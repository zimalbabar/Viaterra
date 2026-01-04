package com.example.viaterra

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.viaterra.SosActivity
import com.example.viaterra.databinding.ActivityLockScreenEmergencyBinding

class LockScreenEmergencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenEmergencyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        binding = ActivityLockScreenEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileSummary()
        startRippleAnimation()

        binding.btnLockSos.setOnClickListener {
            val intent = Intent(this, SosActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("TRIGGER_SOS_AUTO", true)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun loadProfileSummary() {
        val sharedPreferences = getSharedPreferences("EmergencyProfile", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("full_name", "---")
        val blood = sharedPreferences.getString("blood_group", "---")
        val allergies = sharedPreferences.getString("allergies", "---")

        binding.tvLockName.text = "Name: $name"
        binding.tvLockBlood.text = "Blood Group: $blood"
        binding.tvLockAllergies.text = "Allergies: $allergies"
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
        binding.lockSosRipple.startAnimation(anim)
    }
}
