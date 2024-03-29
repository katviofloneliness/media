package com.example.media

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
    private val REQUEST_CODE = 111

    private lateinit var notificationManager: NotificationManager
    private lateinit var audioManager: AudioManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: WakeLock

    private var audioFile: File? = null


    var amplitudeList: MutableList<AmplitudeData> = mutableListOf()

    val database = FirebaseDatabase.getInstance()
    val amplitudesRef = database.getReference("amplitudes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        Places.initialize(this, "AIzaSyDfYhFGAUAT97N405VnXl27My2zd6Oo1eY")
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"media:WakeLockTag")
        //val record = findViewById<Button>(R.id.record1)
        //var path: String = cacheDir.path+"audio.mp3"
        //var mr = MediaRecorder()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.DISABLE_KEYGUARD
            ), REQUEST_CODE
        )
        controllerDND.checkPermissionDndMode(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(General())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home_ampli -> replaceFragment(Home.newInstance(amplitudeList))
                R.id.map -> replaceFragment(MapsFragment())
                R.id.sound -> replaceFragment(General())
                else -> {
                }
            }
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
                amplitudeList.clear()

                for (amplitudeSnapshot in dataSnapshot.children) {
                    val amplitude =
                        amplitudeSnapshot.child("amplitudeDB").getValue(String::class.java)
                    val time = amplitudeSnapshot.child("time").getValue(String::class.java)
                    val outcome = amplitudeSnapshot.child("outcome").getValue(String::class.java)
                    amplitude?.let { amp ->
                        time?.let { t ->
                            outcome?.let {out ->
                                amplitudeList.add(AmplitudeData(amp, t, out))
                            }
                        }
                    }
                }
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
            //Toast.makeText(this, ampDB.toString().format("%.2f",ampDB), Toast.LENGTH_LONG).show()
            recorder.stop()
            //controllerDecisionModel.checkAmplitude(ampDB)
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
    override fun onResume() {
        super.onResume()
        wakeLock.acquire()
    }
    override fun onStop() {
        super.onStop()
        wakeLock.release()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

}