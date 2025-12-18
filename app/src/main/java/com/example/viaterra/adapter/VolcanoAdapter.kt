package com.example.viaterra.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.R
import com.example.viaterra.Volcano

class VolcanoAdapter(
    private val list: List<Volcano>,
    private val onItemClick: (Volcano) -> Unit
) : RecyclerView.Adapter<VolcanoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVolcanoName: TextView = view.findViewById(R.id.tvVolcanoName)
        val tvVolcanoLocation: TextView = view.findViewById(R.id.tvVolcanoLocation)
        val tvVolcanoDistance: TextView = view.findViewById(R.id.tvVolcanoDistance)
        val tvVolcanoStatus: TextView = view.findViewById(R.id.tvVolcanoStatus)
        val tvLastActivity: TextView = view.findViewById(R.id.tvLastActivity)
        val tvAlertLevel: TextView = view.findViewById(R.id.tvAlertLevel)

        init {
            view.setOnClickListener {
                onItemClick(list[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_volcano, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val volcano = list[position]
        holder.tvVolcanoName.text = volcano.name
        holder.tvVolcanoLocation.text = volcano.location
        holder.tvVolcanoDistance.text = volcano.distance
        holder.tvVolcanoStatus.text = volcano.status
        holder.tvLastActivity.text = volcano.lastActivity
        holder.tvAlertLevel.text = volcano.alertLevel
    }

    override fun getItemCount(): Int = list.size
}
