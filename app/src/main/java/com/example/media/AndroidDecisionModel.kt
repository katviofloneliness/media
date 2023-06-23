package com.example.media

import android.content.Context
import android.widget.Toast

class AndroidDecisionModel
    (private val context: Context) : DecisionModel {

    private val thresholdDB: List<Double> = arrayListOf(40.0, 50.0, 80.0)
    private val controllerDND by lazy {
        AndroidDND(context)
    }

    override fun checkAmplitude(amplitudeDB: Double) {
        try {
            if (amplitudeDB <= thresholdDB[0]) controllerDND.enableDndMode()
            else if (amplitudeDB > thresholdDB[0] && amplitudeDB <= thresholdDB[1])
                Toast.makeText(
                    context.applicationContext,
                    "30-50 dB range",
                    Toast.LENGTH_SHORT
                ).show()
            else if (amplitudeDB > thresholdDB[1] && amplitudeDB <= thresholdDB[2])
                Toast.makeText(
                    context.applicationContext,
                    "50-80 dB range",
                    Toast.LENGTH_SHORT
                ).show()
            else if (amplitudeDB > thresholdDB[2]) controllerDND.disableDndMode()

            /*            when(amplitudeDB){
                           in thresholdDB[0]..thresholdDB[1] -> controllerDND.disableDndMode()
                            thresholdDB[1] ->
                        }*/

        } catch (e: Exception) {
        }


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
