import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler

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

    fun stop() {
        isRecording = false
        handler.removeCallbacks(measureRunnable)
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

    private fun calculateAmplitude(buffer: ShortArray, readSize: Int): Double {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i] * buffer[i].toDouble()
        }
        val amplitude = Math.sqrt(sum / readSize)
        return amplitude
    }

    interface AmplitudeCallback {
        fun onAmplitudeMeasured(amplitude: Double)
    }
}
