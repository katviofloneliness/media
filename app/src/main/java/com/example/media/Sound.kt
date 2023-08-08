package com.example.media

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

interface MainActivityCallback {
    fun onRecordClicked()
    fun onStopRecordingClicked()
    fun onPlayClicked()
    fun onStopClicked()
}

class Sound : Fragment() {
    private var callback: MainActivityCallback? = null
    val database = FirebaseDatabase.getInstance()
    val amplitudesRef = database.getReference("amplitudes")

    private lateinit var locationManager: LocationManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivityCallback) {
            callback = context
        } else {
            throw IllegalStateException("Activity must implement MainActivityCallback")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sound, container, false)
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationManager = LocationManager(requireContext())

        val record = view.findViewById<Button>(R.id.record)
        val stopRecording = view.findViewById<Button>(R.id.stop_recording)
        val play = view.findViewById<Button>(R.id.play)
        val stop = view.findViewById<Button>(R.id.stop)
        val interval = view.findViewById<EditText>(R.id.interval)
        val switchLocation = view.findViewById<Switch>(R.id.locationSwitch)

        record.setOnClickListener {
            callback?.onRecordClicked()
        }
        stopRecording.setOnClickListener {
            callback?.onStopRecordingClicked()
        }
        play.setOnClickListener {
            callback?.onPlayClicked()
        }
        stop.setOnClickListener {
            callback?.onStopClicked()
        }
        switchLocation.setOnCheckedChangeListener{_, isChecked ->
            if(isChecked){
                locationManager.startLocationUpdates()
            } else {
                locationManager.stopLocationUpdates()
            }

        }

        interval.addTextChangedListener {
            val newInterval = it.toString().toLongOrNull()
            if(newInterval != null){
                locationManager.setLocationInterval(newInterval)
                //Save interval value in shared preferences
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putLong("interval", newInterval)
                editor.apply()
            }
        }

    }

}
