package com.example.media

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AmplitudeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val Amplitude: TextView = itemView.findViewById(R.id.textViewAmplitude)
    val Time: TextView = itemView.findViewById(R.id.textViewTime)
    val Outcome: TextView = itemView.findViewById(R.id.textViewOutcome)
}