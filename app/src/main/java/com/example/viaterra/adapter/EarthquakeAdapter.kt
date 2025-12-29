package com.example.viaterra.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.viaterra.Earthquake
import com.example.viaterra.R

class EarthquakeAdapter(
    private val list: List<Earthquake>,
    private val onItemClick: (Earthquake) -> Unit
) : RecyclerView.Adapter<EarthquakeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMagnitude: TextView = view.findViewById(R.id.tvMagnitude)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvTime: TextView = view.findViewById(R.id.tvTime)

        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        init {
            view.setOnClickListener {
                onItemClick(list[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_earthquake, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quake = list[position]
        holder.tvMagnitude.text = quake.magnitude
        holder.tvLocation.text = quake.location
        holder.tvTime.text = quake.time
        holder.tvDistance.text = "Distance from you: ${quake.distance}" // <-- Add this line
    }


    override fun getItemCount(): Int = list.size
}
