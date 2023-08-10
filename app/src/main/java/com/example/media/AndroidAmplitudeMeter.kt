import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AudioEffect
import android.os.Handler
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.log10

class AndroidAmplitudeMeter(private val context: Context, private val callback: AmplitudeCallback) {

    private val offset = -20.0
    private var MEASUREMENT_INTERVAL = 30000L
    private var mediaRecorder: MediaRecorder? = null
    private val handler = Handler()

    private val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private var isRecording = false


    private val measureRunnable: Runnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                measureAmplitude()
                val interval = sharedPreferences.getLong("interval", MEASUREMENT_INTERVAL)
                handler.postDelayed(this, interval)
            }
        }
    }
    private val measureRunnableSimple: Runnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                measureAmplitudeSimple()
                val interval = sharedPreferences.getLong("interval", MEASUREMENT_INTERVAL)
                handler.postDelayed(this, interval)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (!isRecording) {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            val tempFileName = "${context.getExternalFilesDir(null)?.absolutePath}/temp_audio.mp3"
            mediaRecorder?.setOutputFile(tempFileName)

            mediaRecorder?.prepare()
            mediaRecorder?.start()
            mediaRecorder?.maxAmplitude

            isRecording = true
            handler.post(measureRunnable)
        }
    }
    fun startSimple() {
        if (!isRecording) {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            val tempFileName = "${context.getExternalFilesDir(null)?.absolutePath}/temp_audio.mp3"
            mediaRecorder?.setOutputFile(tempFileName)

            mediaRecorder?.prepare()
            mediaRecorder?.start()
            mediaRecorder?.maxAmplitude

            isRecording = true
            handler.post(measureRunnableSimple)
        }
    }

    fun stop() {
        isRecording = false
        handler.removeCallbacks(measureRunnable)
        mediaRecorder?.stop()
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    private fun measureAmplitude() {
        val amplitude = mediaRecorder?.maxAmplitude ?: 0
        val db = calculateAmplitude(amplitude)
        callback.onAmplitudeMeasured(db)
    }
    private fun measureAmplitudeSimple() {
        val amplitude = mediaRecorder?.maxAmplitude ?: 0
        val db = calculateAmplitude(amplitude)
        callback.onAmplitudeMeasuredSimple(db)
    }

    private fun calculateAmplitude(maxAmplitude: Int): Double {
        var db = 0.0
        if (maxAmplitude > 0) {
            db = abs(20 * log10(maxAmplitude.toDouble())) + offset
        }
        val dBString = String.format("%.2f", db)
        Toast.makeText(context, dBString + " dB", Toast.LENGTH_SHORT).show()
        return db
    }

    interface AmplitudeCallback {
        fun onAmplitudeMeasured(amplitude: Double)
        fun onAmplitudeMeasuredSimple(amplitude: Double)
    }

}
