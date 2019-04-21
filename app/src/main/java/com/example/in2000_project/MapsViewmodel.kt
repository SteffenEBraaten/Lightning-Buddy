package com.example.in2000_project

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.SharedPreferences
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import com.example.in2000_project.maps.MainActivity
import com.example.in2000_project.maps.MapRepository
import com.example.in2000_project.utils.UalfUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

public class MapsViewmodel(private val sharedPref: SharedPreferences) : ViewModel(){

    var recentData = MutableLiveData<ArrayList<UalfUtil.Ualf>>() //observe this and update UI on change

    public fun getRecentApiData(){
        GlobalScope.launch{
            val data = MapRepository().getMetLightningData()
            if(!data.isNullOrEmpty()){
                val ualfs = UalfUtil.createUalfs(data)
                if(!ualfs.isNullOrEmpty()){
                    setRecentData(ualfs)
                    saveRecentData(ualfs)
                }
            }
        }
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

public class MapsViewmodelFactory(private val sharedPref: SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapsViewmodel(sharedPref) as T
    }
}
