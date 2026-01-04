package com.example.viaterra.model

data class GDACSAlert(
    val eventType: String,
    val severity: String,
    val alertlevel: String,
    val eventId: String,
    val eventName: String,
    val country: String,
    val fromDate: String,
    val toDate: String,
    val latitude: String,
    val longitude: String,
    val description: String
)
