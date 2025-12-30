package com.example.viaterra


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.iv_logo)

        // Start floating animation
        val floatAnim = AnimationUtils.loadAnimation(this, R.anim.floating)
        logo.startAnimation(floatAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, OpenActivity::class.java))
            }

            finish()
        }, 1800) // duration splash stays


    }
}

