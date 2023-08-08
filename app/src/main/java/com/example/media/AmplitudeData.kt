package com.example.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AmplitudeData(val amplitude: Float, val time: String) : Parcelable
