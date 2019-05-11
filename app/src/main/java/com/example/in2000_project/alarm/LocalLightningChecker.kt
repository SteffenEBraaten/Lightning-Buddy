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
                    Log.d("BKG Lightning", "No lightning")
                }


            }catch (e: Exception) {
                Log.e("BKG Lightning","failed to get data: $e")}
        }
    }

    fun getLocalForcastedLightning(context: Context, location: LatLng, radius: Int){
        GlobalScope.launch{
            try {
                val locations = getQueryLocations(location, radius)

                val res = arrayOfNulls<Deferred<ArrayList<WeatherDataUtil.WeatherData>?>>(locations.size)
                for (i in 0 until locations.size){
                    res[i] = async{
                        try {
                            val data = MapRepository().getMetLocationForecastData(locations[i].latitude.toString(), locations[i].longitude.toString())
                            if (!data.isNullOrEmpty()){
                                val weatherData = WeatherDataUtil.createWeatherData(data)
                                weatherData
                            }
                            else{
                                null
                            }
                        }
                        catch (e: Exception){
                            null
                        }
                    }
                }

                var tempData: WeatherDataUtil.WeatherData? = null
                var hasLightning = false
                for (list in res){
                    if (list != null) {
                        val weatherData = list.await()
                        weatherData?.forEach {
                            if (it.symbol!!.id.contains("lightning", true)){
                                hasLightning = true
                                tempData = it
                            }
                        }
                        if (hasLightning) break
                    }
                }
                if (hasLightning) {
                    val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

                    val notBuilder = NotificationCompat
                        .Builder(context, "Default")
                        .setSmallIcon(R.drawable.lightning_symbol)
                        .setContentTitle("Lightning forcast")
                        .setContentText("${tempData!!.symbol!!.id} is forcasted in your current area!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                    with(NotificationManagerCompat.from(context)) {
                        notify(1, notBuilder.build())
                    }
                }
                else{
                    Log.d("BKGForcast" , "No forcasted lightning")
                }
            }
            catch (e: Exception){
                Log.e("BKGForcast","failed to get data: $e")
            }
        }
    }



    private fun getQueryLocations(currentLocation: LatLng, radius: Int): ArrayList<LatLng> {
        val locations = ArrayList<LatLng>()
        locations.add(currentLocation)
        val numPoint: Int = radius / 1000

        val startLat = currentLocation.latitude
        val startLong = currentLocation.longitude

        //North
        for (i in 1..numPoint){
            val newLocation = LatLng(startLat + meterToLatitue(1000) * i, startLong)
            locations.add(newLocation)
        }
        //South

        for (i in 1..numPoint){
            val newLocation = LatLng(startLat - meterToLatitue(1000) * i, startLong)
            locations.add(newLocation)
        }
        //West
        for (i in 1..numPoint){
            val newLocation = LatLng(startLat, startLong + meterToLongtitude(1000, startLat) * i)
            locations.add(newLocation)
        }
        //East
        for (i in 1..numPoint){
            val newLocation = LatLng(startLat, startLong - meterToLongtitude(1000, startLat) * i)
            locations.add(newLocation)
        }
        return locations
    }

    private fun meterToLatitue(m: Int): Double{
        return (m / 111111).toDouble()
    }

    private fun meterToLongtitude(m: Int, lat: Double): Double{
        return (m / (111111 *Math.cos(lat)))
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