package com.example.in2000_project.maps

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.example.in2000_project.*


class MainActivity : BaseActivity() {
    private lateinit var viewModel: MapsViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        this.viewModel = ViewModelProviders.of(this, MapsViewmodelFactory(getPrefs())).get(MapsViewmodel::class.java)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        super.setAlarm()
        super.setDrawer()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.content_frame, MapFragment.newInstance(),
                "Map Fragment").commit()
        }
    }
}

git commit -m "Added repository interfaces for getting lightning data from metProxy(past 48 hours) and frostProxy(historical data). Also added alarmManager that does a request to metProxy every 5 minutes. This backr
ound process starts as the emulator is turned on and when user enters application. Setting for turning this feature off and adjusting request frequensy is not implemented but current implementation exist with these features in mind"