package com.example.in2000_project.alarm

import android.app.*
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.PowerManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.content.ContextCompat.startActivities
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.example.in2000_project.R
import com.example.in2000_project.maps.MainActivity
import com.example.in2000_project.maps.MapFragment
import com.example.in2000_project.maps.MapsViewmodel
import com.example.in2000_project.maps.MapsViewmodelFactory
import com.example.in2000_project.utils.UalfUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.RoundingMode

class Alarm : BroadcastReceiver() {
    private var frequency: Int = 0
    private lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Partial wake lock: get api data")
        wl.acquire(60 * 1000L /*10 minutes*/)
        MapsViewmodel(PreferenceManager.getDefaultSharedPreferences(context)).getRecentApiData()
        inspectRecentData()
//        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show() // For example

        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        val flc = LocationServices.getFusedLocationProviderClient(context)
        flc.lastLocation.addOnSuccessListener {
                location: Location? ->
                    Log.e("BACKGROUND LOC", "${location}")
                    if (location != null) {
                        val sharedPrefs: SharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context)
                        val radius = sharedPrefs.getInt("UserRadius", 1000)
                        if (radius != 0) {
                            Log.d("User radius", "User radius = $radius")
                            LocalLightningChecker().getLocalLightning(this.context, LatLng(location.latitude, location.latitude), radius)
                            LocalLightningChecker().getLocalForcastedLightning(this.context, LatLng(location.latitude, location.latitude), radius)
                        } else {
                            Log.d("User radius", "User's radius config either not set or set to 0.")
                        }
                    }
        }

        wl.release()
    }





    private fun inspectRecentData() {
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var savedMarkersList: MutableSet<MapFragment.SavedMarkers>? = null

        val jsonLinkedList = sharedPrefs.getString("SavedMarkers", null)
        if (jsonLinkedList != null) {
            savedMarkersList = Gson().fromJson(jsonLinkedList, object: TypeToken<MutableSet<MapFragment.SavedMarkers>>(){}.type)
            Log.d("Alarm", "Saved markers retrieved")
            Log.d("Alarm", savedMarkersList.toString())
        }
        if (savedMarkersList == null) {
            Log.d("Alarm", "No saved markers")
            savedMarkersList = mutableSetOf()
        }
        val notificationId = 0
        if (!savedMarkersList.isNullOrEmpty()) {
            val recentData: ArrayList<UalfUtil.Ualf>? = MapsViewmodel.recentData.value
            recentData?.forEach {
                val latitude: Double = it.lat
                val longitude: Double = it.long

                val newLocation = Location("")
                newLocation.latitude = latitude
                newLocation.longitude = longitude

                savedMarkersList.forEach {
                    val savedLatitude: Double = it.latitude
                    val savedLongitude: Double = it.longitude
                    val savedRadius: Double = it.radius

                    val savedLocation = Location("")
                    savedLocation.latitude = savedLatitude
                    savedLocation.longitude = savedLongitude
                    if (savedLocation.distanceTo(newLocation) <= savedRadius) {
                        val notification: NotificationCompat.Builder? = buildNotification(newLocation)
                        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)

                        notificationManager.notify(notificationId, notification!!.build())

                    }
                }

            }
        }
    }
    private fun buildNotification(position: Location): NotificationCompat.Builder? {
        val intent: Intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val notification = NotificationCompat.Builder(context, "Default")
            .setSmallIcon(R.drawable.lightning_symbol)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(
                context.getString(R.string.notification_content) +
                        " (" + position.latitude.toBigDecimal().setScale(3, RoundingMode.UP).toString() + ", " +
                        position.longitude.toBigDecimal().setScale(3, RoundingMode.UP).toString() + ")"
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            //Set intent that launches when notification is tapped
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
        return notification
    }


    fun setAlarm(context: Context, minutes: Int) {
        this.context = context
        val alarmUp = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, Alarm::class.java),
            PendingIntent.FLAG_NO_CREATE
        ) != null

        if(alarmUp && minutes == this.frequency) return
        this.frequency = minutes

        if(alarmUp) cancelAlarm(context)
        if (minutes == 0) return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, Alarm::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, i, 0)
        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            (1000 * 60 * minutes).toLong(),
//            (minutes).toLong(),
            pi
        )
    }

    private fun cancelAlarm(context: Context) {
        val intent = Intent(context, Alarm::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }
}