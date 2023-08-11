package com.example.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class AmplitudeAdapter(private val data: List<AmplitudeData>) :
    RecyclerView.Adapter<AmplitudeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmplitudeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_amplitude, parent, false)
        return AmplitudeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AmplitudeViewHolder, position: Int) {
        val amplitudeData = data[position]
        holder.Amplitude.text = amplitudeData.amplitude.toString() + " dB"
        holder.Time.text = amplitudeData.time
        holder.Outcome.text = amplitudeData.outcome
    }

    override fun getItemCount(): Int {
        return data.size
    }
}