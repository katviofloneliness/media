package com.example.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class AmplitudeAdapter(private val amplitudes: List<Float>) :
    RecyclerView.Adapter<AmplitudeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmplitudeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_amplitude, parent, false)
        return AmplitudeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AmplitudeViewHolder, position: Int) {
        val amplitude = amplitudes[position]
        holder.Amplitude.text = amplitude.toString() + " dB"
    }

    override fun getItemCount(): Int {
        return amplitudes.size
    }
}