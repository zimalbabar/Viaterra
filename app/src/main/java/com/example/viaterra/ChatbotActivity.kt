package com.example.viaterra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.databinding.ActivityChatbotBinding
import java.util.*

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        // Initial greeting
        addBotMessage("Hello! I am your Disaster Advisor. Ask me about Earthquakes, Floods, or Fires.")

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                addUserMessage(text)
                val response = getBotResponse(text)
                addBotMessage(response)
                binding.etMessage.text.clear()
            }
        }
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(text: String) {
        messages.add(ChatMessage(text, false))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }

    private fun getBotResponse(query: String): String {
        val q = query.lowercase(Locale.getDefault())
        return when {
            q.contains("earthquake") -> "Earthquake Safety:\n1. Drop, Cover, and Hold On.\n2. Stay away from windows.\n3. If outdoors, stay in open areas."
            q.contains("flood") -> "Flood Safety:\n1. Move to higher ground immediately.\n2. Do not walk or drive through flood waters.\n3. Turn off utilities if safe."
            q.contains("fire") -> "Fire Safety:\n1. Stop, Drop, and Roll if clothes catch fire.\n2. Crawl low under smoke.\n3. Use fire extinguisher for small fires."
            q.contains("hello") || q.contains("hi") -> "Hi there! Stay safe. Ask me about a disaster."
            else -> "I can help with Earthquakes, Floods, and Fires. Please ask specifically about these events."
        }
    }


}

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val cardMessage: androidx.cardview.widget.CardView = view.findViewById(R.id.cardMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = message.text
        
        // Simple styling for user vs bot
        val params = holder.cardMessage.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        if (message.isUser) {
            holder.cardMessage.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD")) // Light Blue
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.marginStart = 64
            params.marginEnd = 0
        } else {
            holder.cardMessage.setCardBackgroundColor(android.graphics.Color.WHITE)
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
            params.marginStart = 0
            params.marginEnd = 64
        }
        holder.cardMessage.layoutParams = params
    }

    override fun getItemCount() = messages.size
}
