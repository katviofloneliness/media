package com.example.media

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places

class MapsFragment : Fragment() {
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), "AIzaSyDfYhFGAUAT97N405VnXl27My2zd6Oo1eY")
        locationManager = LocationManager(requireContext())
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var view: View = inflater.inflate(R.layout.fragment_maps, container, false)
        // Map fragment init
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        locationManager = LocationManager(requireContext())
        locationManager.startLocationUpdates()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        supportMapFragment?.getMapAsync { map ->
            googleMap = map
            googleMap.isMyLocationEnabled = true

            // Move the map camera to the user's current location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(location.latitude, location.longitude)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                        googleMap.addMarker(
                            MarkerOptions().position(latLng).title("Current Location")
                        )
                    }
                }
        }

        // Map
        supportMapFragment!!.getMapAsync { googleMap ->
            googleMap.setOnMapClickListener { latLng ->
                // When map is clicked add pin
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title(latLng.latitude.toString() + " " + latLng.longitude)
                googleMap.clear()
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                googleMap.addMarker(markerOptions)
                googleMap.isMyLocationEnabled = true
            }
        }

        /*        fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        Toast.makeText(context, location.toString(), Toast.LENGTH_LONG).show()
                    }*/


        /*        val locationRequest = LocationRequest.create()?.apply {
                    interval = 10000
                    fastestInterval = 5000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                Toast.makeText(context, locationRequest.toString(), Toast.LENGTH_LONG).show()*/


        return view
    }


    fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapClickListener {
            val markerOptions = MarkerOptions().position(it).title("current location")
            googleMap.clear()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 3f))
            googleMap.addMarker(markerOptions)

        }
    }

    /*    override fun onResume() {
            super.onResume()
            locationManager.startLocationUpdates()
        }
    */
    override fun onPause() {
        super.onPause()
        locationManager.stopLocationUpdates()
    }


}