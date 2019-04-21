package com.example.in2000_project.settings

import android.os.Bundle
import com.example.in2000_project.BaseActivity
import com.example.in2000_project.R

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        super.attachBackButton()
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.settingsFragment,
                SettingsFragment()
            )
            .commit()
    }


}
