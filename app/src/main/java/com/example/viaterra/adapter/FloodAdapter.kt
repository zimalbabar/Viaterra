package com.example.viaterra.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.FloodActivity
import com.example.viaterra.R
import com.example.viaterra.model.GDACSAlert
import kotlin.text.ifEmpty

class FloodAlertAdapter(private val alerts: List<GDACSAlert>) :
    RecyclerView.Adapter<FloodAlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEventName: TextView = view.findViewById(R.id.tvEventName)
        val tvCountry: TextView = view.findViewById(R.id.tvCountry)
        val tvEventId: TextView = view.findViewById(R.id.tvEventId)
        val tvDateRange: TextView = view.findViewById(R.id.tvDateRange)
        val tvCoordinates: TextView = view.findViewById(R.id.tvCoordinates)
        val tvDescriptionPreview: TextView = view.findViewById(R.id.tvDescriptionPreview)
        val btnViewOnMap: Button = view.findViewById(R.id.btnViewOnMap)
        val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)
        val headerLayout: LinearLayout = view.findViewById(R.id.alertHeaderLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flood_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]

        holder.tvEventName.text = alert.eventName.ifEmpty { "Flood Event ${position + 1}" }
        holder.tvCountry.text = alert.country.ifEmpty { "Unknown Location" }
        holder.tvEventId.text = alert.eventId.ifEmpty { "--" }
        holder.tvDateRange.text = if (alert.fromDate.isNotEmpty() && alert.toDate.isNotEmpty()) {
            "${alert.fromDate} → ${alert.toDate}"
        } else "Date not available"
        holder.tvCoordinates.text = if (alert.latitude.isNotEmpty() && alert.longitude.isNotEmpty()) {
            "${alert.latitude}, ${alert.longitude}"
        } else "Coordinates not available"
        holder.tvDescriptionPreview.text = alert.description.ifEmpty { "No description available" }

        when (alert.alertlevel.uppercase()) {
            "RED" -> holder.headerLayout.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.pastel_red)
            )
            "ORANGE" -> holder.headerLayout.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.pastel_orange)
            )
            "GREEN" -> holder.headerLayout.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.pastel_green)
            )
            else -> holder.headerLayout.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.pastel_grey)
            )
        }



        holder.btnViewOnMap.setOnClickListener {
            if (alert.latitude.isNotEmpty() && alert.longitude.isNotEmpty()) {
                val uri = "geo:${alert.latitude},${alert.longitude}?q=${alert.latitude},${alert.longitude}(${alert.eventName})"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                holder.itemView.context.startActivity(intent)
            }
        }

        holder.btnViewDetails.setOnClickListener {
            val url = "https://www.gdacs.org/report.aspx?eventtype=FL&eventid=${alert.eventId}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = alerts.size
}