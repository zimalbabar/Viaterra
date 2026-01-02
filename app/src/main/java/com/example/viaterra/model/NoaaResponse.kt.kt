package com.example.viaterra.model



data class NoaaResponse(
    val features: List<TornadoFeature>
)

data class TornadoFeature(
    val properties: TornadoProperties
)

data class TornadoProperties(
    val event: String,
    val headline: String,
    val description: String,
    val severity: String,
    val areaDesc: String
)
