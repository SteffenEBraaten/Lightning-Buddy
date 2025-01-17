package com.example.in2000_project.maps

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.util.Log
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.net.InetAddress

class MapRepository{
    val metProxy = "https://in2000-apiproxy.ifi.uio.no/"
    val frostProxy = "https://in2000-frostproxy.ifi.uio.no/"


    public fun getMetLightningData() : String? {
        val httpClient = addLogging(false)

        val retrofit = Retrofit.Builder()
            .baseUrl(metProxy)
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(httpClient.build())
            .build()

        val metAPI = retrofit.create(MetLightningAPI::class.java)

        val call = metAPI.getData()

        val d = call.execute()
        return d.body()
    }

    public fun getMetLocationForecastData(lat: String, lon: String) : String? {
        val httpClient = addLogging(false)

        val retrofit = Retrofit.Builder()
            .baseUrl(metProxy)
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(httpClient.build())
            .build()

        val metAPI = retrofit.create(MetLocationForecastAPI::class.java)
        val call = metAPI.getData(lat, lon)

        val d = call.execute()
        return d.body()
    }

    @SuppressLint("SimpleDateFormat")
    public fun getFrostData(from: Date, to: Date) : String?{
        val httpClient = addLogging(false)

        val retrofit = Retrofit.Builder()
            .baseUrl(frostProxy)
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(httpClient.build())
            .build()

        val frostAPI = retrofit.create(FrostAPI::class.java)
        val format = SimpleDateFormat("yyyy-MM-dd")
        val fromTime = format.format(from)
        val toTime = format.format(to)
        val call = frostAPI.getData("$fromTime/$toTime")

        if (!haveInternet()){
            throw NetworkErrorException(0, "No internet")
        }

        val d = call.execute()
        Log.e("Frost call success:", "${d.isSuccessful}")
        if (!d.isSuccessful){
            val errorBody: String? = d.errorBody()?.string()
            Log.e("Error body:", errorBody)

            if (errorBody != null){
                try {
                    val errorJson = JSONObject(errorBody)
                    if (errorJson.has("error")){
                        val errorField = errorJson.getJSONObject("error")
                        throw NetworkErrorException(errorField.getInt("code"), errorField.getString("reason"))
                    }
                }catch (e: org.json.JSONException){
                    throw NetworkErrorException(-1, errorBody)
                }
            }
            else{
                throw NetworkErrorException(-2, "Unknown error")
            }
        }
        return d.body()
    }

    fun haveInternet(): Boolean {
        try {
            val ipAddr: InetAddress = InetAddress.getByName("google.com")
            return !ipAddr.equals("")

        }
        catch (e: Exception) {
            return false
        }
    }

    private fun addLogging(includeBody : Boolean) : OkHttpClient.Builder {
        val httpClient = OkHttpClient.Builder()

        val loggingH = HttpLoggingInterceptor()
        loggingH.level = HttpLoggingInterceptor.Level.HEADERS
        httpClient.addInterceptor(loggingH)

        if(includeBody){
            val loggingB = HttpLoggingInterceptor()
            loggingB.level = HttpLoggingInterceptor.Level.BODY
            httpClient.addInterceptor(loggingB)
        }

        return httpClient
    }
}

interface FrostAPI{
    @Headers("User-agent: Gruppe01")
    @GET("lightning/v0.ualf")
    fun getData(@Query("referencetime") interval: String): Call<String>
}

interface MetLightningAPI{
    @Headers("User-agent: Gruppe01")
    @GET("weatherapi/lightning/1.0/")
    fun getData(): Call<String>
}

interface MetLocationForecastAPI{
    @Headers("User-agent: Gruppe01")
    @GET("weatherapi/locationforecast/1.9/")
    fun getData(@Query("lat") lat: String, @Query("lon") lon: String): Call<String>
}


class NullOnEmptyConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type?,
        annotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<ResponseBody, *>? {
        val delegate = retrofit!!.nextResponseBodyConverter<String>(this, type!!, annotations!!)
        return Converter<ResponseBody, Any> { body -> if (body.contentLength() == 0L) null else delegate.convert(body) }
    }
}

class NetworkErrorException(errorCode:Int, reason:String?): Exception(reason){
    val errorCode = errorCode
    val reason = reason
}