package com.example.in2000_project

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        super.attachBackButton()
        supportFragmentManager.beginTransaction()
            .replace(R.id.settingsFragment, SettingsFragment())
            .commit()
    }


}
