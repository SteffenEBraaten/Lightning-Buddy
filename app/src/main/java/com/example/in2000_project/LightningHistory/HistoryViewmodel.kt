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
import com.example.in2000_project.maps.NetworkErrorException
import com.example.in2000_project.utils.UalfUtil
import kotlinx.coroutines.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.*
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.Intent
import java.net.SocketTimeoutException


class HistoryViewmodel : ViewModel(){

    companion object Data {
        val recentData = MutableLiveData<ArrayList<UalfUtil.Ualf>>() //observe this and update UI on change
    }

    fun handleSearh(context: Context, from: Date, to: Date, act: LightningHistoryActivity, mapFrag: MapWithoutSearchbar?){

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val data = MapRepository().getFrostData(from, to)
                    withContext(Dispatchers.Main) {
                        if (!data.isNullOrEmpty()){
                            val ualfs = UalfUtil.createUalfs(data)
                            if (!ualfs.isNullOrEmpty()) {
                                act.dispayToast(context, act.getString(R.string.generateLightning), Toast.LENGTH_SHORT)
    //                            Log.e("MAP FRAG", "${mapFrag?.toString()}")
    //                            mapFrag?.plotLightning(ualfs)
                                HistoryViewmodel.recentData.postValue(ualfs)
                                act.dispayToast(context,act.getString(R.string.done), Toast.LENGTH_SHORT)
                            }
                        }
                        else{
                            act.dispayToast(context, act.getString(R.string.noDataForPeriod), Toast.LENGTH_LONG)
                        }
                    }
                }

            catch (e: NetworkErrorException){
                withContext(Dispatchers.Main) {
                    act.dispayToast(context,"Error: ${e.errorCode} ${e.reason}", Toast.LENGTH_LONG)
                    if (e.errorCode != 0){
                        act.inflateDialog(context, from, to)

                    }
                    Log.e("getHistoricalData", "failed to get historical data: $e")
                }
            }
            catch(e: SocketTimeoutException){
                withContext(Dispatchers.Main) {
                    act.dispayToast(context,"Error: Request timed out, try another date", Toast.LENGTH_LONG)
                    Log.e("getHistoricalData", "failed to get historical data: $e")
                }
            }
        }
    }
}
