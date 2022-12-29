package com.example.maps2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions


class MainActivity : AppCompatActivity() {

    private lateinit var mPermissionResultLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false
    private var isLocationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPermissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            isReadPermissionGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            isLocationPermissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        }

        requestPermission()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
              val origin = Location("current location") //  actual current location
            origin.latitude = 38.902166
            origin.longitude = -77.049687

            val destination = Location("White House") // actual destination
            destination.latitude = 38.8976763
            destination.longitude = -77.0387185

            val request = DirectionsApi.newRequest(GeoApiContext.Builder().build())
                .origin(origin)
                .destination(destination)
                .mode(TravelMode.DRIVING)
                .departureTime(DateTime.now())
                .trafficModel(TrafficModel.BEST_GUESS)

            // In this code we provided, request is an instance of the DirectionsApiRequest class and setCallback is a method that takes a PendingResult.Callback as an argument. The PendingResult.Callback is an interface with two methods: onResult and onFailure

            val callback: Any = request.setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult) {
                    val route = result.routes[0]
                    val points = route.overviewPolyline.decodePath()
                    val polylineOptions = PolylineOptions().width(10f).color(Color.RED)
                    points.forEach { point ->
                        polylineOptions.add(LatLng(point.lat, point.lng))
                    }
                    googleMap.addPolyline(polylineOptions)
                }

                override fun onFailure(e: Exception) {
                    if (e is ApiException) {
                        // Handle error caused by the Google Maps API
                    } else {
                        // Handle other errors
                    }
                })

            }
                fun showDestinationReached() {
                    Toast.makeText(this, "You have reached your destination", Toast.LENGTH_SHORT)
                        .show()
                }


        }













    private fun requestPermission() {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest = mutableListOf<String>()
        if (!isLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!isLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissionRequest.isNotEmpty()) {
            mPermissionResultLauncher.launch(permissionRequest.toTypedArray())
        }
    }
}

