package com.example.in2000_project

import android.os.Bundle
import android.support.v7.app.AppCompatActivity


abstract class BaseActivity : AppCompatActivity() {
    /*
        private val settings = getSharedPreferences("prefs", 0)
        private val editor = settings.edit()

        fun editSharePrefs(): SharedPreferences.Editor {
            return editor
        }
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
