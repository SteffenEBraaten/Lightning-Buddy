package com.example.in2000_project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

class MainActivity : BaseActivity(), OnMapReadyCallback, PlaceSelectionListener {
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var mapsAPI: String
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapsAPI = getString(R.string.Maps_API)

        super.setDrawer()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //Get notified when map is ready to be used
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(applicationContext, mapsAPI)
        placesClient = Places.createClient(this)
    }

    val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 100
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        //Check for location permissions and request permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            setUpMap()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                , MY_PERMISSIONS_REQUEST_ACCESS_LOCATION)
        }

    }
    private fun setUpMap() {
        // The reason for checking this all the time is because the user could at any time revoke permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
        val autocompleteFragment: AutocompleteSupportFragment =
            supportFragmentManager.findFragmentById(R.id.searchbar_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(arrayListOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
            Place.Field.VIEWPORT))
        autocompleteFragment.setOnPlaceSelectedListener(this)
    }

    override fun onPlaceSelected(place: Place) {
        googleMap.clear()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.viewport, 0))
        //Only run bellow part if latLng is not null
        place.latLng?.run {
            googleMap.addMarker(MarkerOptions().position(place.latLng!!))
        }
    }

    override fun onError(status: Status) {
        Toast.makeText(applicationContext, ""+status.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        googleMap.isMyLocationEnabled = true
                        setUpMap()
                    }
                } else {
                  // Permission denied.
                }
            }
            else -> {
            //Ignore all other requests
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    //TODO: Decide if setting light and dark mode should rather be done in one function or two functions like I did bellow
    fun setMapDarkMode() {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.darkmode_json))
    }
    fun setLightMode() {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.standard_json))
    }
    fun setMarkerLightning(location: LatLng) {
        googleMap.addMarker(MarkerOptions().position(location)
            .icon(BitmapDescriptorFactory
            .fromBitmap(resizeMapIcon("lightning_icon_tmp", 150, 150))))
    }
    fun resizeMapIcon(iconName: String, width: Int, height: Int): Bitmap {
        val imageBitmap: Bitmap = BitmapFactory
            .decodeResource(resources, resources.getIdentifier(iconName, "drawable", packageName))
        val resizedBitmap: Bitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return resizedBitmap
    }
}