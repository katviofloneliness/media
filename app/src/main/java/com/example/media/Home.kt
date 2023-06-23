package com.example.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.merge

class Home : Fragment() {


    companion object {
        private const val ARG_AMPLITUDES = "amplitudes"

        fun newInstance(amplitudes: List<Float>): Home {
            val fragment = Home()
            val args = Bundle()
            args.putFloatArray(ARG_AMPLITUDES, amplitudes.toFloatArray())
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val text = view.findViewById<TextView>(R.id.home_txt)
        val amplitudes = arguments?.getFloatArray(ARG_AMPLITUDES)?.toList()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewAmplitudes)

        if (amplitudes != null && amplitudes.isNotEmpty()) {
            recyclerView.adapter = AmplitudeAdapter(amplitudes)
            recyclerView.layoutManager = LinearLayoutManager(context)
            //text.text = amplitudes.joinToString(",") + "test"
        } else {
            text.text = "No amplitudes found"
        }

        return view
    }
}
