package com.example.viaterra.api

import com.example.viaterra.model.VolcanoData
import retrofit2.Call
import retrofit2.http.GET

interface VolcanoApi {
    @GET("hans-public/api/volcano/getMonitoredVolcanoes")
    fun getMonitoredVolcanoes(): Call<List<VolcanoData>>
}