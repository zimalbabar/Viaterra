package com.example.viaterra.model

import com.example.viaterra.EarthquakeGeometry

data class EarthquakeResponse(
    val features: List<Feature>
)

data class Feature(
    val properties: Properties,
    val geometry: EarthquakeGeometry
)

data class Properties(
    val mag: Double?,
    val place: String?,
    val time: Long?,
    val distance: String // e.g., "12.5 miles"
)
