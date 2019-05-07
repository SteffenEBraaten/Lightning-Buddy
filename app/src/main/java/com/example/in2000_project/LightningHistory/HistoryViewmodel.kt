package com.example.in2000_project.LightningHistory

import android.app.DatePickerDialog
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.in2000_project.R
import com.example.in2000_project.maps.MapRepository
import com.example.in2000_project.utils.UalfUtil
import kotlinx.coroutines.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.*


class HistoryViewmodel : ViewModel(){
    companion object Data {
        val recentData = MutableLiveData<ArrayList<UalfUtil.Ualf>>() //observe this and update UI on change
    }


    fun handleSearh(context: Context, from: Date, to: Date){
        Log.e("Test handle search", "Test test")
        CoroutineScope(Dispatchers.IO).launch {
            try {

            val data = MapRepository().getFrostData(from, to)
                withContext(Dispatchers.Main) {
                    if (!data.isNullOrEmpty()){
                        val ualfs = UalfUtil.createUalfs(data)
                        if (!ualfs.isNullOrEmpty()) {
                            for (ualf in ualfs){
                                Log.e("test", "test : $ualf")
                            }
                        }
                    }
                    else{
                        Toast.makeText(context, "There was no data for this place/period, please try again!", Toast.LENGTH_LONG).show()
                    }
                }
            }

            catch (e: Exception){
                Log.e("getHistoricalData", "failed to get historical data: $e")
                Toast.makeText(context, "An error has occured", Toast.LENGTH_LONG).show()
            }

        }
    }


}
