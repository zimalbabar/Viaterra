package com.example.viaterra.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.R
import com.example.viaterra.model.TornadoProperties

class AlertAdapter(private val alerts: List<TornadoProperties>) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val event: TextView = view.findViewById(R.id.event)
        val area: TextView = view.findViewById(R.id.area)
        val headline: TextView = view.findViewById(R.id.headline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tornado_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.event.text = alert.event
        holder.area.text = alert.areaDesc
        holder.headline.text = alert.headline
    }

    override fun getItemCount() = alerts.size
}
