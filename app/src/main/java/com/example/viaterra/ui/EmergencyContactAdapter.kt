// FIX 1: Corrected the package name to match your project structure
package com.example.viaterra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// FIX 2: Corrected the import for the EmergencyContact data c
import com.example.viaterra.data.EmergencyContact

class EmergencyContactAdapter(
    private val contacts: List<EmergencyContact>
) : RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // FIX 3: Corrected the view IDs to match your other files (tvContactName, tvPhoneNumber)
        val tvName: TextView = itemView.findViewById(R.id.tvContactName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhoneNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        // FIX 4: Corrected the layout file name to match your other files (item_contact)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        // These lines will now work because the imports and data class are correct
        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phoneNumber
    }

    override fun getItemCount(): Int = contacts.size
}
