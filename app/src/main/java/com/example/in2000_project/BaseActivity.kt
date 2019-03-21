package com.example.in2000_project

import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.content.Intent
import android.support.design.widget.NavigationView
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import com.example.in2000_project.Settings.SettingsActivity


abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout

    data class Settings(
        val useLocation: Boolean,
        val language: String,
        val allowNotifications: Boolean,
        val email: String,
        val vibrate: Boolean,
        val termsOfService: Boolean
    )

    protected fun getPrefs() : SharedPreferences{
        return PreferenceManager.getDefaultSharedPreferences(baseContext)
    }

    protected fun getSettings() : Settings{
        val sharedPrefs = getPrefs()
        return Settings(
                    useLocation = sharedPrefs.getBoolean("useLocation", false),
                    language = sharedPrefs.getString("language", "English") as String,
                    allowNotifications = sharedPrefs.getBoolean("allowNotifications", true),
                    email = sharedPrefs.getString("email", "") as String,
                    vibrate = sharedPrefs.getBoolean("vibrate", true),
                    termsOfService = sharedPrefs.getBoolean("termsOfService", false)
                )
    }

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
            finish()
        }
    }


    protected fun setDrawer(){
        setToolbar(getString(R.string.app_name), R.drawable.ic_menu_black_24dp)
        this.drawer = findViewById(R.id.drawer_layout)
        this.toolbar.setNavigationOnClickListener{
            this.drawer.openDrawer(GravityCompat.START)
        }

        setNavigationView()
    }

    private fun setNavigationView(){
        val drawerView = findViewById<NavigationView>(R.id.nav_view)
        drawerView.setNavigationItemSelectedListener(this)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.drawercontent_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }

        return true
    }


}

