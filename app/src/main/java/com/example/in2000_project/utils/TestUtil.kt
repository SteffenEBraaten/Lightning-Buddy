package com.example.in2000_project.utils

import android.util.Log
import com.example.in2000_project.maps.MapRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

public class TestUtil{

    companion object {

        public fun testMetLightning(){
            GlobalScope.launch{
                handleMetLightningData(MapRepository().getMetLightningData())
            }
        }

        public fun handleMetLightningData(data : String?){
            if(data.isNullOrEmpty())return

            val ualfs = UalfUtil.createUalfs(data)
            if(ualfs.isNullOrEmpty()) return

            //update map and or save

        }

        public fun testFrost(){
            GlobalScope.launch{
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, 2017)
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val dateRepresentation = cal.time

                val cal2 = Calendar.getInstance()
                cal2.set(Calendar.YEAR, 2017)
                cal2.set(Calendar.MONTH, Calendar.FEBRUARY)
                cal2.set(Calendar.DAY_OF_MONTH, 1)
                val dateRepresentation2 = cal2.time

                handleFrostData(MapRepository().getFrostData(dateRepresentation, dateRepresentation2))
            }
        }

        public fun handleFrostData(data : String?){
            if(data.isNullOrEmpty())return

            val ualfs = UalfUtil.createUalfs(data)
            if(ualfs.isNullOrEmpty()) return
        }

    }
}
