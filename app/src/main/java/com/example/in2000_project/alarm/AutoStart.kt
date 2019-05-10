package com.example.in2000_project.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager.getDefaultSharedPreferences

class AutoStart : BroadcastReceiver() {
    var alarm = Alarm()

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, AlarmService::class.java)
        val minutes = getDefaultSharedPreferences(context).getString("lightningDataFrequency", "5")
        serviceIntent.putExtra("minutes", minutes)
        context.startService(serviceIntent)
    }
}