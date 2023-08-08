package com.example.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10

class AndroidAudioRecorder(
    private val context: Context
) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var samplingRate = 1
    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)
            //setAudioSamplingRate(samplingRate)
            prepare()
            start()
            recorder = this
        }
        recorder?.maxAmplitude
    }

    override fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }

    override fun getAmp(): Double {
        val database = FirebaseDatabase.getInstance()
        val amplitudeRef = database.getReference("amplitudes")
        val amplitudeKey = amplitudeRef.push().key
        //recorder?.maxAmplitude

        val currentTime = System.currentTimeMillis()
        val amplitudeTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(currentTime))

        var dB = 20 * log10(recorder?.maxAmplitude!!.toDouble() / samplingRate)
        val amplitudeData = hashMapOf("amplitudeDB" to dB, "time" to amplitudeTime)
        amplitudeKey?.let { key ->
            amplitudeRef.child(key).setValue(amplitudeData)
        }
        //return recorder?.maxAmplitude.toString()

        return dB
    }

}