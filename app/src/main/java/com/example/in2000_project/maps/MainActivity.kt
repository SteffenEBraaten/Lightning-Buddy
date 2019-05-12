package com.example.in2000_project.maps

import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.example.in2000_project.*
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.Marker


class MainActivity : BaseActivity(), MapFragment.OnSetRadiusListener, RadiusFragment.OnRadiusFragmentChangeListener {
    private lateinit var viewModel: MapsViewmodel
    private var radiusFragment: RadiusFragment? = null
    private lateinit var mapFragment: MapFragment
    private lateinit var activeCircle: Circle
    private lateinit var currentMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewModel = ViewModelProviders.of(this, MapsViewmodelFactory(getPrefs())).get(
            MapsViewmodel::class.java)
        setContentView(R.layout.activity_main)
        darkMode()
        super.setAlarm()
        super.setDrawer()
        createNotificationChannel()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.content_frame, MapFragment.newInstance(),
                "Map Fragment").commit()
        }
    }
    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is MapFragment) {
            mapFragment = fragment
            fragment.setOnRadiusListener(this)
        }
        if (fragment is RadiusFragment) {
            fragment.setOnRadiusChangeListener(this)
        }
    }
    override fun onSetRadiusCall(circle: Circle, marker: Marker) {
        if (radiusFragment != null) {
            Log.d("Main", "Removing last radius fragment")
            supportFragmentManager
                .beginTransaction()
                .remove(radiusFragment as RadiusFragment)
                .commit()
        }
        activeCircle = circle
        currentMarker = marker

        val setRadiusFragment: RadiusFragment = RadiusFragment()
        radiusFragment = setRadiusFragment
        var inputArguments: Bundle = Bundle()
        inputArguments.putString("min", "10")
        inputArguments.putString("max", "800")
        inputArguments.putString("buttonText", resources.getString(R.string.save))
        inputArguments.putString("measure", resources.getString(R.string.km))
        inputArguments.putString("bodyText", resources.getString(R.string.marker_radius_text))
        setRadiusFragment.arguments = inputArguments
        Log.d("Main", "Adding new radius fragment")
        supportFragmentManager.beginTransaction().add(R.id.main_relative, setRadiusFragment).commit()
        attachCancelButton(setRadiusFragment)
    }

    override fun onRadiusChanged(radius: Int) {
        mapFragment.updateRadius(radius, activeCircle)
    }

    override fun onSaveClicked() {
        supportFragmentManager
            .beginTransaction()
            .remove(radiusFragment as RadiusFragment)
            .commit()
        mapFragment.saveMarker(activeCircle, currentMarker)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId: String = "Default"
            val channelName: CharSequence = "Default channel"
            val importance: Int = NotificationManager.IMPORTANCE_HIGH

            val notificationChannel: NotificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.YELLOW
            notificationChannel.enableVibration(true)

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }
    private fun darkMode(){
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = defaultSharedPreferences.getBoolean("darkMode", false)
        if(darkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
