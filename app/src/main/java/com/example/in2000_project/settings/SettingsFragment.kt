package com.example.in2000_project.settings

import android.util.Patterns
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager
import android.widget.Toast
import com.example.in2000_project.R
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import com.example.in2000_project.alarm.AlarmService
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.widget.*
import kotlinx.android.synthetic.main.dialog_set_start_and_end_time.view.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.SharedPreferences

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        attachEvents()
        setSummaries()
        setAlarm()
    }

    private fun refreshFragment(){
        fragmentManager!!.beginTransaction()
            .replace(
                R.id.settingsFragment,
                SettingsFragment()
            )
            .commit()
    }

    private fun setSummaries() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.context)
        var lightningDataFrequency = sharedPrefs.getString("lightningDataFrequency", "5")

        if(lightningDataFrequency!!.toInt() <= 0) lightningDataFrequency = getString(R.string.noUpdates)
        else lightningDataFrequency = getString(R.string.every) + " " + lightningDataFrequency + " " + getString(R.string.minutes)
        preferenceScreen.findPreference("lightningDataFrequency").summary = lightningDataFrequency

        val fromTime = sharedPrefs.getString("fromTime", "")
        val toTime = sharedPrefs.getString("toTime", "")
        var string = getString(R.string.from) + " " + fromTime + " " + getString(R.string.to) + " " + toTime
        if(fromTime != "" && toTime != "") {
            if(fromTime > toTime){
                string = getString(R.string.from) + " " + fromTime + " " + getString(R.string.toNextDay) + " " + toTime
                preferenceScreen.findPreference("silentMode").summary = string
            }
            else preferenceScreen.findPreference("silentMode").summary = string
        }
        else string = getString(R.string.noTimeHasBeenSet)
        preferenceScreen.findPreference("silentMode").summary = string
    }

    private fun attachEvents(){
        val termsOfService = findPreference("termsOfService")
        termsOfService?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val alert = unaryAlertDialogCreator(
                getString(R.string.termsOfServiceTitle),
                getString(R.string.termsOfService),
                getString(R.string.Close),
                {})

            alert.show()
            true
        }

        val reset = findPreference("resetSettings")
        reset?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val alert = binaryAlertDialogCreator(
                getString(R.string.reset),
                getString(R.string.resetSettingsMessage),
                getString(R.string.reset),
                getString(R.string.cancel),
                ::resetSettings,
                {})
            alert.show()
            true
        }

        val darkMode = findPreference("darkMode")
        darkMode?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            setDarkMode(value as Boolean)
            reStart()
            true
        }

        val silent = findPreference("silentMode")
        silent?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_set_start_and_end_time, null)
            val builder = AlertDialog.Builder(context!!).setView(dialogView)
            val alertDialog = builder.show()

            val fromTimeEditText = dialogView.findViewById<EditText>(R.id.fromTime)
            val toTimeEditText = dialogView.findViewById<EditText>(R.id.toTime)

            fromTimeEditText.setOnClickListener {
                val c = Calendar.getInstance()
                val timeSetListener = TimePickerDialog.OnTimeSetListener{_, hour, minute ->
                    c.set(Calendar.HOUR_OF_DAY, hour)
                    c.set(Calendar.MINUTE, minute)
                    fromTimeEditText.setText(SimpleDateFormat("HH : mm").format(c.time))

                }
                TimePickerDialog(this.context, timeSetListener, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), true ).show()
            }

            toTimeEditText.setOnClickListener {
                val c = Calendar.getInstance()
                val timeSetListener = TimePickerDialog.OnTimeSetListener{_, hour, minute ->
                    c.set(Calendar.HOUR_OF_DAY, hour)
                    c.set(Calendar.MINUTE, minute)
                    toTimeEditText.setText(SimpleDateFormat("HH : mm").format(c.time))
                }
                TimePickerDialog(this.context, timeSetListener, c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE), true ).show()
            }

            dialogView.add_Button.setOnClickListener {
                var validTime  = true
                val fromTimeString = fromTimeEditText.text.toString()
                val toTimeString = toTimeEditText.text.toString()
                var string = getString(R.string.from) + " " + fromTimeString + " " + getString(R.string.to) + " " + toTimeString

                if (fromTimeString == "" || toTimeString == ""){
                    validTime = false
                    if (fromTimeString == "" && toTimeString == ""){
                        validTime = true
                        string = getString(R.string.noTimeHasBeenSet)
                        preferenceScreen.findPreference("silentMode").summary = string

                    }
                    else if(fromTimeString == ""){
                        Toast.makeText(context, getString(R.string.pleaseSelectTimeFrom), Toast.LENGTH_LONG).show()
                        fromTimeEditText.performClick()
                    }
                    else{
                        Toast.makeText(context, getString(R.string.pleaseSelectTimeTo), Toast.LENGTH_LONG).show()
                        toTimeEditText.performClick()
                    }
                }

                if(fromTimeString == toTimeString && fromTimeString != ""){
                    validTime = false
                    Toast.makeText(context, getString(R.string.invalidTimePeriod), Toast.LENGTH_LONG).show()
                    fromTimeEditText.performClick()
                }

                if(validTime) {
                    alertDialog.dismiss()
                    if(fromTimeString =="" && toTimeString == ""){
                        Toast.makeText(context, getString(R.string.noTimeHasBeenSet), Toast.LENGTH_LONG).show()
                    }
                    if(fromTimeString > toTimeString){
                        string = getString(R.string.from) + " " + fromTimeString + " " + getString(R.string.toNextDay) + " " + toTimeString
                        preferenceScreen.findPreference("silentMode").summary = string
                        Toast.makeText(context, getString(R.string.theNotificationsWillNotBePushedFrom) + " " + fromTimeString + " " + getString(R.string.toNextDay) + " " + toTimeString, Toast.LENGTH_LONG).show()
                    }
                    if(fromTimeString < toTimeString){
                        preferenceScreen.findPreference("silentMode").summary = string
                        Toast.makeText(context, getString(R.string.theNotificationsWillNotBePushedFrom) + " " + fromTimeString + " " + getString(R.string.toNextDay) + " " + toTimeString, Toast.LENGTH_LONG).show()
                    }

                    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.context)
                    val sharedPrefsEditor = sharedPrefs.edit()
                    sharedPrefsEditor.putString("fromTime", fromTimeString)
                    sharedPrefsEditor.putString("toTime", toTimeString)
                    sharedPrefsEditor.apply()
                }
            }
            true
        }

        preferenceScreen.findPreference("lightningDataFrequency").setOnPreferenceChangeListener { _, _ ->
            refreshFragment()
            true
        }

        preferenceScreen.findPreference("giveFeedback").setOnPreferenceClickListener {
            emailFeedback()
            true
        }
    }

    private fun emailFeedback(){
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("lightningbuddy.feedback@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT,"Feedback")
        startActivity(intent)
    }

    private fun setAlarm(){
        val serviceIntent = Intent(this.activity, AlarmService::class.java)
        val minutes = PreferenceManager.getDefaultSharedPreferences(context).getString("lightningDataFrequency", "5")
        serviceIntent.putExtra("minutes", minutes)
        this.activity!!.startService(serviceIntent)
    }

    private fun darkMode(){
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context)
        val darkMode = defaultSharedPreferences.getBoolean("darkMode", false)
        setDarkMode(darkMode)
    }

    private fun setDarkMode(isDarkMode: Boolean) {
        if(isDarkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun reStart(){
        startActivity(Intent(activity, SettingsActivity::class.java))
        activity!!.overridePendingTransition(R.anim.alpha_enter,R.anim.alpha_exit)
        activity!!.finish()
    }

    private fun resetSettings() {
        val sharedPrefsEditor = PreferenceManager.getDefaultSharedPreferences(this.context).edit()
        sharedPrefsEditor.putBoolean("allowNotifications", true)
        sharedPrefsEditor.putString("SavedMarkers", "")
        sharedPrefsEditor.putString("lightningDataFrequency", "5")
        sharedPrefsEditor.putBoolean("darkMode", false)
        sharedPrefsEditor.putString("fromTime", "")
        sharedPrefsEditor.putString("toTime", "")
        sharedPrefsEditor.apply()
        refreshFragment()
        darkMode()
        reStart()
    }


    private fun unaryAlertDialogCreator(title : String, message : String, posBtn : String, pos : () -> Unit) : AlertDialog{
        val alertBuilder = AlertDialog.Builder(this.context as Context)
        alertBuilder.setTitle(title)
        alertBuilder.setMessage(message)

        alertBuilder.setPositiveButton(posBtn) { dialog, _ ->
            pos()
            dialog.dismiss()
        }

        return alertBuilder.create()
    }


    private fun binaryAlertDialogCreator(title : String, message : String, posBtn : String, negBtn : String, pos : () -> Unit, neg : () -> Unit) : AlertDialog {
        val alertBuilder = AlertDialog.Builder(this.context as Context)
        alertBuilder.setTitle(title)
        alertBuilder.setMessage(message)

        alertBuilder.setPositiveButton(posBtn) { dialog, _ ->
            pos()
            dialog.dismiss()
        }
        alertBuilder.setNegativeButton(negBtn) { dialog, _ ->
            neg()
            dialog.dismiss()
        }

        return alertBuilder.create()
    }
}
