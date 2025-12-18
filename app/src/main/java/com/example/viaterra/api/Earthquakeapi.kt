package com.example.viaterra.api

import com.example.viaterra.model.EarthquakeResponse
import retrofit2.Call
import retrofit2.http.GET

interface EarthquakeApi {

    @GET("earthquakes/feed/v1.0/summary/all_day.geojson")
    fun getEarthquakes(): Call<EarthquakeResponse>
}
