package com.route.googlemaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : AppCompatActivity() {
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) getUserLocation()
            // else checkPermission()
        }


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var map: GoogleMap? = null
    private var marker: Marker? = null

    private val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    private val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION

    private val permissions = arrayOf(fineLocation, coarseLocation)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()
        checkPermission()
    }

    private fun checkPermission() {

        when {
            isPermissionGranted() -> getUserLocation()

            shouldShowRationale() ->
                showDialog(this,
                    "permission denied, go to settings to allow precise location permission",
                    "go to settings",
                    "no,thanks", { showAppSettings() })

            else -> showDialog(this,
                "this app can show where you are on the map if you give it permission",
                "accept",
                "no,thanks",
                { requestPermission.launch(permissions) })

        }
    }

    private fun shouldShowRationale() = ActivityCompat
        .shouldShowRequestPermissionRationale(this, fineLocation)

    private fun isPermissionGranted() =
        (ContextCompat.checkSelfPermission(this, fineLocation)
                == PackageManager.PERMISSION_GRANTED)

    private fun showAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val request = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000)
            .build()

        fusedLocationClient
            .requestLocationUpdates(request, {
                updateMap(it.latitude, it.longitude)

            }, Looper.getMainLooper())

    }

    private fun updateMap(latitude: Double, longitude: Double) {

        if (marker == null) {

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 16F)

            val options = MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title("your location")
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_tracker))

            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment

            mapFragment?.getMapAsync {
                marker = it.addMarker(options)
                it.animateCamera(cameraUpdate)
                map = it
            }
        }
        map?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
        marker?.position = LatLng(latitude, longitude)

    }

    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorResId: Int
    ): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}