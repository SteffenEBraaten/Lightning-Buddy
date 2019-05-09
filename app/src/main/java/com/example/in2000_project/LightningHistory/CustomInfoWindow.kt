package com.example.in2000_project.LightningHistory

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import com.example.in2000_project.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindow(val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker?): View {
        val infoView = (context as Activity).layoutInflater.inflate(R.layout.info_window_history_marker, null)
        val infoData: InfoWindowData? = p0?.tag as InfoWindowData?

        infoView.findViewById<TextView>(R.id.info_title)?.text = "Lat: ${infoData?.lat}\tLong: ${infoData?.long}"
        infoView.findViewById<TextView>(R.id.info_content)?.text = "Date: ${infoData?.time}"
        return infoView
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}