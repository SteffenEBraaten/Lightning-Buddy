package com.example.in2000_project.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AutoStart : BroadcastReceiver() {
    var alarm = Alarm()

    override fun onReceive(context: Context, intent: Intent) { //TODO: get minutes from settings
        val serviceIntent = Intent(context, AlarmService::class.java)
        serviceIntent.putExtra("minutes", 5)
        context.startService(serviceIntent)
    }
}