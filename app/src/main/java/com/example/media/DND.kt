package com.example.media

interface DND {
    fun enableDndMode()
    fun disableDndMode()
    fun checkPermissionDndMode(activity: MainActivity)
}