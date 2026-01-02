package com.example.viaterra.adapter

//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.viaterra.R
//import com.example.viaterra.Volcano
//
//class VolcanoAdapter(
//    private val list: List<Volcano>,
//    private val onItemClick: (Volcano) -> Unit
//) : RecyclerView.Adapter<VolcanoAdapter.ViewHolder>() {
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvVolcanoName: TextView = view.findViewById(R.id.tvVolcanoName)
//        val tvVolcanoLocation: TextView = view.findViewById(R.id.tvVolcanoLocation)
//        val tvVolcanoDistance: TextView = view.findViewById(R.id.tvVolcanoDistance)
//        val tvVolcanoStatus: TextView = view.findViewById(R.id.tvVolcanoStatus)
//        val tvLastActivity: TextView = view.findViewById(R.id.tvLastActivity)
//        val tvAlertLevel: TextView = view.findViewById(R.id.tvAlertLevel)
//
//        init {
//            view.setOnClickListener {
//                onItemClick(list[adapterPosition])
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_volcano, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val volcano = list[position]
//        holder.tvVolcanoName.text = volcano.name
//        holder.tvVolcanoLocation.text = volcano.location
//        holder.tvVolcanoDistance.text = volcano.distance
//        holder.tvVolcanoStatus.text = volcano.status
//        holder.tvLastActivity.text = volcano.lastActivity
//        holder.tvAlertLevel.text = volcano.alertLevel
//    }
//
//    override fun getItemCount(): Int = list.size
//}



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
