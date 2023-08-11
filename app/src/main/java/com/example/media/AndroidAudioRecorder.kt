package com.example.media

import android.content.Context
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.log10

class AndroidAudioRecorder(
    private val context: Context
) : AudioRecorder {
    private val controllerDecisionModel by lazy {
        AndroidDecisionModel(context)
    }

    private var recorder: MediaRecorder? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var samplingRate = 22050
    private val offset = -20.0
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
            setAudioSamplingRate(samplingRate)
            prepare()
            start()
            recorder = this
        }
        recorder?.maxAmplitude
    }

    override fun stop() {
        noiseSuppressor?.enabled = false
        noiseSuppressor?.release()
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }

    override fun getAmp(): Double {
        val database = FirebaseDatabase.getInstance()
        val amplitudeRef = database.getReference("amplitudes")
        val amplitudeKey = amplitudeRef.push().key

        val currentTime = System.currentTimeMillis()
        val amplitudeTime =
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))
        val dB = abs(20 * log10(recorder?.maxAmplitude!!.toDouble())) + offset
        val dBString = String.format("%.2f", dB)
        val outcome = controllerDecisionModel.checkAmplitude(dB)
        val amplitudeData =
            hashMapOf("amplitudeDB" to dBString, "time" to amplitudeTime, "outcome" to outcome)
        amplitudeKey?.let { key ->
            amplitudeRef.child(key).setValue(amplitudeData)
        }
        Toast.makeText(context, dBString, Toast.LENGTH_SHORT).show()

        return dB
    }

}