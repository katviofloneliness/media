import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.widget.Toast

class AndroidAmplitudeMeter(private val context: Context, private val callback: AmplitudeCallback) {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private var MEASUREMENT_INTERVAL = 30000L
    }
    private val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)


    private var isRecording = false
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    )

    private var audioRecord: AudioRecord? = null
    private val handler = Handler()

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
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize
            )

            audioRecord?.startRecording()

            isRecording = true
            measureAmplitude()
            val interval = sharedPreferences.getLong("interval", MEASUREMENT_INTERVAL)
            handler.postDelayed(measureRunnable, interval)
        }
    }

    @SuppressLint("MissingPermission")
    fun startSimple() {
        if (!isRecording) {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize
            )
            audioRecord?.startRecording()

            isRecording = true
            measureAmplitudeSimple()
            val interval = sharedPreferences.getLong("interval", MEASUREMENT_INTERVAL)
            handler.postDelayed(measureRunnableSimple, interval)
        }
    }

    fun stop() {
        isRecording = false
        handler.removeCallbacks(measureRunnable)
        handler.removeCallbacks(measureRunnableSimple)
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun measureAmplitude() {
        val buffer = ShortArray(bufferSize)
        val read = audioRecord?.read(buffer, 0, bufferSize)
        if (read != null && read > 0) {
            val amplitude = calculateAmplitude(buffer, read)
            callback.onAmplitudeMeasured(amplitude)
        }
    }
    private fun measureAmplitudeSimple() {
        val buffer = ShortArray(bufferSize)
        val read = audioRecord?.read(buffer, 0, bufferSize)
        if (read != null && read > 0) {
            val amplitude = calculateAmplitude(buffer, read)
            callback.onAmplitudeMeasuredSimple(amplitude)
        }
    }

    private fun calculateAmplitude(buffer: ShortArray, readSize: Int): Double {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i] * buffer[i].toDouble()
        }
        val rms = Math.sqrt(sum / readSize)
        val reference = 32767.0// Maximum value for a 16-bit PCM signal
        var db = 0.0
        if (rms > 0) {
            db = Math.abs( 20 * Math.log10(rms / reference))
        }
        Toast.makeText(context, db.toString() +" dB", Toast.LENGTH_LONG).show()
        return db

    }

    interface AmplitudeCallback {
        fun onAmplitudeMeasured(amplitude: Double)
        fun onAmplitudeMeasuredSimple(amplitude: Double)
    }
}
