package com.example.in2000_project.alarm

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class AlarmService : Service() {
    var alarm = Alarm()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            val extras = intent.extras as Bundle
            val minutes = extras.get("minutes") as Int?
            alarm.setAlarm(this, minutes ?: 5)
        }

        return Service.START_STICKY
    }

    @Suppress("OverridingDeprecatedMember")
    override fun onStart(intent: Intent, startId: Int) {
        val extras = intent.extras as Bundle
        val minutes = extras.get("minutes") as Int?
        alarm.setAlarm(this, minutes ?: 5)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}