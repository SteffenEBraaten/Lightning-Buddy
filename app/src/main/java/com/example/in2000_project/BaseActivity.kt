package com.example.in2000_project

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar


abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var toolbar: Toolbar


    protected fun setToolbar(title: String, withNavbar: Boolean) {
        this.toolbar = findViewById(R.id.my_toolbar)
        this.toolbar.title = title
        if (withNavbar)
            this.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }
}

