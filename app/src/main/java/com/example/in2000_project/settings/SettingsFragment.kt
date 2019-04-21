package com.example.in2000_project.settings

import android.util.Patterns
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.example.in2000_project.maps.MapRepository
import com.example.in2000_project.R
import com.example.in2000_project.utils.TestUtil
import com.example.in2000_project.utils.UalfUtil
import java.util.*
import kotlinx.coroutines.*




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
        val language = sharedPrefs.getString("language", "English")
        preferenceScreen.findPreference("language").summary = language
    }

    private fun attachEvents(){
        val termsOfService = findPreference("termsOfService")
        termsOfService?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val alert = binaryAlertDialogCreator(
                "Terms Of Service",
                getString(R.string.termsOfService),
                "I agree",
                "I disagree",
                ::acceptTermsAgreement,
                ::declineTermsAgreement)

            alert.show()
            true
        }

        val reset = findPreference("resetSettings")
        reset?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val alert = binaryAlertDialogCreator(
                "Reset settings",
                 getString(R.string.resetSettingsMessage),
                "Reset",
                "Cancel",
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
                Toast.makeText(activity, "Invalid email", Toast.LENGTH_SHORT).show()
            }
            true
        }

        preferenceScreen.findPreference("language").setOnPreferenceChangeListener { _, _ ->
            refreshFragment()
            true
        }
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
//        val sharedPrefsEditor = PreferenceManager.getDefaultSharedPreferences(this.context).edit()
//        sharedPrefsEditor.putBoolean("useLocation", false)
//        sharedPrefsEditor.putString("language", "English")
//        sharedPrefsEditor.putBoolean("allowNotifications", true)
//        sharedPrefsEditor.putString("email", "")
//        sharedPrefsEditor.putBoolean("vibrate", true)
//        sharedPrefsEditor.apply()
//        refreshFragment()

        TestUtil.testMetLightning()
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
