package com.example.media

import java.io.File

interface AudioPlayer {
    fun playFile(file: File)
    fun stop()
}