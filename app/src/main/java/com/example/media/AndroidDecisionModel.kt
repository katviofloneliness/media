package com.example.media

import android.content.Context
import android.media.AudioManager
import android.widget.Toast

class AndroidDecisionModel
    (private val context: Context){

    private val thresholdDB: List<Double> = arrayListOf(50.0, 70.0, 90.0)
    private val controllerDND by lazy {
        AndroidDND(context)
    }

    fun checkAmplitude(amplitudeDB: Double): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        return when {
            amplitudeDB <= thresholdDB[0] -> {
                controllerDND.enableDndMode()
                "Silent"
            }
            amplitudeDB <= thresholdDB[2] -> {
                controllerDND.disableDndMode()
                setVolumeLevel(audioManager, 0.5f)
                "Medium"
            }
            else -> {
                controllerDND.disableDndMode()
                setVolumeLevel(audioManager, 1.0f)
                "Loud"
            }
        }
    }

    fun checkAmplitudeSimple(amplitudeDB: Double): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        return when {
            amplitudeDB <= thresholdDB[1] -> {
                controllerDND.disableDndMode()
                setVolumeLevel(audioManager, 0.3f)
                "Low volume"
            }
            else -> {
                controllerDND.disableDndMode()
                setVolumeLevel(audioManager, 0.7f)
                "High volume"
            }
        }
    }

    private fun setVolumeLevel(audioManager: AudioManager, volumeLevel: Float) {
/*        audioManager.ringerMode = if (volumeLevel == 0.0f) {
            AudioManager.RINGER_MODE_SILENT
        } else {
            AudioManager.RINGER_MODE_NORMAL
        }

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        val newVolume = (maxVolume * volumeLevel).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_RING, newVolume, 0)*/

        // Adjust Ringtone volume
        val maxRingVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        val newRingVolume = (maxRingVolume * volumeLevel).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_RING, newRingVolume, 0)
        // Adjust Notification volume
        val maxNotificationVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        val newNotificationVolume = (maxNotificationVolume * volumeLevel).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, newNotificationVolume, 0)
        // Adjust Media volume
        val maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newMediaVolume = (maxMediaVolume * volumeLevel).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newMediaVolume, 0)
    }
}

/*Noise intensity levels [dB]	Example source of sound

0	Hearing threshold for frequency1000 Hz
10	Leaves rustling
20	Very quiet, whisper at a distance of 1 m
30	Quiet house/room
40	Regular house/room, radio
50	Office, quiet music
60	Moderate, regular conversation
70	Busy street traffic, louder office
80	Loud radio, door slamming
90	Loud, inside truck car, car horn, hearing damage caused by prolonged exposure to noise
100	Noisy industrial plant, siren 30m away, hearing damage from 8 hours of noise exposure
110	Very loud, hearing damage caused by 30-minute exposure to noise
120	Loud rock concert, jackhammer 2m away, pain threshold
140	Jet plane 30m away, severe pain, hearing loss*/
