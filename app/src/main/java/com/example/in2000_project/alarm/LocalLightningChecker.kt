package com.example.in2000_project.alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.example.in2000_project.R
import com.example.in2000_project.maps.MainActivity
import com.example.in2000_project.maps.MapRepository
import com.example.in2000_project.utils.UalfUtil
import com.example.in2000_project.utils.WeatherDataUtil
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.lang.Exception

class LocalLightningChecker {


    fun getLocalLightning(context: Context, location: LatLng, radius: Int) {
        GlobalScope.launch{
            try {

                var hasLightning = false

                val data = MapRepository().getMetLightningData()
                if (!data.isNullOrEmpty()){
                    val ualfs = UalfUtil.createUalfs(data)
                    if (!ualfs.isNullOrEmpty()){
                        ualfs.forEach {
                            if (distanceTwoPoints(location.latitude, location.longitude, it.lat, it.long) < radius){
                                hasLightning = true
                            }
                        }
                    }
                }

                if (hasLightning){
                    val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

                    val notBuilder = NotificationCompat
                        .Builder(context, "Default")
                        .setSmallIcon(R.drawable.lightning_symbol)
                        .setContentTitle("New lightning")
                        .setContentText("High chance of lightning in your local area!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                    with(NotificationManagerCompat.from(context)) {
                        notify(1, notBuilder.build())
                    }

                }
                else{
                    Log.e("Background check", "NO lightning")
                }


            }catch (e: Exception) {
                Log.e("Background tracker","failed to get data: $e")}
        }
    }


    private fun distanceTwoPoints(lat1: Double, long1: Double, lat2: Double, long2: Double): Double{

        val R = 6371 // Radius of the earth in km
        val dLat = deg2rad(lat2-lat1)  // deg2rad below
        val dLon = deg2rad(long2-long1)
        val a =
            Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                    Math.sin(dLon/2) * Math.sin(dLon/2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        val d = R * c // Distance in km
        return d
    }

    private fun deg2rad(deg: Double): Double{
        return deg * (Math.PI/180)
    }

}