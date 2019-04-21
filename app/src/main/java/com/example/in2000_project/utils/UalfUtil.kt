package com.example.in2000_project.utils

import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

public abstract class UalfUtil{

    public data class Ualf(val id : Int, val date : Date, val lat : Double, val long : Double)

    companion object {
        fun createUalfs(file : String) : ArrayList<Ualf>? {
            val fileLines = file.split("\n")
            if(fileLines.isEmpty()) return null

            var idCount = 0
            val ualfs = ArrayList<Ualf>()
            for (line in fileLines){
                if(line.isEmpty()) continue
                val lineData = line.split(" ")
                try {
                    val date = DateUtil.createDate(
                        lineData[1].toInt(), lineData[2].toInt()-1, lineData[3].toInt(),
                        lineData[4].toInt(), lineData[5].toInt(), lineData[6].toInt()
                    )
                    ualfs.add(Ualf(idCount++, date, lineData[8].toDouble(), lineData[9].toDouble()))
                }catch (e : Exception){
                    Log.e("Ualf util error", e.toString())
                }
            }

            return ualfs
        }
    }
}