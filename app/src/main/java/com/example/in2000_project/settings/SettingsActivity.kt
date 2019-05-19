package com.example.in2000_project.settings

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import com.example.in2000_project.BaseActivity
import com.example.in2000_project.R
import com.example.in2000_project.maps.MainActivity
import android.net.Uri


class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        super.attachBackButton()
        setSosbutton()
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.settingsFragment,
                SettingsFragment()
            )
            .commit()
    }

    private fun setSosbutton() {
        val button = findViewById<Button>(R.id.help_button)
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:112")
//            intent.data = Uri.parse("@string/police_number")
            startActivity(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.alpha_enter, R.anim.alpha_exit)
            finish()

            return true
    }

}
