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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class LocationManager(private val context: Context) {
    private val controllerDND by lazy {
        AndroidDND(context)
    }

    private val lowVolumeList = listOf(
        Place.Type.DOCTOR,
        Place.Type.DENTIST,
        Place.Type.ACCOUNTING,
        Place.Type.AQUARIUM,
        Place.Type.ART_GALLERY,
        Place.Type.CAMPGROUND,
        Place.Type.CEMETERY,
        Place.Type.CHURCH,
        Place.Type.CITY_HALL,
        Place.Type.COURTHOUSE,
        Place.Type.FUNERAL_HOME,
        Place.Type.UNIVERSITY,
        Place.Type.HINDU_TEMPLE,
        Place.Type.HOSPITAL,
        Place.Type.MOSQUE,
        Place.Type.LIBRARY,
        Place.Type.MOVIE_THEATER,
        Place.Type.MUSEUM,
        Place.Type.PLACE_OF_WORSHIP,
        Place.Type.PRIMARY_SCHOOL,
        Place.Type.SCHOOL,
        Place.Type.SECONDARY_SCHOOL,
        Place.Type.SYNAGOGUE,
    )
    private val mediumVolumeList = listOf(
        Place.Type.AIRPORT,
        Place.Type.DEPARTMENT_STORE,
        Place.Type.DRUGSTORE,
        Place.Type.ELECTRICIAN,
        Place.Type.ELECTRONICS_STORE,
        Place.Type.EMBASSY,
        Place.Type.FIRE_STATION,
        Place.Type.FLORIST,
        Place.Type.FURNITURE_STORE,
        Place.Type.GAS_STATION,
        Place.Type.HAIR_CARE,
        Place.Type.HARDWARE_STORE,
        Place.Type.HOME_GOODS_STORE,
        Place.Type.INSURANCE_AGENCY,
        Place.Type.JEWELRY_STORE,
        Place.Type.LAUNDRY,
        Place.Type.LAWYER,
        Place.Type.LIGHT_RAIL_STATION,
        Place.Type.LIQUOR_STORE,
        Place.Type.LOCAL_GOVERNMENT_OFFICE,
        Place.Type.LOCKSMITH,
        Place.Type.LODGING,
        Place.Type.MEAL_DELIVERY,
        Place.Type.MEAL_TAKEAWAY,
        Place.Type.MOVIE_RENTAL,
        Place.Type.MOVING_COMPANY,
        Place.Type.ATM,
        Place.Type.BAKERY,
        Place.Type.BANK,
        Place.Type.BICYCLE_STORE,
        Place.Type.BOOK_STORE,
        Place.Type.BUS_STATION,
        Place.Type.CAFE,
        Place.Type.CAR_DEALER,
        Place.Type.CAR_RENTAL,
        Place.Type.CAR_REPAIR,
        Place.Type.CAR_WASH,
        Place.Type.CLOTHING_STORE,
        Place.Type.CONVENIENCE_STORE,
        Place.Type.PAINTER,
        Place.Type.PHARMACY,
        Place.Type.SHOE_STORE,
        Place.Type.PHYSIOTHERAPIST,
        Place.Type.PLUMBER,
        Place.Type.POLICE,
        Place.Type.POST_OFFICE,
        Place.Type.REAL_ESTATE_AGENCY,
        Place.Type.SPA,
        Place.Type.TRAVEL_AGENCY,
        Place.Type.VETERINARY_CARE,
        Place.Type.ROOFING_CONTRACTOR,
        Place.Type.PARK,
    )
    private val highVolumeList = listOf(
        Place.Type.AMUSEMENT_PARK,
        Place.Type.GYM,
        Place.Type.PARKING,
        Place.Type.PET_STORE,
        Place.Type.RESTAURANT,
        Place.Type.RV_PARK,
        Place.Type.SHOPPING_MALL,
        Place.Type.STADIUM,
        Place.Type.STORAGE,
        Place.Type.STORE,
        Place.Type.SUBWAY_STATION,
        Place.Type.SUPERMARKET,
        Place.Type.TAXI_STAND,
        Place.Type.TOURIST_ATTRACTION,
        Place.Type.TRAIN_STATION,
        Place.Type.TRANSIT_STATION,
        Place.Type.ZOO,
        Place.Type.NIGHT_CLUB,
        Place.Type.CASINO,
        Place.Type.ROUTE,
        Place.Type.TOWN_SQUARE
    )


    private var locationInterval = 30000L
    private val sharedPreferences =
        context.getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)

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
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
            return
        }
        placesClient.findCurrentPlace(request)
            .addOnSuccessListener { response ->
                for (placeLikelihood in response.placeLikelihoods) {
                    val place = placeLikelihood.place
                    savePlaceTypeToFirebase(place)
                    checkPlaceTypeAndAdjustVolume(place)
                    Toast.makeText(
                        context.applicationContext,
                        place.types.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }
                controllerDND.disableDndMode()
            }
            .addOnFailureListener { exception ->
                controllerDND.disableDndMode()
            }
    }

    private fun setVolumeLevel(audioManager: AudioManager, volumeLevel: Float) {
        audioManager.ringerMode = if (volumeLevel == 0.0f) {
            AudioManager.RINGER_MODE_SILENT
        } else {
            AudioManager.RINGER_MODE_NORMAL
        }
        // Adjust ringtone volume
        val maxRingVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        val newRingVolume = (maxRingVolume * volumeLevel).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_RING, newRingVolume, 0)
        // Adjust Notification volume
        val maxNotificationVolume =
            audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        val newNotificationVolume = (maxNotificationVolume * volumeLevel).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, newNotificationVolume, 0)
        // Adjust Media volume
        val maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newMediaVolume = (maxMediaVolume * volumeLevel).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newMediaVolume, 0)
    }

    private fun checkPlaceTypeAndAdjustVolume(place: Place) {
        val isInMediumVolumeBuilding = place.types.any { type ->
            type in mediumVolumeList
        }
        val isInLowVolumeBuilding = place.types.any { type ->
            type in lowVolumeList
        }
        val isInHighVolumeBuilding = place.types.any { type ->
            type in highVolumeList
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when {
            // Set volume to 100% for high volume places
            isInHighVolumeBuilding -> {
                controllerDND.disableDndMode()
                setVolumeLevel(audioManager, 1.0f)
            }
            // Set volume to 50% for medium volume places
            isInMediumVolumeBuilding -> {
                controllerDND.disableDndMode()
                setVolumeLevel(audioManager, 0.5f)
            }
            // Set volume to silent for low volume places
            isInLowVolumeBuilding -> {
                controllerDND.enableDndMode()
                setVolumeLevel(audioManager, 0.0f)
            }
            // Reset volume to 50% for unspecified places
            else -> {
                controllerDND.disableDndMode()
                setVolumeLevel(audioManager, 0.5f)
            }
        }
    }

    fun setLocationInterval(interval: Long) {
        locationInterval = interval
        sharedPreferences.edit().putLong("interval", interval).apply()
        //stopLocationUpdates()
        //startLocationUpdates()
    }

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = locationInterval
            fastestInterval = locationInterval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun savePlaceTypeToFirebase(place: Place) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("places")

        //val placeType = place.types.firstOrNull()
        val placeType = place.types


        if (placeType != null) {
            val placeValues = HashMap<String, Any>()
            //placeValues["placeType"] = placeType.name
            placeValues["placeType"] = placeType.toString()

            val currentTime = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val formattedTime = dateFormat.format(currentTime)

            placeValues["requestTime"] = formattedTime // Save the formatted time

            val requestKey = databaseReference.push().key
            if (requestKey != null) {
                val requestReference = databaseReference.child(requestKey)
                requestReference.setValue(placeValues)
                val placeReference = requestReference.child("place")
                placeReference.setValue(place.name)
            }
        }
    }
}