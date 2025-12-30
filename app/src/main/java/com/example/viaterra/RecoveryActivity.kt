package com.example.viaterra

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.viaterra.databinding.ActivityRecoveryBinding

class RecoveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecoveryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupExpandableCard(binding.tvHealthTitle, binding.tvHealthContent)
        setupExpandableCard(binding.tvWaterTitle, binding.tvWaterContent)
        setupExpandableCard(binding.tvHelplineTitle, binding.tvHelplineContent)
    }

    private fun setupExpandableCard(trigger: View, content: View) {
        trigger.setOnClickListener {
            if (content.visibility == View.VISIBLE) {
                content.visibility = View.GONE
            } else {
                content.visibility = View.VISIBLE
            }
        }
    }
}
