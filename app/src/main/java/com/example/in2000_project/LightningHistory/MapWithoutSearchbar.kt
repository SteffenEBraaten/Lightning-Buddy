package com.example.in2000_project.LightningHistory

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.example.in2000_project.R
import com.example.in2000_project.maps.MapsViewmodel
import com.example.in2000_project.maps.MapsViewmodelFactory
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MapWithoutSearchbar : OnMapReadyCallback, PlaceSelectionListener, Fragment() {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var mapsAPI: String
    private lateinit var placesClient: PlacesClient
    private lateinit var viewModel : HistoryViewmodel // use this to get data

    private lateinit var rootView: View
//    private var markersList: LinkedList<MarkerWithCircle> = LinkedList()
//    private var savedMarkersList: MutableSet<SavedMarkers>? = null
    private var sharedPrefs: SharedPreferences? = null

//    data class SavedMarkers(var latitude: Double, var longitude: Double, var radius: Double)
//    data class MarkerWithCircle(var marker: Marker?, var circle: Circle?)

    private lateinit var changeObserver: Observer<ArrayList<UalfUtil.Ualf>>
//    private lateinit var coRoutine: Job
    //Milliseconds
//    private var refreshRate: Long = 3 * 60 * 1000


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
//        this.viewModel = ViewModelProviders.of(this.activity!!,
//            MapsViewmodelFactory(PreferenceManager.getDefaultSharedPreferences(this.activity!!.baseContext))
//        ).get(HistoryViewmodel::class.java)
        Log.d("Fragment map", "Successfully got viewmodel")

//        changeObserver = Observer<ArrayList<UalfUtil.Ualf>> { newLightning ->
//            newLightning?.forEach {
//                val newLocation = LatLng(it.lat, it.long)
//                setMarkerLightning(newLocation)
//            }
//        }

        mapsAPI = getString(R.string.Maps_API)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        //Get notified when map is ready to be used
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(activity!!, mapsAPI)
        placesClient = Places.createClient(activity!!)

    }


    private val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 100

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

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
//        Log.d("Fragment map", "Clearing map of markers")
//        googleMap.clear()
//        Log.d("Fragment map", "Moving to $place")
//        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.viewport, 0))
//        //Only run bellow part if latLng is not null
//        place.latLng?.run {
//            googleMap.addMarker(MarkerOptions().position(place.latLng!!))
//        }
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

//
//    override fun onPause() {
//        super.onPause()
//        Log.d("Fragment Map", "Pause")
//        persistentSave()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.d("Fragment Map", "Stop")
//        persistentSave()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        Log.d("Fragment Map", "View destroy")
//        persistentSave()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d("Fragment Map", "Destroy")
//        persistentSave()
//        coRoutine.cancel()
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        Log.d("Fragment Map", "Detach")
//        persistentSave()
//    }
}
