package com.example.in2000_project.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.PowerManager
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.example.in2000_project.maps.MapsViewmodel
import java.text.SimpleDateFormat
import java.util.*

class Alarm : BroadcastReceiver() {
    private var frequency: Int = 0

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences("setTime", Context.MODE_MULTI_PROCESS)
        val fromTime = sharedPrefs.getString("fromTime", "")
        val toTime = sharedPrefs.getString("toTime", "")
        val temp = SimpleDateFormat("HH : mm")
        val currentTime = temp.format(Date())

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Partial wake lock: get api data")
        wl.acquire(60*1000L /*10 minutes*/)
        MapsViewmodel(PreferenceManager.getDefaultSharedPreferences(context)).getRecentApiData()

        if(fromTime != "" && toTime != ""){
            if(fromTime < toTime){
                if(currentTime > toTime || currentTime < fromTime){
                    Toast.makeText(context, "Alarm !!!!!!!!!!" , Toast.LENGTH_LONG).show() // For example
                    wl.release()
                }
            }
            else if (fromTime > toTime){
                if(currentTime > toTime && currentTime < fromTime){
                    Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show() // For example
                    wl.release()
                }
            }
        }
        else  return
    }


    fun setAlarm(context: Context, minutes: Int) {

        val alarmUp = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, Alarm::class.java),
            PendingIntent.FLAG_NO_CREATE
        ) != null

        if(alarmUp && minutes == this.frequency) return
        this.frequency = minutes

        if(alarmUp) cancelAlarm(context)
        if (minutes == 0) return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, Alarm::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, i, 0)
        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            (1000 * 60 * minutes).toLong(),
            pi
        )
    }

    private fun cancelAlarm(context: Context) {
        val intent = Intent(context, Alarm::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }
}