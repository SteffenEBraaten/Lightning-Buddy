package com.example.in2000_project.maps

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.support.v7.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.in2000_project.R
import com.example.in2000_project.utils.UalfUtil
import com.example.in2000_project.utils.WeatherDataUtil
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception



class MapFragment: OnMapReadyCallback, PlaceSelectionListener, Fragment() {
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var mapsAPI: String
    private lateinit var placesClient: PlacesClient
    private lateinit var viewModel : MapsViewmodel // use this to get data

    private lateinit var rootView: View
    private var markersList: LinkedList<MarkerWithCircle> = LinkedList()
    private var savedMarkersList: MutableSet<SavedMarkers>? = null
    private var sharedPrefs: SharedPreferences? = null
    private var prevSearchMarker: Marker? = null

    data class SavedMarkers(var latitude: Double, var longitude: Double, var radius: Double)
    data class MarkerWithCircle(var marker: Marker?, var circle: Circle?)

    private lateinit var changeObserver: Observer<ArrayList<UalfUtil.Ualf>>
    private lateinit var coRoutine: Job
    //Milliseconds
    private var refreshRate: Long = 5 * 60 * 1000


    //Factory method for creating new map fragment
    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("Fragment map", "Inflating map fragment")
        rootView = inflater.inflate(R.layout.map_fragment, parent, false)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Fragment map", "Getting viewmodel for map")
        this.viewModel = ViewModelProviders.of(this.activity!!,
            MapsViewmodelFactory(PreferenceManager.getDefaultSharedPreferences(this.activity!!.baseContext))
        ).get(MapsViewmodel::class.java)
        Log.d("Fragment map", "Successfully got viewmodel")

