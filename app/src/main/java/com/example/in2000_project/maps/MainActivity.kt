package com.example.in2000_project.maps

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.support.v7.preference.PreferenceManager
import android.util.Log
import com.example.in2000_project.*
import com.example.in2000_project.utils.UalfUtil


class MainActivity : BaseActivity() {
    private lateinit var viewModel: MapsViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewModel = ViewModelProviders.of(this, MapsViewmodelFactory(getPrefs())).get(
            MapsViewmodel::class.java)
        setContentView(R.layout.activity_main)
        darkMode()
        super.setAlarm()
        super.setDrawer()
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction().add(R.id.content_frame, MapFragment.newInstance(),
//                "Map Fragment").commit()
//        }
    }

    override fun onResume() {
        super.onResume()
        if (supportFragmentManager.findFragmentByTag("Map Fragment") == null){
            supportFragmentManager.beginTransaction().add(R.id.content_frame, MapFragment.newInstance(),
                "Map Fragment").commit()
        }
    }

    override fun onPause() {
        super.onPause()
        val fragment = supportFragmentManager.findFragmentByTag("Map Fragment")
        Log.e("MainAkt", "Removing: ${fragment}")
        if (fragment != null){
            Log.e("MainAct", "Removing map frag")
            supportFragmentManager.beginTransaction().remove(fragment).commit()
            Log.e("MainAct", "Frag removed: ${supportFragmentManager.findFragmentByTag("Map Fragment")}")
        }
    }

    override fun onStop() {
        super.onStop()
        val fragment = supportFragmentManager.findFragmentByTag("Map Fragment")
        Log.e("MainAkt", "Removing: ${fragment}")
        if (fragment != null){
            Log.e("MainAct", "Removing map frag")
            supportFragmentManager.beginTransaction().remove(fragment).commit()
            Log.e("MainAct", "Frag removed: ${supportFragmentManager.findFragmentByTag("Map Fragment")}")
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
