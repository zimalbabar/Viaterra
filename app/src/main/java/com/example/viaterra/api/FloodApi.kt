package com.example.viaterra.api

import com.example.viaterra.FloodResponse
import retrofit2.Call
import retrofit2.http.GET

interface FloodApi {
    @GET("alerts/active/?event=Flood Warning")


    fun getActiveAlerts(): Call<FloodResponse>
}
