package online.transporteari.transportecargaconductor.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import online.transporteari.transportecargaconductor.R
import online.transporteari.transportecargaconductor.models.History
import online.transporteari.transportecargaconductor.utils.RelativeTime

class HistoriesAdapter(val context: Activity, val histories: ArrayList<History>): RecyclerView.Adapter<HistoriesAdapter.HistoriesAdapterViewHolder>() {

    class HistoriesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textViewOrigin: TextView
        val textViewDestination: TextView
        val textViewDate: TextView

        init {
            textViewOrigin = view.findViewById(R.id.textViewOrigin)
            textViewDestination = view.findViewById(R.id.textViewDestination)
            textViewDate = view.findViewById(R.id.textViewDate)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_history, parent, false)
        return HistoriesAdapterViewHolder(view)
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {
        val history = histories[position]
        holder.textViewOrigin.text = history.origin
        holder.textViewDestination.text = history.destination
        if(history.timestamp != null){
            holder.textViewDate.text = RelativeTime.getTimeAgo(history.timestamp!!, context)
        }
    }
}