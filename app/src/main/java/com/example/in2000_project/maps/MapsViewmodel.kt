package com.example.in2000_project.maps

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.SharedPreferences
import android.util.Log
import android.util.Xml
import android.widget.Toast
import com.example.in2000_project.utils.UalfUtil
import com.example.in2000_project.utils.WeatherdataUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.simpleframework.xml.core.Persister
import java.io.StringReader
import java.lang.Exception

public class MapsViewmodel(private val sharedPref: SharedPreferences) : ViewModel(){

    var recentData = MutableLiveData<ArrayList<UalfUtil.Ualf>>() //observe this and update UI on change

    public fun getRecentApiData(){
        GlobalScope.launch{
            try {
                val data = MapRepository().getMetLightningData()
                if(!data.isNullOrEmpty()){
                    val ualfs = UalfUtil.createUalfs(data)
                    if(!ualfs.isNullOrEmpty()){
                        setRecentData(ualfs)
                        saveRecentData(ualfs)
                    }
                }
            }catch (e: Exception) {Log.e("getRecentApiData","failed to update metLightning: $e")}
        }

//        GlobalScope.launch{
//            try {
//                val data = MapRepository().getMetLocationForecastData("60.10","9.58")
//                if(!data.isNullOrEmpty()){
//                    val serializer = Persister()
//                    val reader = StringReader(data)
//                    val d = serializer.read(WeatherdataUtil.Weatherdata::class.java, reader, false)
//                    Log.e("test","d = ${Gson().toJson(d)}")
//                }
//            }catch (e: Exception) {Log.e("getRecentApiData","failed to update metLocationForecast: $e")}
//        }
    }


    public fun saveRecentData(ualfs: ArrayList<UalfUtil.Ualf>) {
        val sharedPrefEditor = this.sharedPref.edit()

        sharedPrefEditor.putString("recentData", Gson().toJson(ualfs))
        sharedPrefEditor.apply()
    }

    public fun updateRecentData(){
        setRecentData(getSavedRecentData())
    }

    public fun setRecentData(data: ArrayList<UalfUtil.Ualf>?){
        this.recentData.value = data ?: ArrayList()
    }

    public fun getSavedRecentData(): ArrayList<UalfUtil.Ualf>? {
        val jsonList = this.sharedPref.getString("recentData", null)
        if(jsonList.isNullOrEmpty()) return null

        return Gson().fromJson<ArrayList<UalfUtil.Ualf>>(jsonList, object: TypeToken<ArrayList<UalfUtil.Ualf>>(){}.type)
    }
}

@Suppress("UNCHECKED_CAST")
public class MapsViewmodelFactory(private val sharedPref: SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapsViewmodel(sharedPref) as T
    }
}
