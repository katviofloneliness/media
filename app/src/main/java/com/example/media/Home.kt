package com.example.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Home : Fragment() {


    companion object {
        private const val ARG_AMPLITUDES = "amplitudes"

        fun newInstance(amplitudes: List<AmplitudeData>): Home {
            val fragment = Home()
            val args = Bundle()
            args.putParcelableArray(ARG_AMPLITUDES, amplitudes.toTypedArray())
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val text = view.findViewById<TextView>(R.id.home_txt)
        val amplitudes = arguments?.getParcelableArray(ARG_AMPLITUDES) as? Array<AmplitudeData>?
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewAmplitudes)

        if (amplitudes != null && amplitudes.isNotEmpty()) {
            recyclerView.adapter = AmplitudeAdapter(amplitudes.toList())
            recyclerView.layoutManager = LinearLayoutManager(context)
        } else {
            text.text = "No amplitudes found"
        }

        return view
    }
}
