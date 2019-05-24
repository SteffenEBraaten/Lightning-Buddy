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


class UalfUtilTest {
    @Test
    fun testUalfUtilSingle(){
        val ualf11 = UalfUtil.Ualf(0,
            DateUtil.createDate(2019, 5-1, 20, 14, 31, 32),
            55.5735, 8.4569, -14, 7.2, 4.9)
        val ualfString1 = "0 2019 05 20 14 31 32 491014656 55.5735 8.4569 -14 0 7 10 77.80 0.40 0.40 0.39 7.2 4.9 -0.0 1 1 0 1"
        val ualf12 = UalfUtil.createUalfs(ualfString1)!![0]
        assertTrue("Ualfs not same:\n$ualf11 \n!=\n$ualf12)", ualf11.toString() == ualf12.toString())
    }

    @Test
    fun testUalfUtilMultiple(){
        val ualf1 = UalfUtil.Ualf(0,
            DateUtil.createDate(2019, 5-1, 20, 14, 31, 32),
            55.5735, 8.4569, -14, 7.2, 4.9)
        val ualf2 = UalfUtil.Ualf(1,
            DateUtil.createDate(2019, 5-1, 20, 14, 31, 32),
            55.5729, 8.4754, -12, 0.0, 0.0)
        val ualfList1 = ArrayList<UalfUtil.Ualf>()
        ualfList1.add(ualf1)
        ualfList1.add(ualf2)

        val ualfString1 = "0 2019 05 20 14 31 32 491014656 55.5735 8.4569 -14 0 7 10 77.80 0.40 0.40 0.39 7.2 4.9 -0.0 1 1 0 1"
        val ualfString2 = "$ualfString1\n0 2019 05 20 14 31 32 491082240 55.5729 8.4754 -12 0 5 7 98.21 0.51 0.40 0.44 0.0 0.0 -0.0 1 1 0 1"
        val ualfList2 = UalfUtil.createUalfs(ualfString2)

        assertTrue("Ualflists not same:\n$ualfList1)\n!=\n$ualfList2", ualfList1.toString() == ualfList2.toString())
    }
}
