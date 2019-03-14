package com.example.in2000_project

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions

class MapsViewmodel(context: Context) {
    private val context:Context = context

    //TODO: Decide if setting light and dark mode should rather be done in one function or two functions like I did bellow
    fun setMapDarkMode(map: GoogleMap) {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.darkmode_json))
    }
    fun setLightMode(map: GoogleMap) {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.standard_json))
    }
}