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
import com.example.in2000_project.utils.WeatherDataUtil
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class LocalLightningChecker {


    fun getLocalLightning(context: Context, locations: ArrayList<LatLng>) {
        GlobalScope.launch{
            try {
                val res = arrayOfNulls<ArrayList<WeatherDataUtil.WeatherData>?>(locations.size)
                val coroutines = arrayOfNulls<Job?>(locations.size)
                for (i in 0..locations.size){
                    coroutines[i] = GlobalScope.launch {
                        try {
                            val data = MapRepository().getMetLocationForecastData(locations[i].latitude.toString(), locations[i].longitude.toString())
                            if (!data.isNullOrEmpty()){
                                val weatherData = WeatherDataUtil.createWeatherData(data)
                                res[i] = weatherData
                            }
                        }
                        catch (e: Exception){

                        }
                    }
                }
                for (i in 0..locations.size){
                    coroutines[i]?.join()
                }
                var hasLightning = false
                for (i in 0..locations.size){
                    if (hasLightning) break
                    res[i]?.forEach {
                        if (it.symbol != null){
                            if (it.symbol!!.id.contains("Lightning")){
                                hasLightning = true
                            }
                        }
                    }
                }

//                val data = MapRepository().getMetLocationForecastData("60.10","9.58")


//                if(!data.isNullOrEmpty()){
//                    val weatherdata = WeatherDataUtil.createWeatherData(data)
//                    if(weatherdata.isNotEmpty()){
//                        Log.e("WEATHERDATA","NOT EMPTY:\n${Gson().toJson(weatherdata)}")
//                        val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
//                        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
//
//                        var notBuilder = NotificationCompat
//                            .Builder(context, "Default")
//                            .setSmallIcon(R.drawable.lightning_symbol)
//                            .setContentTitle("New lightning")
////                            .setContentText("${location.longitude}  ${location.latitude}")
//                            .setContentText("123123123")
//                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                            .setContentIntent(pendingIntent)
//                            .setAutoCancel(true)
//
//                        with(NotificationManagerCompat.from(context)) {
//                            notify(1, notBuilder.build())
//                        }
//                    }
//                }
            }catch (e: Exception) {
                Log.e("getRecentApiData","failed to update metLocationForecast: $e")}
        }
    }




}