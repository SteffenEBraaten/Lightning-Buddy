package com.example.in2000_project

import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.R.menu
import android.view.MenuInflater




abstract class BaseActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout

    //R.drawable.ic_arrow_back_white_24dp => back nav button
    private fun setToolbar(title: String, navImg: Int?) {
        this.toolbar = findViewById(R.id.my_toolbar)
        this.toolbar.title = title
        if (navImg != null) {
            this.toolbar.setNavigationIcon(navImg)
            setSupportActionBar(toolbar)
        }
    }

    protected fun attachBackButton(){
        //TODO: navigate back to previous activity or home
        setToolbar(getString(R.string.app_name), R.drawable.ic_arrow_back_black_24dp)
        this.toolbar.setNavigationOnClickListener{
            //navback
        }
    }


    protected fun setDrawer(){
        setToolbar(getString(R.string.app_name), R.drawable.ic_menu_black_24dp)
        this.drawer = findViewById(R.id.drawer_layout)
        this.toolbar.setNavigationOnClickListener{
            this.drawer.openDrawer(GravityCompat.START)
        }
    }


}

