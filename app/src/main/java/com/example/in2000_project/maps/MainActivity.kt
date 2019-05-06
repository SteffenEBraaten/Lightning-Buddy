package com.example.in2000_project.maps

import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.support.v7.preference.PreferenceManager
import com.example.in2000_project.*
import com.example.in2000_project.utils.UalfUtil


class MainActivity : BaseActivity() {
    private lateinit var viewModel: MapsViewmodel

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
