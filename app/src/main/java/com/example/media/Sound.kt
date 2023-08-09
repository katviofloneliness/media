package com.example.media

import AndroidAmplitudeMeter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//import com.example.media.AndroidAmplitudeMeter.AmplitudeCallback

interface MainActivityCallback {
    fun onRecordClicked()
    fun onStopRecordingClicked()
    fun onPlayClicked()
    fun onStopClicked()
}
/*interface AmplitudeCallback {
    fun onAmplitudeMeasured(amplitude: Double)
}*/

class Sound : Fragment(), AndroidAmplitudeMeter.AmplitudeCallback {
    private var callback: MainActivityCallback? = null
    val database = FirebaseDatabase.getInstance()
    val amplitudesRef = database.getReference("amplitudes")

    private lateinit var amplitudeMeter: AndroidAmplitudeMeter
    private lateinit var controllerDecisionModel: AndroidDecisionModel
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
        amplitudeMeter = AndroidAmplitudeMeter(requireContext(),this)
        controllerDecisionModel = AndroidDecisionModel(requireContext())

        val record = view.findViewById<Button>(R.id.record)
        val stopRecording = view.findViewById<Button>(R.id.stop_recording)
        val play = view.findViewById<Button>(R.id.play)
        val stop = view.findViewById<Button>(R.id.stop)
        val interval = view.findViewById<EditText>(R.id.interval)
        val switchLocation = view.findViewById<Switch>(R.id.locationSwitch)
        val soundSwitch = view.findViewById<Switch>(R.id.soundSwitch)
        val soundSwitchSimple = view.findViewById<Switch>(R.id.soundSwitchSimple)

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
        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                amplitudeMeter.start()
            } else {
                amplitudeMeter.stop()
            }
        }
        soundSwitchSimple.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                amplitudeMeter.startSimple()
            } else {
                amplitudeMeter.stop()
            }
        }



        interval.addTextChangedListener {
            val newInterval = it.toString().toLongOrNull()
            if(newInterval != null){
                locationManager.setLocationInterval(newInterval)
                // Save interval value in shared preferences
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putLong("interval", newInterval)
                editor.apply()
            }
        }

    }
    override fun onAmplitudeMeasured(amplitudeDB: Double) {
        val database = FirebaseDatabase.getInstance()
        val amplitudeRef = database.getReference("amplitudesInterval")
        val amplitudeKey = amplitudeRef.push().key

        val currentTime = System.currentTimeMillis()
        val amplitudeTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))
        var outcome = controllerDecisionModel.checkAmplitude(amplitudeDB)

        val amplitudeData = hashMapOf("amplitudeDB" to amplitudeDB, "time" to amplitudeTime, "outcome" to outcome)
        amplitudeKey?.let { key ->
            amplitudeRef.child(key).setValue(amplitudeData)
        }
        Log.d("Amplitude", "Measured amplitude: $amplitudeDB")
    }

    override fun onAmplitudeMeasuredSimple(amplitudeDB: Double) {
        val database = FirebaseDatabase.getInstance()
        val amplitudeRef = database.getReference("amplitudesIntervalSimple")
        val amplitudeKey = amplitudeRef.push().key

        val currentTime = System.currentTimeMillis()
        val amplitudeTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))
        var outcome = controllerDecisionModel.checkAmplitudeSimple(amplitudeDB)

        val amplitudeData = hashMapOf("amplitudeDB" to amplitudeDB, "time" to amplitudeTime, "outcome" to outcome)
        amplitudeKey?.let { key ->
            amplitudeRef.child(key).setValue(amplitudeData)
        }
        Log.d("AmplitudeSimple", "Measured amplitude simple: $amplitudeDB")
    }


}
