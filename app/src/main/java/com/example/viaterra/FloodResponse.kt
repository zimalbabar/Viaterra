package com.example.viaterra


data class FloodResponse(
    val features: List<FloodFeature>
)

data class FloodFeature(
    val properties: FloodProperties
)

data class FloodProperties(
    val event: String,
    val headline: String,
    val description: String,
    val severity: String,
    val areaDesc: String
)
