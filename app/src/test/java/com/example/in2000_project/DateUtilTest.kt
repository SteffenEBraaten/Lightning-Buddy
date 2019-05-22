package com.example.in2000_project

import android.util.Log
import com.example.in2000_project.utils.DateUtil
import com.example.in2000_project.utils.UalfUtil
import com.example.in2000_project.utils.WeatherDataUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DateUtilTest {
    @Test
    fun testDateUtil(){
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2019)
        cal.set(Calendar.MONTH, 4)
        cal.set(Calendar.DAY_OF_MONTH, 19)
        cal.set(Calendar.HOUR_OF_DAY, 1)
        cal.set(Calendar.MINUTE, 3)
        cal.set(Calendar.SECOND, 2)
        val date1 = cal.time
        val date2 = DateUtil.createDate(2019, 4, 19, 1, 3, 2)
        assertTrue("Dates not same\n$date1\n!=\n$date2", date1.toString() == date2.toString())
    }
}
