package com.example.viaterra.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.R
import com.example.viaterra.data.EmergencyContact

class EmergencyContactAdapter(
    private val onCallClick: (EmergencyContact) -> Unit,
    private val onSmsClick: (EmergencyContact) -> Unit,
    private val onWhatsAppClick: (EmergencyContact) -> Unit,
    private val onEditClick: (EmergencyContact) -> Unit,
    private val onDeleteClick: (EmergencyContact) -> Unit,
    private val onPriorityClick: (EmergencyContact) -> Unit
) : RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder>() {

    private var contacts = listOf<EmergencyContact>()

    fun submitList(newContacts: List<EmergencyContact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvContactName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhoneNumber)
        val btnCall: ImageButton = itemView.findViewById(R.id.btnCall)
        val btnSms: ImageButton = itemView.findViewById(R.id.btnSms)
        val btnWhatsApp: ImageButton = itemView.findViewById(R.id.btnWhatsApp)
        val btnPriority: ImageButton = itemView.findViewById(R.id.btnPriority)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phoneNumber

        // Update priority star appearance
        if (contact.isPriority) {
            holder.btnPriority.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark)
            )
        } else {
            holder.btnPriority.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
            )
        }

        holder.btnCall.setOnClickListener { onCallClick(contact) }
        holder.btnSms.setOnClickListener { onSmsClick(contact) }
        holder.btnWhatsApp.setOnClickListener { onWhatsAppClick(contact) }
        holder.btnPriority.setOnClickListener { onPriorityClick(contact) }
        holder.btnEdit.setOnClickListener { onEditClick(contact) }
        holder.btnDelete.setOnClickListener { onDeleteClick(contact) }
    }

    override fun getItemCount(): Int = contacts.size
}