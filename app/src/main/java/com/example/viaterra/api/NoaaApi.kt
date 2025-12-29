package com.example.viaterra.api

import com.example.viaterra.model.NoaaResponse
import retrofit2.Call
import retrofit2.http.GET

interface NoaaApi {
    @GET("alerts/?event=Tornado Warning")

    fun getActiveAlerts(): Call<NoaaResponse>
}
