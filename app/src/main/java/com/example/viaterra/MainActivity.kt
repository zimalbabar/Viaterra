//package com.example.viaterra
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.example.viaterra.databinding.ActivityMainBinding
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.btnChatbot.setOnClickListener {
//            startActivity(Intent(this, ChatbotActivity::class.java))
//        }
//
//        binding.btnRecovery.setOnClickListener {
//            startActivity(Intent(this, RecoveryActivity::class.java))
//        }
//
//        binding.btnFaq.setOnClickListener {
//            startActivity(Intent(this, FaqActivity::class.java))
//        }
//    }
//}