        changeObserver = Observer<ArrayList<UalfUtil.Ualf>> { newLightning ->
            newLightning?.forEach {
                val newLocation = LatLng(it.lat, it.long)
                setMarkerLightning(newLocation, refreshRate)
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

        retrieveSavedMarkers()
    }
    private fun retrieveSavedMarkers() {
        Log.d("Fragment map", "Retrieving saved markers from shared preferences")
        val jsonLinkedList = sharedPrefs!!.getString("SavedMarkers", null)
        if (jsonLinkedList != null) {
            savedMarkersList = Gson().fromJson(jsonLinkedList, object: TypeToken<MutableSet<SavedMarkers>>(){}.type)
            Log.d("Fragment map", "Saved markers retrieved")
            Log.d("Fragment map", savedMarkersList.toString())
        }
        if (savedMarkersList == null) {
            Log.d("Fragment map", "No saved markers")
            savedMarkersList = mutableSetOf()
        }
    }

    private val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 100
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        Log.d("Fragment map", "Map ready")
        MapsViewmodel.recentData.observe(this, changeObserver)
        coRoutine = GlobalScope.launch{
            while (true) {
                //viewModel.getRecentApiData()
                viewModel.getRecentApiData()
                Log.d("Refresh API", "API refreshed")
                delay(refreshRate)
            }
        }

        //Make map style follow dark mode toggle
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val darkMode = defaultSharedPreferences.getBoolean("darkMode", false)
        if (darkMode) {
            Log.d("Fragment map", "Map = darkmode")
            setMapStyle(false)
        } else {
            Log.d("Fragment map", "Map = lightmode")
            setMapStyle(true)
        }

        //Check for location permissions and request permissions if not already granted
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            setUpMap()
        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                , MY_PERMISSIONS_REQUEST_ACCESS_LOCATION)
        }

        var prevMarker: MarkerWithCircle? = MarkerWithCircle(null, null)
        var saveButton: Button? = null
        googleMap.setOnMapClickListener (object: GoogleMap.OnMapClickListener {
            override fun onMapClick(position: LatLng?) {
                Log.d("Fragment map", "Map clicked at posistion $position")
                prevMarker = addMarkerWithRadius(position!!, googleMap, prevMarker)

                saveButton = addSaveButton(saveButton, prevMarker!!)


            }
        })
    }
    private fun addSaveButton(prevButton: Button?, marker: MarkerWithCircle): Button {
        Log.d("Fragment map", "Adding save button")
        var fragmentLayout: RelativeLayout = rootView.findViewById<RelativeLayout>(R.id.map_frame)

        fragmentLayout.removeView(prevButton)
        prevButton?.run {
            Log.d("Fragment map", "Removed button $prevButton")
        }

        var saveButton: Button = Button(activity)
        saveButton.text = resources.getString(R.string.save)

        var layoutParameters: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        saveButton.layoutParams = layoutParameters

        layoutParameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParameters.addRule(RelativeLayout.ALIGN_PARENT_END)
        fragmentLayout.addView(saveButton)
        Log.d("Fragment map", "Save button $saveButton added")

        saveButton.setOnClickListener {
            saveButton.alpha = 1.toFloat()
            fadeButton(saveButton)
            Log.d("Fragment map", "Save button clicked")
            markersList.add(marker)
            Log.d("Fragment map", "markersList size: " + markersList.size)
        }

        fadeButton(saveButton)
        return saveButton
    }
    private fun fadeButton(button: Button) {
        val timeToFade: Long = 3000
        val fadeDelay: Long = 4000
        button.animate().alpha(0.6.toFloat()).setDuration(timeToFade).startDelay = fadeDelay
    }
    private fun addMarkerWithRadius(position: LatLng, googleMap: GoogleMap, prevMark: MarkerWithCircle?): MarkerWithCircle? {
        prevMark?.marker?.remove()
        prevMark?.circle?.remove()

        prevMark?.marker = googleMap.addMarker(MarkerOptions().position(position).draggable(true))
        //radius is in meters. Currently set to 10km
        var radius: Double = 10000.0
        var circle: Circle = googleMap.addCircle(CircleOptions().center(position).radius(radius).strokeColor(Color.BLUE)
            .fillColor(Color.argb(150, 146, 184, 244)))
        //The zoom level is kind of tricky if you change the radius
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11.1.toFloat()))
        Log.d("Fragment map", "Marker added")

        googleMap.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker?) {
                circle.center = marker?.position
            }

            override fun onMarkerDragEnd(marker: Marker?) {
               circle.center = marker?.position
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 11.1.toFloat()))
                Log.d("Fragment map", "Marker moved")
            }

            override fun onMarkerDrag(marker: Marker?) {
                circle.center = marker?.position
            }
        })
        prevMark?.circle = circle
        Log.d("Fragment map", "Returning marker")
        return prevMark
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
                    Log.d("Fragment map", "Current position: $currentLatLng")
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
        prevSearchMarker?.remove()
        Log.d("Fragment map", "Moving to $place")
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.viewport, 0))
        //Only run bellow part if latLng is not null
        place.latLng?.run {
            prevSearchMarker = googleMap.addMarker(MarkerOptions().position(place.latLng!!))
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
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity!!,
                R.raw.standard_json
            ))
        } else {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity!!,
                R.raw.darkmode_json
            ))
        }
    }
    fun setMarkerLightning(location: LatLng, duration: Long) {
        Log.d("Fragment map", "Setting marker at $location")
        val marker: Marker = googleMap.addMarker(MarkerOptions().position(location)
            .icon(BitmapDescriptorFactory
                .fromBitmap(resizeMapIcon("lightning_symbol", 150, 150))))
            Handler().postDelayed({
                marker.remove()
            }, duration)
    }
    /*
    Function overloading if the marker should not be removed after a given time.
    Function returns the marker so caller can handle removal
     */
    fun setMarkerLightning(location: LatLng): Marker {
        Log.d("Fragment map", "Setting marker at $location")
        val marker: Marker = googleMap.addMarker(MarkerOptions().position(location)
            .icon(BitmapDescriptorFactory
                .fromBitmap(resizeMapIcon("lightning_symbol", 150, 150))))
        return marker
    }
    private fun resizeMapIcon(iconName: String, width: Int, height: Int): Bitmap {
        val imageBitmap: Bitmap = BitmapFactory
            .decodeResource(resources, resources.getIdentifier(iconName, "drawable", activity!!.packageName))
        val resizedBitmap: Bitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return resizedBitmap
    }
    private fun persistentSave() {
        for (entry: MarkerWithCircle in markersList) {
            var position: LatLng? = entry.marker?.position
            var radius: Double? = entry.circle?.radius
            savedMarkersList?.add(SavedMarkers(position!!.latitude, position.longitude, radius!!))
        }

        markersList?.run {
            val prefEditor = sharedPrefs?.edit()
            prefEditor?.putString("SavedMarkers", Gson().toJson(savedMarkersList))
            Log.d("Fragment map", "Markers saved")
            prefEditor?.apply()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("Fragment Map", "Pause")
        persistentSave()
    }

    override fun onStop() {
        super.onStop()
        Log.d("Fragment Map", "Stop")
        persistentSave()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Fragment Map", "View destroy")
        persistentSave()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Fragment Map", "Destroy")
        persistentSave()
        coRoutine.cancel()
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("Fragment Map", "Detach")
        persistentSave()
    }
}