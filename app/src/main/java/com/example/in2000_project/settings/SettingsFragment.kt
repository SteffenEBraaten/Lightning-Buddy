package com.example.in2000_project.settings


import android.util.Patterns
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager
import android.widget.Toast
import com.example.in2000_project.R
import android.support.v7.app.AppCompatDelegate
import com.example.in2000_project.alarm.AlarmService
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.widget.*
import kotlinx.android.synthetic.main.dialog_set_start_and_end_time.view.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.SharedPreferences








// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
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
        val email = sharedPrefs.getString("email", "")
        preferenceScreen.findPreference("email").summary = email
        var lightningDataFrequency = sharedPrefs.getString("lightningDataFrequency", "5")

        if(lightningDataFrequency!!.toInt() <= 0) lightningDataFrequency = getString(R.string.noUpdates)
        else lightningDataFrequency = getString(R.string.every) + " " + lightningDataFrequency + " " + getString(R.string.minutes)
        preferenceScreen.findPreference("lightningDataFrequency").summary = lightningDataFrequency

        val sharedPrefs1: SharedPreferences = context!!.getSharedPreferences("setTime", Context.MODE_MULTI_PROCESS)
        val fromTime = sharedPrefs1.getString("fromTime", "")
        val toTime = sharedPrefs1.getString("toTime", "")
        var string = "From " + fromTime + " to " + toTime
        if(fromTime != "" && toTime != "") {
            if(fromTime > toTime){
                string = "From " + fromTime + " to next day " + toTime
                preferenceScreen.findPreference("silentMode").summary = string
            }
            else preferenceScreen.findPreference("silentMode").summary = string
        }
        else string = "No time has been set"
        preferenceScreen.findPreference("silentMode").summary = string

    }

    private fun attachEvents(){
        val termsOfService = findPreference("termsOfService")
        termsOfService?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val alert = binaryAlertDialogCreator(
                getString(R.string.termsOfServiceTitle),
                getString(R.string.termsOfService),
                getString(R.string.accept),
                getString(R.string.decline),
                ::acceptTermsAgreement,
                ::declineTermsAgreement)

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

        preferenceScreen.findPreference("email").setOnPreferenceChangeListener { preference, value ->
            if(value == "" || isValidEmail(value as String)){
                refreshFragment()
            }else{
                val sharedPrefsEditor = PreferenceManager.getDefaultSharedPreferences(this.context).edit()
                sharedPrefsEditor.putString(preference.key, "")
                sharedPrefsEditor.apply()
                Toast.makeText(activity, getString(R.string.invalidEmail), Toast.LENGTH_SHORT).show()
            }
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
                    var validDates  = true
                    val fromTimeString = fromTimeEditText.text.toString()
                    val toTimeString = toTimeEditText.text.toString()
                    var string = "From " + fromTimeString + " to " + toTimeString

                    if (fromTimeString == "" || toTimeString == ""){
                        validDates = false
                        if (fromTimeString == "" && toTimeString == ""){
                            validDates = true
                            string = "No time has been set"
                            preferenceScreen.findPreference("silentMode").summary = string

                        }
                        else if(fromTimeString == ""){
                            Toast.makeText(context, "Please select time from", Toast.LENGTH_LONG).show()
                            fromTimeEditText.performClick()
                        }
                        else{
                            Toast.makeText(context, "Please select time to", Toast.LENGTH_LONG).show()
                            toTimeEditText.performClick()
                        }
                    }

                    if(fromTimeString == toTimeString && fromTimeString != ""){
                        validDates = false
                        Toast.makeText(context, "Invalid time period", Toast.LENGTH_LONG).show()
                        fromTimeEditText.performClick()
                    }

                    if(validDates) {
                        alertDialog.dismiss()
                        if(fromTimeString > toTimeString){
                            string = "From " + fromTimeString + " to next day " + toTimeString
                            preferenceScreen.findPreference("silentMode").summary = string
                            Toast.makeText(context, "The notifications will not be pushed from " + fromTimeString + " to next day " + toTimeString, Toast.LENGTH_LONG).show()
                        }
                        else{
                            preferenceScreen.findPreference("silentMode").summary = string
                            Toast.makeText(context, "The notifications will not be pushed from " + fromTimeString + " to " + toTimeString, Toast.LENGTH_LONG).show()
                        }

                        val sharedPrefs: SharedPreferences = this.context!!.getSharedPreferences("setTime", Context.MODE_MULTI_PROCESS)
                        val sharedPrefsEditor = sharedPrefs.edit()
                        sharedPrefsEditor.putString("fromTime", fromTimeString)
                        sharedPrefsEditor.putString("toTime", toTimeString)
                        sharedPrefsEditor.apply()
                    }
                }
            true
        }
    }



    private fun setAlarm(){
        val serviceIntent = Intent(this.activity, AlarmService::class.java)
        val minutes = android.preference.PreferenceManager.getDefaultSharedPreferences(context).getString("lightningDataFrequency", "5")
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

    private fun isValidEmail(email : String) : Boolean{
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun acceptTermsAgreement(){
        setTermsAgreement(true)
    }

    private fun declineTermsAgreement(){
        setTermsAgreement(false)
    }

    private fun setTermsAgreement(value : Boolean){
        val sharedPrefsEditor = PreferenceManager.getDefaultSharedPreferences(this.context).edit()
        sharedPrefsEditor.putBoolean("termsOfService", value)
        sharedPrefsEditor.apply()
    }

    private fun resetSettings() {
        val sharedPrefsEditor = PreferenceManager.getDefaultSharedPreferences(this.context).edit()
        sharedPrefsEditor.putBoolean("useLocation", true)
        sharedPrefsEditor.putBoolean("allowNotifications", true)
        sharedPrefsEditor.putString("email", "")
        sharedPrefsEditor.putString("lightningDataFrequency", "5")
        sharedPrefsEditor.putBoolean("darkMode", false)
        sharedPrefsEditor.apply()

        val sharedPrefs: SharedPreferences = this.context!!.getSharedPreferences("setTime", Context.MODE_MULTI_PROCESS)
        val sharedPrefsEditor1 = sharedPrefs.edit()
        sharedPrefsEditor1.putString("fromTime", "")
        sharedPrefsEditor1.putString("toTime", "")
        sharedPrefsEditor1.apply()
        refreshFragment()
        darkMode()
        reStart()
    }


    private fun binaryAlertDialogCreator(title : String, message : String, posBtn : String, negBtn : String, pos : () -> Unit, neg : () -> Unit) : AlertDialog{
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
