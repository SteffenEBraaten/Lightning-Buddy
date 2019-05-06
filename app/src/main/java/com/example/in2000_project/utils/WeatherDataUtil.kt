package com.example.in2000_project.utils

import android.annotation.SuppressLint
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
public abstract class WeatherDataUtil{

    data class WeatherData(
        val from: Date,
        val to: Date,
        var latitude: Double?,
        var longitude: Double?,
        var symbol: Symbol?
    ){
        constructor(from: Date, to: Date): this(from, to, null, null, null)
    }

    data class Symbol(
        val id: String,
        val number: Int
    )

    companion object {
        public fun createWeatherData(data: String?): ArrayList<WeatherData>{
            val weatherData = ArrayList<WeatherData>()
            if(data.isNullOrEmpty()) return weatherData

            val factory = XmlPullParserFactory.newInstance() as XmlPullParserFactory
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser() as XmlPullParser

            xpp.setInput(StringReader(data))

            var eventType = xpp.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_DOCUMENT -> { }
                    XmlPullParser.START_TAG -> {
                        try {
                            handleStartTag(xpp, weatherData)
                        }catch (e: Exception){
                            Log.e("XMLPULLPARSER"," $e")
                        }
                    }
                    XmlPullParser.END_TAG -> { }
                    XmlPullParser.TEXT -> { }
                }

                eventType = xpp.next()
            }

            removeIrrelevant(weatherData)
            return weatherData
        }

        @SuppressLint("SimpleDateFormat")
        private fun handleStartTag(xpp: XmlPullParser, weatherData: ArrayList<WeatherData>) {
            when(xpp.name){
                "time" -> {
                    val f = xpp.getAttributeValue(null, "from")
                    val t = xpp.getAttributeValue(null, "to")

                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

                    val from = formatter.parse(f)
                    val to = formatter.parse(t)

                    removeIrrelevant(weatherData)
                    weatherData.add(WeatherData(from, to))
                }

                "location" -> {
                    val data = weatherData.last()
                    if(data.latitude == null && data.longitude == null){
                        val latitude = xpp.getAttributeValue(null, "latitude")
                        data.latitude = latitude.toDouble()

                        val longitude = xpp.getAttributeValue(null, "longitude")
                        data.longitude = longitude.toDouble()
                    }
                }

                "symbol" -> {
                    val data = weatherData.last()
                    if(data.symbol == null){
                        val symbolId = xpp.getAttributeValue(null, "id")
                        val symbolNumber = xpp.getAttributeValue(null, "number")
                        data.symbol = Symbol(symbolId, symbolNumber.toInt())
                    }
                }
            }
        }

        private fun removeIrrelevant(weatherData: ArrayList<WeatherData>) {
            if(weatherData.isNotEmpty()){
                if(weatherData.last().symbol == null){
                    weatherData.removeAt(weatherData.lastIndex)
                }
            }
        }
    }



}