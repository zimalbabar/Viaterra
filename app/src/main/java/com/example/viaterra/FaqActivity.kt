package com.example.viaterra

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.databinding.ActivityFaqBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

data class FaqItem(val question: String, val answer: String, var isExpanded: Boolean = false)

class FaqActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaqBinding
    private val faqList = mutableListOf<FaqItem>()
    private lateinit var adapter: FaqAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        adapter = FaqAdapter(faqList, this)
        binding.rvFaq.layoutManager = LinearLayoutManager(this)
        binding.rvFaq.adapter = adapter

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            db = FirebaseFirestore.getInstance()
            fetchFaqs()
        } catch (e: Exception) {
            Log.e("FaqActivity", "Error initializing Firebase", e)
            loadFallbackData()
            Toast.makeText(this, "Using offline data (Firebase init failed)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchFaqs() {
        db.collection("faqs")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    loadFallbackData()
                    return@addOnSuccessListener
                }
                faqList.clear()
                for (document in result) {
                    val question = document.getString("question") ?: "Unknown Question"
                    val answer = document.getString("answer") ?: "No Answer Available"
                    faqList.add(FaqItem(question, answer))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("FaqActivity", "Error getting documents.", exception)
                loadFallbackData()
                Toast.makeText(this, "Error loading FAQs: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFallbackData() {
        faqList.clear()
        faqList.add(FaqItem("How do I find a nearby shelter?", "Check local maps or use the 'Map' feature in this app (mock)."))
        faqList.add(FaqItem("What should I put in an emergency kit?", "Water, canned food, flashlight, batteries, first aid kit."))
        faqList.add(FaqItem("Who do I call in an emergency?", "Call 112 or specific disaster helplines provided in the Recovery Guide."))
        faqList.add(FaqItem("Is tap water safe after a flood?", "No, assume it is contaminated. Boil it first."))
        adapter.notifyDataSetChanged()
    }
}

class FaqAdapter(private val faqs: List<FaqItem>, private val activity: AppCompatActivity) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    class FaqViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val tvAnswer: TextView = view.findViewById(R.id.tvAnswer)
        val layoutAnswer: LinearLayout = view.findViewById(R.id.layoutAnswerWithActions)
        val btnCall: Button = view.findViewById(R.id.btnCall)
        val btnShare: Button = view.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faq = faqs[position]
        holder.tvQuestion.text = faq.question
        holder.tvAnswer.text = faq.answer

        val isVisible = faq.isExpanded
        holder.layoutAnswer.visibility = if (isVisible) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            faq.isExpanded = !faq.isExpanded
            notifyItemChanged(position)
        }

        holder.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:112")
            activity.startActivity(intent)
        }

        holder.btnShare.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${faq.question}\n${faq.answer}")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            activity.startActivity(shareIntent)
        }
    }

    override fun getItemCount() = faqs.size
}
