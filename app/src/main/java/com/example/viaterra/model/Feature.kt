package com.example.viaterra.model

import com.example.viaterra.EarthquakeGeometry
import com.example.viaterra.model.Properties

data class Feature(
    val properties: Properties,
    val geometry: EarthquakeGeometry
)