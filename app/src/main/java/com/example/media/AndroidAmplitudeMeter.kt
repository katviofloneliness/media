import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

class AndroidAmplitudeMeter(private val callback: AmplitudeCallback) {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var isRecording = false
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    )

    private var audioRecord: AudioRecord? = null

    @SuppressLint("MissingPermission")
    fun start() {
        if (!isRecording) {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize
            )

            audioRecord?.startRecording()

            isRecording = true
            Thread { measureAmplitude() }.start()
        }
    }

    fun stop() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun measureAmplitude() {
        val buffer = ShortArray(bufferSize)
        while (isRecording) {
            val read = audioRecord?.read(buffer, 0, bufferSize)
            if (read != null && read > 0) {
                val amplitude = calculateAmplitude(buffer, read)
                callback.onAmplitudeMeasured(amplitude)
                Thread.sleep(300) // Adjust this delay if needed
            }
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
