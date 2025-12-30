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
    private val contacts: MutableList<EmergencyContact>,
    private val onEditClick: (EmergencyContact, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val onPriorityClick: (EmergencyContact, Int) -> Unit
) : RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvContactName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhoneNumber)
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

        holder.btnPriority.setOnClickListener {
            onPriorityClick(contact, position)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(contact, position)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = contacts.size
}