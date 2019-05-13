package com.example.in2000_project

import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.content.Intent
import android.support.design.widget.NavigationView
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.preference.PreferenceManager
import android.util.Log
import com.example.in2000_project.LightningHistory.LightningHistoryActivity
import com.example.in2000_project.alarm.AlarmService
import com.example.in2000_project.maps.*
import com.example.in2000_project.settings.SettingsActivity


abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout

    data class Settings(
        val useLocation: Boolean,
        val darkMode: Boolean,
        val allowNotifications: Boolean,
        val email: String,
        val vibrate: Boolean,
        val termsOfService: Boolean
    )

    protected fun setAlarm(){
        val serviceIntent = Intent(this, AlarmService::class.java)
        val minutes = getPrefs().getString("lightningDataFrequency", "5")
        serviceIntent.putExtra("minutes", minutes!!)
        this.startService(serviceIntent)
    }

    protected fun getPrefs() : SharedPreferences{
        return PreferenceManager.getDefaultSharedPreferences(baseContext)
    }

    protected fun getSettings() : Settings{
        val sharedPrefs = getPrefs()
        return Settings(
                    useLocation = sharedPrefs.getBoolean("useLocation", false),
                    darkMode = sharedPrefs.getBoolean("darkMode", false),
                    allowNotifications = sharedPrefs.getBoolean("allowNotifications", true),
                    email = sharedPrefs.getString("email", "") as String,
                    vibrate = sharedPrefs.getBoolean("vibrate", true),
                    termsOfService = sharedPrefs.getBoolean("termsOfService", false)
                )
    }

    protected fun setToolbar(title: String, navImg: Int?) {
        this.toolbar = findViewById(R.id.my_toolbar)
        this.toolbar.title = title
        if (navImg != null) {
            this.toolbar.setNavigationIcon(navImg)
            setSupportActionBar(toolbar)
        }
    }

    protected fun attachBackButton(){
        setToolbar(getString(R.string.app_name), R.drawable.ic_arrow_back_black_24dp)
        this.toolbar.setNavigationOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.alpha_enter, R.anim.alpha_exit)
            finish()


        }
    }
    protected fun attachCancelButton(fragment: Fragment) {
        setToolbar(getString(R.string.app_name), R.drawable.abc_ic_clear_material)
        this.toolbar.setNavigationOnClickListener {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
            setDrawer()
            val mapFragment: MapFragment =
                supportFragmentManager.findFragmentByTag("Map Fragment") as MapFragment
            mapFragment.clearMap()
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
            R.id.drawercontent_set_radius -> {
                startRadiusFragment(item)
            }
            R.id.drawercontent_lightninghistory -> startActivity(Intent(this, LightningHistoryActivity::class.java))

        }

        return true
    }

    private fun startRadiusFragment(item: MenuItem) {
        var setRadiusFragment: RadiusFragment = RadiusFragment()
        var inputArguments: Bundle = Bundle()
        inputArguments.putString("min", "0")
        inputArguments.putString("max", "1000")
        inputArguments.putString("buttonText", resources.getString(R.string.set))
        inputArguments.putString("measure", resources.getString(R.string.km))
        inputArguments.putString("bodyText", resources.getString(R.string.user_radius_text))
        setRadiusFragment.arguments = inputArguments

        supportFragmentManager.beginTransaction().add(R.id.main_relative, setRadiusFragment).commit()
        item.isChecked = false
        this.drawer.closeDrawer(GravityCompat.START)
        attachCancelButton(setRadiusFragment)
    }
}

