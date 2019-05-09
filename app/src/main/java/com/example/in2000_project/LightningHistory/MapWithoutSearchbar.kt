package com.example.in2000_project.LightningHistory

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast

import com.example.in2000_project.R
import com.example.in2000_project.maps.MapFragment
import com.example.in2000_project.utils.UalfUtil
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
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log
import kotlin.math.roundToInt

data class InfoWindowData(val time: Date, val lat: Double, val long: Double)


class MapWithoutSearchbar() : OnMapReadyCallback, PlaceSelectionListener, Fragment() {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var mapsAPI: String
    private lateinit var placesClient: PlacesClient
    private lateinit var viewModel : HistoryViewmodel // use this to get data
    private val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 100

    private lateinit var rootView: View
    private var sharedPrefs: SharedPreferences? = null

    private var historyMarkers = ArrayList<Marker>()
    private lateinit var changeObserver: Observer<ArrayList<UalfUtil.Ualf>>

    var currentFocus: MapFragment.SavedMarkers? = null

    //Factory method for creating new map fragment
    companion object {
        fun newInstance(): MapWithoutSearchbar {
            return MapWithoutSearchbar()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("Fragment map", "Inflating map fragment")
        rootView = inflater.inflate(R.layout.fragment_map_without_searchbar, parent, false)
        sharedPrefs = this.activity?.getSharedPreferences("Map Fragment", Context.MODE_PRIVATE)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Fragment map", "Getting viewmodel for map")
        Log.d("Fragment map", "Successfully got viewmodel")

        changeObserver = Observer { ualfList ->
            Log.e("CHANGE", "LIGHTNINGLIST CHANGES")
            this.historyMarkers.forEach { it.remove() }

            val newFocus = (activity as LightningHistoryActivity).selectedMarker
            Log.e("New Focus:", newFocus.toString())
            val zoomLevel = calcZoomLevel(newFocus.latitude, newFocus.radius)
            Log.e("Zoom Level", zoomLevel.toString())
            this.googleMap.animateCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(LatLng(newFocus.latitude, newFocus.longitude), zoomLevel))

//            ualfList?.forEach {
//                val newLocation = LatLng(it.lat, it.long)
//                val newInfo = InfoWindowData(it.date, it.lat, it.long)
//                setMarkerLightning(newLocation, newInfo)
//            }
            for (i in ualfList!!.indices) {
                val ualf = ualfList[i]
                val newLocation = LatLng(ualf.lat, ualf.long)
                val newInfo = InfoWindowData(ualf.date, ualf.lat, ualf.long)
                setMarkerLightning(newLocation, newInfo, i.toFloat())
            }
        }

        mapsAPI = getString(R.string.Maps_API)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        //Get notified when map is ready to be used
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(activity!!, mapsAPI)
        placesClient = Places.createClient(activity!!)

    }

    private fun calcZoomLevel(lat: Double, radius: Double): Float{
        var displayMetrics = DisplayMetrics()
        (activity as LightningHistoryActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        var maxLength = Math.min(height, width)
        return log(156543.03392 * Math.cos(lat * Math.PI / 180) * maxLength/ radius * 2, 2.0).toFloat()
//        Math.pow(2, zoom) = 156543.03392 * Math.cos(lat * Math.PI / 180) * maxLength / km
    }

    private fun setMarkerLightning(location: LatLng, info: InfoWindowData, zIndex: Float) {

        val marker: Marker = googleMap.addMarker(MarkerOptions()
                                                    .position(location)
                                                    .icon(BitmapDescriptorFactory.fromBitmap(
                                                                                    resizeMapIcon("lightning_symbol", 40, 130))
                                                    )
                                                    .zIndex(zIndex)
                                                )
        this.historyMarkers.add(marker)
        marker.tag = info

    }

    private fun resizeMapIcon(iconName: String, width: Int, height: Int): Bitmap {
        val imageBitmap: Bitmap = BitmapFactory
            .decodeResource(resources, resources.getIdentifier(iconName, "drawable", activity!!.packageName))
        val resizedBitmap: Bitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return resizedBitmap
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.uiSettings.isZoomControlsEnabled = true
        //Make map style follow dark mode toggle
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val darkMode = defaultSharedPreferences.getBoolean("darkMode", false)
        setMapStyle(!darkMode)

        //Check for location permissions and request permissions if not already granted
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            setUpMap()
        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                , MY_PERMISSIONS_REQUEST_ACCESS_LOCATION)
        }

        HistoryViewmodel.recentData.observe(this, changeObserver)
//        googleMap.setOnInfoWindowClickListener {  }
        val customInfoWindow = CustomInfoWindow(activity as Context)
        googleMap.setInfoWindowAdapter(customInfoWindow)

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        googleMap.isMyLocationEnabled = true
                        Log.d("Fragment map", "Location permission granted")
                        setUpMap()
                    }
                } else {
                    // Permission denied.
                    Log.d("Fragment map", "Location permission denied")
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
//                    Log.d("Fragment map", "Current position: $currentLatLng")
                }
            }
        }

    }

    override fun onError(status: Status) {
        Toast.makeText(activity!!, ""+status.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onPlaceSelected(place: Place) {

    }

    fun setMapStyle(lightMode: Boolean) {
        if (lightMode) {
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(activity!!,
                    R.raw.standard_json
                ))
        } else {
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(activity!!,
                    R.raw.darkmode_json
                ))
        }
    }
}
