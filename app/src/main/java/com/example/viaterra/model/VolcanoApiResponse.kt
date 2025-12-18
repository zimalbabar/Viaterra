package com.example.viaterra.model


data class VolcanoApiResponse(
    val volcanoes: List<VolcanoData>?
)

data class VolcanoData(
    val volcanoNumber: Int?,
    val volcanoName: String?,
    val country: String?,
    val lat: Double?,
    val lon: Double?,
    val elev: Double?,
    val volcanoType: String?,
    val alertLevel: String?
)
