package com.example.in2000_project.maps

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.SharedPreferences
import android.util.Log
import com.bumptech.glide.load.engine.executor.GlideExecutor.UncaughtThrowableStrategy.LOG
import com.example.in2000_project.utils.DateUtil
import com.example.in2000_project.utils.UalfUtil
import com.example.in2000_project.utils.WeatherDataUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class MapsViewmodel(private val sharedPref: SharedPreferences) : ViewModel(){
    companion object Data {
        val recentData = MutableLiveData<ArrayList<UalfUtil.Ualf>>() //observe this and update UI on change
    }

    fun getRecentApiData(){
        GlobalScope.launch{
            try {
                val data = MapRepository().getMetLightningData()
                if (!data.isNullOrEmpty()) {
                    val ualfs = UalfUtil.createUalfs(data)
                    if (!ualfs.isNullOrEmpty()) {
                        setRecentData(ualfs)
                        saveRecentData(ualfs)
                    }
                }
            } catch (e: Exception) {
                Log.e("getRecentApiData", "failed to update metLightning: $e")
            }

        }

        GlobalScope.launch{
            try {
                val data = MapRepository().getMetLocationForecastData("60.10","9.58")
                if(!data.isNullOrEmpty()){
                    val weatherdata = WeatherDataUtil.createWeatherData(data)
                    if(weatherdata.isNotEmpty()){
                        Log.d("WEATHERDATA","NOT EMPTY:\n${Gson().toJson(weatherdata)}")
                    }
                }
            }catch (e: Exception) {Log.e("getRecentApiData","failed to update metLocationForecast: $e")}
        }
    }
    /*
    For some reason it doubles whatever amount gets sent in. Can't seem to figure out why.
    Due to time constraints I figure this is a feature not a bug XD
    Also, it is not possible to display only 1 lightning...
    */
    public fun getDummyData(amount: Int) {
        var dummyDataString = ""
        for (x in 0 until amount) {
            var date: Date = Date()
            val dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy mm dd hh mm ss")
            val dateString: String = dateFormatter.format(date)

            dummyDataString += "0 "
            dummyDataString += dateString
            dummyDataString += " 0 "

            val randomGenerator: Random = Random()
            //Add location
            val latMax: Double = 71.735
            val latMin: Double = 57.7
            val latitude: Double = latMin + (latMax - latMin) * randomGenerator.nextDouble()

            val longMax: Double = 22.83
            val longMin: Double = 3.85
            val longitude: Double = longMin + (longMax - longMin) * randomGenerator.nextDouble()
            Log.d("getDummmyData()", "Dummy data lat/lng: $latitude $longitude")

            dummyDataString += latitude.toString()
            dummyDataString += " " + longitude.toString()
            dummyDataString += " 10 10 10 10 10 10 10.0 10.0 10.0 10.0"
            Log.d("dummyDataString:", "$dummyDataString")
            dummyDataString += "\n"
        }
        val ualfs = UalfUtil.createUalfs(dummyDataString)
        if (!ualfs.isNullOrEmpty()) {
            setRecentData(ualfs)
            saveRecentData(ualfs)
        }


    }


    fun saveRecentData(ualfs: ArrayList<UalfUtil.Ualf>) {
        val sharedPrefEditor = this.sharedPref.edit()

        sharedPrefEditor.putString("recentData", Gson().toJson(ualfs))
        sharedPrefEditor.apply()
    }

    fun updateRecentData(){
        setRecentData(getSavedRecentData())
    }

    fun setRecentData(data: ArrayList<UalfUtil.Ualf>?){
        MapsViewmodel.recentData.postValue(data)
    }

    fun getSavedRecentData(): ArrayList<UalfUtil.Ualf>? {
        val jsonList = this.sharedPref.getString("recentData", null)
        if(jsonList.isNullOrEmpty()) return null

        return Gson().fromJson<ArrayList<UalfUtil.Ualf>>(jsonList, object: TypeToken<ArrayList<UalfUtil.Ualf>>(){}.type)
    }
}

@Suppress("UNCHECKED_CAST")
class MapsViewmodelFactory(private val sharedPref: SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapsViewmodel(sharedPref) as T
    }
}
