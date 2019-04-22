package com.example.in2000_project

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class MapFragment: OnMapReadyCallback, PlaceSelectionListener, Fragment() {
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var mapsAPI: String
    private lateinit var placesClient: PlacesClient

    //Factory method for creating new map fragment
    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.map_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapsAPI = getString(R.string.Maps_API)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        //Get notified when map is ready to be used
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(activity!!, mapsAPI)
        placesClient = Places.createClient(activity!!)

    }

    val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 100
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        //Check for location permissions and request permissions if not already granted
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            setUpMap()
        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                , MY_PERMISSIONS_REQUEST_ACCESS_LOCATION)
        }
        googleMap.setOnMapClickListener (object: GoogleMap.OnMapClickListener {
            override fun onMapClick(position: LatLng?) {
                addMarkerWithRadius(position!!)
            }
        })
    }
    private fun addMarkerWithRadius(position: LatLng) {
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(position).draggable(true))
        //radius is in meters. Currently set to 10km
        var radius: Double = 10000.0
        var circle: Circle = googleMap.addCircle(CircleOptions().center(position).radius(radius).strokeColor(Color.BLUE)
            .fillColor(Color.argb(150, 146, 184, 244)))
        //The zoom level is kind of tricky if you change the radius
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11.1.toFloat()))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
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
    private fun setUpMap() {
        // The reason for checking this all the time is because the user could at any time revoke permissions
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener(activity!!) { location ->
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }

        val autocompleteFragment: AutocompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.searchbar_fragment) as AutocompleteSupportFragment

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
        Toast.makeText(activity!!, ""+status.toString(), Toast.LENGTH_LONG).show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
    fun setMapStyle(lightMode: Boolean) {
        if (lightMode) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity!!, R.raw.standard_json))
        } else {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity!!, R.raw.darkmode_json))
        }
    }
    fun setMarkerLightning(location: LatLng) {
        val marker: Marker = googleMap.addMarker(MarkerOptions().position(location)
            .icon(BitmapDescriptorFactory
                .fromBitmap(resizeMapIcon("lightning_icon_tmp", 150, 150))))
        Handler().postDelayed({
            marker.remove()
        }, 5000)
    }
    private fun resizeMapIcon(iconName: String, width: Int, height: Int): Bitmap {
        val imageBitmap: Bitmap = BitmapFactory
            .decodeResource(resources, resources.getIdentifier(iconName, "drawable", activity!!.packageName))
        val resizedBitmap: Bitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return resizedBitmap
    }

}