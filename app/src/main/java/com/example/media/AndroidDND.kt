package com.example.media

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings


class AndroidDND(
    private val context: Context
) : DND {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun checkPermissionDndMode(activity: MainActivity) {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            activity.startActivity(intent)
            return
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun enableDndMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        } else {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        }
        /*        Toast.makeText(
                    context.applicationContext,
                    "Do Not Disturb mode enabled",
                    Toast.LENGTH_SHORT
                ).show()*/
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun disableDndMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        } else {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }
        /*        Toast.makeText(
                    context.applicationContext,
                    "Do Not Disturb mode disabled",
                    Toast.LENGTH_SHORT
                ).show()*/
    }

}