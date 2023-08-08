package com.example.media

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.media.databinding.ActivityMainBinding
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File


class MainActivity : AppCompatActivity(), MainActivityCallback {


    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }
    private val player by lazy {
        AndroidAudioPlayer(applicationContext)
    }
    private val controllerDND by lazy {
        AndroidDND(applicationContext)
    }
    private val controllerDecisionModel by lazy {
        AndroidDecisionModel(applicationContext)
    }
    private val locationManager by lazy {
        LocationManager(this)
    }
    private val REQUEST_CODE = 111

    private lateinit var notificationManager: NotificationManager
    private lateinit var audioManager: AudioManager
    private lateinit var binding: ActivityMainBinding

    private var audioFile: File? = null

    //var amplitudeList: MutableList<Float> = mutableListOf()
    var amplitudeList: MutableList<AmplitudeData> = mutableListOf()

    val database = FirebaseDatabase.getInstance()
    val amplitudesRef = database.getReference("amplitudes")

    //lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        Places.initialize(this, "AIzaSyDfYhFGAUAT97N405VnXl27My2zd6Oo1eY")

        //val record = findViewById<Button>(R.id.record1)
        //var path: String = cacheDir.path+"audio.mp3"
        //var mr = MediaRecorder()


        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //locationManager.startLocationUpdates()


        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ), REQUEST_CODE
        )
        controllerDND.checkPermissionDndMode(this)


        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Sound())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home_ampli -> replaceFragment(Home.newInstance(amplitudeList))
                R.id.map -> replaceFragment(MapsFragment())
                R.id.sound -> replaceFragment(Sound())
                else -> {
                }
            }
            //amplitudeList.clear()
            true
        }
        /*record.setOnClickListener {
          mr.setAudioSource(MediaRecorder.AudioSource.MIC)
          mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
          mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
          mr.setOutputFile(path)
          mr.prepare()
          mr.start()
      }*/

        amplitudesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Amplitudes data has changed, retrieve and process the new data
               // val amplitudeList: MutableList<Float> = mutableListOf()
                amplitudeList.clear()

                for (amplitudeSnapshot in dataSnapshot.children) {
                    val amplitude = amplitudeSnapshot.child("amplitudeDB").getValue(Float::class.java)
                    val time = amplitudeSnapshot.child("time").getValue(String::class.java)
                    amplitude?.let { amp ->
                        time?.let { t ->
                            amplitudeList.add(AmplitudeData(amp, t))
                        }
                    }
                }
                val fragment = Home.newInstance(amplitudeList)
                replaceFragment(fragment)
                // Pass the amplitudeList to the new fragment for display
                //val fragment = Home.newInstance(amplitudeList)
                //replaceFragment(fragment)
                // Add the fragment to your activity using FragmentManager
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    override fun onRecordClicked() {
        File(cacheDir, "audio.mp3").also {
            recorder.start(it)
            audioFile = it
        }
    }

    override fun onStopRecordingClicked() {
        try {
            var ampDB = recorder.getAmp()
            Toast.makeText(this, ampDB.toString(), Toast.LENGTH_LONG).show()
            recorder.stop()
            controllerDecisionModel.checkAmplitude(ampDB)

/*            if (ampDB > 70) controllerDND.enableDndMode()
            else controllerDND.disableDndMode()*/

        } catch (e: NullPointerException) {
        }

    }

    override fun onPlayClicked() {
        try {
            player.playFile(audioFile!!)
        } catch (e: NullPointerException) {
        }

    }

    override fun onStopClicked() {
        try {
            player.stop()
        } catch (e: NullPointerException) {
        }
    }

}