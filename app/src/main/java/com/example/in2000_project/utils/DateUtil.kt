package com.example.in2000_project.utils

import java.util.*

public abstract class DateUtil{
    companion object {
        fun createDate(year : Int, month : Int, day : Int, hour : Int, minute : Int, second : Int) : Date{
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, second)
            return cal.time
        }
    }
}