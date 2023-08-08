package com.example.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.time.Duration.Companion.seconds

class LocationManager(private val context: Context) {

    private val lowVolumeList = listOf(Place.Type.LIBRARY, Place.Type.SCHOOL, Place.Type.CHURCH, Place.Type.UNIVERSITY
    )
    private val mediumVolumeList = listOf(Place.Type.RESTAURANT, Place.Type.BAR, Place.Type.GROCERY_OR_SUPERMARKET
    )
    private val highVolumeList = listOf(Place.Type.STADIUM, Place.Type.ROUTE)

    private var locationInterval = 30000L
    private val sharedPreferences = context.getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)

    // Initialize PlacesClient
    private val placesClient: PlacesClient = Places.createClient(context)
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                checkIfInBuilding(location)
            }
        }
    }

    fun startLocationUpdates() {
        locationInterval = sharedPreferences.getLong("interval", 30000L)
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            getLocationRequest(),
            locationCallback,
            null
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkIfInBuilding(location: Location) {
        val placeFields = listOf(Place.Field.NAME, Place.Field.TYPES)
        val request = FindCurrentPlaceRequest.newInstance(placeFields)


        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        placesClient.findCurrentPlace(request)
            .addOnSuccessListener { response ->
                for (placeLikelihood in response.placeLikelihoods) {
                    val place = placeLikelihood.place
                    checkPlaceTypeAndAdjustVolume(place)
                    // Handle the failure case if unable to fetch place details
                    Toast.makeText(
                        context.applicationContext,
                        locationInterval.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }
                adjustVolume(false)
            }
            .addOnFailureListener { exception ->
                // Handle the failure case if unable to fetch place details
                adjustVolume(false)
            }
    }

    private fun adjustVolume(isInLibrary: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (isInLibrary) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } else {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            audioManager.setStreamVolume(
                AudioManager.STREAM_RING,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
                0
            )
        }
    }

    private fun setVolumeLevel(audioManager: AudioManager, volumeLevel: Float) {
        audioManager.ringerMode = if (volumeLevel == 0.0f) {
            AudioManager.RINGER_MODE_SILENT
        } else {
            AudioManager.RINGER_MODE_NORMAL
        }

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        val newVolume = (maxVolume * volumeLevel).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_RING, newVolume, 0)
    }

    private fun checkPlaceTypeAndAdjustVolume(place: Place) {
        val isInLowVolumeBuilding = place.types.any { type ->
            type in lowVolumeList
        }
        val isInMediumVolumeBuilding = place.types.any { type ->
            type in mediumVolumeList
        }
        val isInHighVolumeBuilding = place.types.any { type ->
            type in highVolumeList
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when {
            isInHighVolumeBuilding -> {
                setVolumeLevel(audioManager, 1.0f) // Set volume to 100% for high volume places
            }
            isInMediumVolumeBuilding -> {
                setVolumeLevel(audioManager, 0.5f) // Set volume to 50% for medium volume places
            }
            isInLowVolumeBuilding -> {
                setVolumeLevel(audioManager, 0.0f) // Set volume to silent for low volume places
            }
            else -> {
                setVolumeLevel(audioManager, 1.0f) // Reset volume to normal for other places
            }
        }
    }

    fun setLocationInterval(interval: Long){
        locationInterval = interval
        sharedPreferences.edit().putLong("interval", interval).apply()
        //stopLocationUpdates()
        //startLocationUpdates()
    }

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            //interval = 30000 //3000
            interval = locationInterval
            fastestInterval = locationInterval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}