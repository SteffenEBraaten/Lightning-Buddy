package com.example.in2000_project.maps

import android.Manifest
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
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
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.in2000_project.R
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
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log


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
    lateinit var activeCircle: Circle

    data class SavedMarkers(var name: String,
                            var latitude: Double,
                            var longitude: Double,
                            var radius: Double) {
        constructor(lat: Double, long: Double, r: Double) :
                this("defaultName", lat, long, r)

    }
    data class MarkerWithCircle(var marker: Marker?, var circle: Circle?, var name: String?)

    private lateinit var changeObserver: Observer<ArrayList<UalfUtil.Ualf>>
    private var coRoutine: Job? = null
    //Milliseconds
    private var refreshRate: Long = 5 * 60 * 1000
    
    //Callback for setting radius fragment
    internal lateinit var callback: OnSetRadiusListener
    
    fun setOnRadiusListener(callback: OnSetRadiusListener) {
        this.callback = callback
    }
    interface OnSetRadiusListener {
        fun onSetRadiusCall(circle: Circle, marker: Marker)
    }


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
        this.googleMap.uiSettings.isZoomControlsEnabled = true
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

        var prevMarker: MarkerWithCircle? = MarkerWithCircle(null,null, null)
        googleMap.setOnMapClickListener (object: GoogleMap.OnMapClickListener {
            override fun onMapClick(position: LatLng?) {
                Log.d("Fragment map", "Map clicked at posistion $position")
                prevMarker = addMarkerWithRadius(position!!, googleMap, prevMarker)
            }
        })
    }
    private fun addMarkerWithRadius(position: LatLng, googleMap: GoogleMap, prevMark: MarkerWithCircle?): MarkerWithCircle? {
        prevMark?.marker?.remove()
        prevMark?.circle?.remove()

        prevMark?.marker = googleMap.addMarker(MarkerOptions().position(position).draggable(true))

        //radius is in meters. Currently set to 10km
        var radius: Double = 10000.0
        activeCircle = googleMap.addCircle(CircleOptions().center(position).radius(radius).strokeColor(Color.BLUE)
            .fillColor(Color.argb(150, 146, 184, 244)))

        //For some reason I have to multiply by 10 to get the correct zoom level
        val zoomLevel = calcZoomLevel(activeCircle.center.latitude, radius * 10)
        //The zoom level is kind of tricky if you change the radius
        googleMap.animateCamera(CameraUpdateFactory
            .newLatLngZoom(activeCircle.center, zoomLevel))

        //Set the radius fragment
        callback.onSetRadiusCall(activeCircle, prevMark!!.marker!!)

        googleMap.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker?) {
                activeCircle.center = marker?.position
            }

            override fun onMarkerDragEnd(marker: Marker?) {
               activeCircle.center = marker?.position
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 11.1.toFloat()))
                Log.d("Fragment map", "Marker moved")
            }

            override fun onMarkerDrag(marker: Marker?) {
                activeCircle.center = marker?.position
            }
        })
        prevMark.circle = activeCircle
        Log.d("Fragment map", "Returning marker")
        return prevMark
    }
    fun updateRadius(radius: Int, circle: Circle) {
        circle.radius = radius.toDouble() * 1000
        //For some reason I have to multiply by 10 to get the correct zoom
        var zoomLevel: Float = calcZoomLevel(circle.center.latitude, circle.radius * 10)
        Log.d("Zoom level", "Zoom level = $zoomLevel")
        googleMap.animateCamera(CameraUpdateFactory
            .newLatLngZoom(LatLng(circle.center.latitude, circle.center.longitude), zoomLevel))

    }
    private fun calcZoomLevel(lat: Double, radius: Double): Float{
        var displayMetrics = DisplayMetrics()
        (activity as MainActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        var maxLength = Math.min(height, width)
        return log(156543.03392 * Math.cos(lat * Math.PI / 180) * maxLength/ radius * 2, 2.0).toFloat()
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
    public fun circleOnUser(radius: Int): Circle {
        val position = LatLng(lastLocation.latitude, lastLocation.longitude)
        activeCircle = googleMap
            .addCircle(CircleOptions()
                .center(position)
                .radius(radius.toDouble())
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(150, 146, 184, 244)))
        //For some reason I have to multiply by 10 to get the correct zoom level
        val zoomLevel = calcZoomLevel(activeCircle.center.latitude, radius.toDouble() * 10)
        //The zoom level is kind of tricky if you change the radius
        googleMap.animateCamera(CameraUpdateFactory
            .newLatLngZoom(activeCircle.center, zoomLevel))
        return activeCircle
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
        val marker: Marker = googleMap.addMarker(MarkerOptions().position(location)
            .icon(BitmapDescriptorFactory
                .fromBitmap(resizeMapIcon("lightning_symbol", 40, 130))))
            Handler().postDelayed({
                marker.remove()
            }, duration)
    }
    /*
    Function overloading if the marker should not be removed after a given time.
    Function returns the marker so caller can handle removal
     */
    fun setMarkerLightning(location: LatLng): Marker {
        val marker: Marker = googleMap.addMarker(MarkerOptions().position(location)
            .icon(BitmapDescriptorFactory
                .fromBitmap(resizeMapIcon("lightning_symbol", 40, 130))))
        return marker
    }
    private fun resizeMapIcon(iconName: String, width: Int, height: Int): Bitmap {
        val imageBitmap: Bitmap = BitmapFactory
            .decodeResource(resources, resources.getIdentifier(iconName, "drawable", activity!!.packageName))
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }
    fun saveMarker(circle: Circle, marker: Marker) {
        triggerAlertDialogName(circle, marker)
    }

    fun triggerAlertDialogName(circle: Circle, marker: Marker) {
        val dialogNameInput = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.dialog_input_name_savemarker, null)
        dialogNameInput.setView(view)

        val descriptionText = view.findViewById<TextView>(R.id.markerName)
        val nameEditText = view.findViewById<EditText>(R.id.markerEditText)
        descriptionText.text = getString(R.string.saveMarkerDescText)

        dialogNameInput.setPositiveButton(getString(R.string.save)) {
            _, _ ->
        }
        val dialog = dialogNameInput.create()
        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                if (nameEditText.text.toString() != "") {
                    markersList.add(MarkerWithCircle(marker, circle, nameEditText.text.toString()))
                    Log.d("Fragment map", "markersList size: " + markersList.size)
                    dialog.dismiss()
                    Toast.makeText(activity, getString(R.string.markerSaved), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.wrongInputMarkerSave), Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun persistentSave() {
        Log.e("Persistent Save", "Saving")
        for (entry: MarkerWithCircle in markersList) {
            val name: String = entry.name!!
            val position: LatLng? = entry.marker?.position
            val radius: Double? = entry.circle?.radius
            savedMarkersList?.add(SavedMarkers(name, position!!.latitude, position.longitude, radius!!))
        }

        markersList.run {
            val prefEditor = sharedPrefs?.edit()
            prefEditor?.putString("SavedMarkers", Gson().toJson(savedMarkersList))
            Log.d("Fragment map", "Markers saved")
            prefEditor?.apply()
        }
    }
    fun clearMap() {
        googleMap.clear()
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
        coRoutine?.cancel()
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("Fragment Map", "Detach")
        persistentSave()
    }
}