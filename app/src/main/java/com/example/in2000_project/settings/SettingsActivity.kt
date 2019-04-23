package com.example.in2000_project.settings

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.example.in2000_project.BaseActivity
import com.example.in2000_project.R
import com.example.in2000_project.maps.MainActivity


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

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.alpha_enter, R.anim.alpha_exit)
            finish()

            return true
    }

}
