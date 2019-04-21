package com.example.in2000_project.maps

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.support.v7.preference.PreferenceManager
import com.example.in2000_project.*


class MainActivity : BaseActivity() {
    private lateinit var viewModel: MapsViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        this.viewModel = ViewModelProviders.of(this, MapsViewmodelFactory(getPrefs())).get(
            MapsViewmodel::class.java)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        darkMode()
        super.setAlarm()
        super.setDrawer()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.content_frame, MapFragment.newInstance(),
                "Map Fragment").commit()
        }
    }

    private fun darkMode(){
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = defaultSharedPreferences.getBoolean("darkMode", false)
        if(darkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

    }
}
