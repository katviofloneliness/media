package com.example.media

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
    fun getAmp(): Double
}