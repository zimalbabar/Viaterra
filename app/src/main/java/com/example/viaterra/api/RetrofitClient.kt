package com.example.viaterra.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://earthquake.usgs.gov/"

    val api: EarthquakeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EarthquakeApi::class.java)
    }

    private const val TORNADO_BASE_URL = "https://api.weather.gov/"

    val Tornadoapi: NoaaApi by lazy {
        Retrofit.Builder()
            .baseUrl(TORNADO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoaaApi::class.java)
    }


    private const val FLOOD_BASE_URL = "https://api.weather.gov/"

    val floodapi: FloodApi by lazy {
        Retrofit.Builder()
            .baseUrl(FLOOD_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FloodApi::class.java)
    }





}