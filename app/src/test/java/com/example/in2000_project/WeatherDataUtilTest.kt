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
class WeatherDataUtilTest {

    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    @Test
    fun testWeatherDataUtilSingle(){
        val timetag =
                "<time datatype=\"forecast\" from=\"2019-05-20T16:00:00Z\" to=\"2019-05-20T17:00:00Z\">\n" +
                    "<location altitude=\"485\" latitude=\"60.1000\" longitude=\"9.5800\">\n" +
                        "<precipitation unit=\"mm\" value=\"0.0\" minvalue=\"0.0\" maxvalue=\"0.1\"/>\n" +
                        "<symbol id=\"Cloud\" number=\"4\"/>\n" +
                    "</location>\n" +
                "</time>"
        val time1 = WeatherDataUtil.createWeatherData(timetag)[0]
        val time2 = WeatherDataUtil.WeatherData(
            formatter.parse("2019-05-20T16:00:00Z"),
            formatter.parse("2019-05-20T17:00:00Z"),
            "60.1000".toDouble(), "9.5800".toDouble(),
            WeatherDataUtil.Symbol("Cloud", "4".toInt()))

        assertTrue("Weatherdata not same\n$time1\n!=\n$time2", time1.toString() == time2.toString())
    }

    @Test
    fun testWeatherDataUtilMultiple(){

        val weatherData =
            "<weatherdata xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://api.met.no/weatherapi/locationforecast/1.9/schema\" created=\"2019-05-20T16:47:03Z\">\n" +
                    "<meta>\n" +
                    "<model name=\"LOCAL\" termin=\"2019-05-20T12:00:00Z\" runended=\"2019-05-20T14:33:46Z\" nextrun=\"2019-05-20T22:00:00Z\" from=\"2019-05-20T17:00:00Z\" to=\"2019-05-23T06:00:00Z\"/>\n" +
                    "<model name=\"EPS\" termin=\"2019-05-20T00:00:00Z\" runended=\"2019-05-20T09:03:02Z\" nextrun=\"2019-05-20T22:00:00Z\" from=\"2019-05-23T12:00:00Z\" to=\"2019-05-29T18:00:00Z\"/>\n" +
                    "</meta>\n" +
                    "<product class=\"pointData\">\n" +
                    "<time datatype=\"forecast\" from=\"2019-05-20T17:00:00Z\" to=\"2019-05-20T17:00:00Z\">\n" +
                    "<location altitude=\"485\" latitude=\"60.1000\" longitude=\"9.5800\">\n" +
                    "<temperature id=\"TTT\" unit=\"celsius\" value=\"11.4\"/>\n" +
                    "<windDirection id=\"dd\" deg=\"6.1\" name=\"N\"/>\n" +
                    "<windSpeed id=\"ff\" mps=\"1.8\" beaufort=\"2\" name=\"Svak vind\"/>\n" +
                    "<windGust id=\"ff_gust\" mps=\"4.1\"/>\n" +
                    "<areaMaxWindSpeed mps=\"5.1\"/>\n" +
                    "<humidity value=\"94.5\" unit=\"percent\"/>\n" +
                    "<pressure id=\"pr\" unit=\"hPa\" value=\"1011.4\"/>\n" +
                    "<cloudiness id=\"NN\" percent=\"94.8\"/>\n" +
                    "<fog id=\"FOG\" percent=\"0.3\"/>\n" +
                    "<lowClouds id=\"LOW\" percent=\"70.5\"/>\n" +
                    "<mediumClouds id=\"MEDIUM\" percent=\"79.7\"/>\n" +
                    "<highClouds id=\"HIGH\" percent=\"26.5\"/>\n" +
                    "<dewpointTemperature id=\"TD\" unit=\"celsius\" value=\"10.6\"/>\n" +
                    "</location>\n" +
                    "</time>\n" +
                    "<time datatype=\"forecast\" from=\"2019-05-20T16:00:00Z\" to=\"2019-05-20T17:00:00Z\">\n" +
                    "<location altitude=\"485\" latitude=\"60.1000\" longitude=\"9.5800\">\n" +
                    "<precipitation unit=\"mm\" value=\"0.0\" minvalue=\"0.0\" maxvalue=\"0.1\"/>\n" +
                    "<symbol id=\"Cloud\" number=\"4\"/>\n" +
                    "</location>\n" +
                    "</time>\n" +
                    "<time datatype=\"forecast\" from=\"2019-05-20T14:00:00Z\" to=\"2019-05-20T17:00:00Z\">\n" +
                    "<location altitude=\"485\" latitude=\"60.1000\" longitude=\"9.5800\">\n" +
                    "<precipitation unit=\"mm\" value=\"0.6\" minvalue=\"0.4\" maxvalue=\"1.0\"/>\n" +
                    "<symbol id=\"LightRain\" number=\"9\"/>\n" +
                    "</location>\n" +
                    "</time>\n" +
                    "</product>\n" +
                    "</weatherdata>"

        val weatherDataList1 = WeatherDataUtil.createWeatherData(weatherData)

        val time1 = WeatherDataUtil.WeatherData(
            formatter.parse("2019-05-20T16:00:00Z"),
            formatter.parse("2019-05-20T17:00:00Z"),
            "60.1000".toDouble(), "9.5800".toDouble(),
            WeatherDataUtil.Symbol("Cloud", "4".toInt()))


        val time2 = WeatherDataUtil.WeatherData(
            formatter.parse("2019-05-20T14:00:00Z"),
            formatter.parse("2019-05-20T17:00:00Z"),
            "60.1000".toDouble(), "9.5800".toDouble(),
            WeatherDataUtil.Symbol("LightRain", "9".toInt()))

        val weatherDataList2 = ArrayList<WeatherDataUtil.WeatherData>()
        weatherDataList2.add(time1)
        weatherDataList2.add(time2)

        assertTrue("WeatherDataList not same\n$weatherDataList1\n!=\n$weatherDataList2", weatherDataList1.toString() == weatherDataList2.toString())
    }
}
