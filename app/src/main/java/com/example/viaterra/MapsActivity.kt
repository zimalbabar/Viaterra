package com.example.viaterra

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.literal
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MapsActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private val activeLayers = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps_activity)

        mapView = findViewById(R.id.mapView)

        mapView.getMapboxMap().loadStyleUri(Style.OUTDOORS) {
            setupLayerToggles()
            Toast.makeText(this, "Map loaded! Click chips to add layers", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLayerToggles() {
        findViewById<Chip>(R.id.chipEarthquakes).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) loadEarthquakes() else removeLayer("earthquakes")
        }

        findViewById<Chip>(R.id.chipVolcanoes).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) loadVolcanoes() else removeLayer("volcanoes")
        }

        findViewById<Chip>(R.id.chipTectonicPlates).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) loadTectonicPlates() else removeLayer("tectonic-plates")
        }

        findViewById<Chip>(R.id.chipRivers).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) loadRivers() else removeLayer("rivers")
        }
    }

    private fun loadEarthquakes() {
        lifecycleScope.launch {
            try {
                val features = withContext(Dispatchers.IO) {
                    val url = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.geojson"
                    val json = URL(url).readText()
                    val jsonObj = JSONObject(json)
                    val featuresArray = jsonObj.getJSONArray("features")

                    val list = mutableListOf<Feature>()
                    for (i in 0 until featuresArray.length()) {
                        val feature = featuresArray.getJSONObject(i)
                        val geometry = feature.getJSONObject("geometry")
                        val coords = geometry.getJSONArray("coordinates")
                        val properties = feature.getJSONObject("properties")

                        val mag = properties.optDouble("mag", 0.0)
                        val place = properties.optString("place", "Unknown")

                        list.add(Feature.fromGeometry(
                            Point.fromLngLat(coords.getDouble(0), coords.getDouble(1))
                        ).apply {
                            addNumberProperty("magnitude", mag)
                            addStringProperty("place", place)
                        })
                    }
                    list
                }

                addEarthquakeLayer(features)
                activeLayers.add("earthquakes")

                // Zoom out to see all earthquakes
                mapView.getMapboxMap().setCamera(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(Point.fromLngLat(140.0, 35.0)) // Japan area - lots of earthquakes
                        .zoom(4.0)
                        .build()
                )

                Toast.makeText(this@MapsActivity, "✅ ${features.size} earthquakes loaded - Zoomed to Pacific Ring of Fire", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MapsActivity, "❌ Error loading earthquakes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addEarthquakeLayer(features: List<Feature>) {
        mapView.getMapboxMap().getStyle { style ->
            val sourceId = "earthquakes-source"
            val layerId = "earthquakes-layer"

            style.addSource(geoJsonSource(sourceId) {
                featureCollection(FeatureCollection.fromFeatures(features))
            })

            style.addLayer(circleLayer(layerId, sourceId) {
                circleRadius(
                    interpolate(
                        literal("linear"),
                        get("magnitude"),
                        literal(2.5), literal(4.0),
                        literal(5.0), literal(8.0),
                        literal(7.0), literal(16.0)
                    )
                )
                circleColor("#FF5722")
                circleOpacity(0.7)
                circleStrokeWidth(1.0)
                circleStrokeColor("#FFFFFF")
            })
        }
    }

    private fun loadVolcanoes() {
        lifecycleScope.launch {
            try {
                val features = withContext(Dispatchers.IO) {
                    val url = "https://eonet.gsfc.nasa.gov/api/v3/events?category=volcanoes&status=open&limit=50"
                    val json = URL(url).readText()
                    val jsonObj = JSONObject(json)
                    val events = jsonObj.getJSONArray("events")

                    val list = mutableListOf<Feature>()
                    for (i in 0 until events.length()) {
                        val event = events.getJSONObject(i)
                        val title = event.optString("title", "Volcano")
                        val geometries = event.getJSONArray("geometry")

                        if (geometries.length() > 0) {
                            val geometry = geometries.getJSONObject(0)
                            val coords = geometry.getJSONArray("coordinates")

                            list.add(Feature.fromGeometry(
                                Point.fromLngLat(coords.getDouble(0), coords.getDouble(1))
                            ).apply {
                                addStringProperty("title", title)
                            })
                        }
                    }
                    list
                }

                addVolcanoLayer(features)
                activeLayers.add("volcanoes")

                // Zoom to see volcanoes globally
                mapView.getMapboxMap().setCamera(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(Point.fromLngLat(0.0, 20.0))
                        .zoom(2.0)
                        .build()
                )

                Toast.makeText(this@MapsActivity, "✅ ${features.size} active volcanoes loaded", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MapsActivity, "❌ Error loading volcanoes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addVolcanoLayer(features: List<Feature>) {
        mapView.getMapboxMap().getStyle { style ->
            val sourceId = "volcanoes-source"
            val layerId = "volcanoes-layer"

            style.addSource(geoJsonSource(sourceId) {
                featureCollection(FeatureCollection.fromFeatures(features))
            })

            style.addLayer(circleLayer(layerId, sourceId) {
                circleRadius(10.0)
                circleColor("#D32F2F")
                circleOpacity(0.8)
                circleStrokeWidth(2.0)
                circleStrokeColor("#FFEB3B")
            })
        }
    }

    private fun loadTectonicPlates() {
        lifecycleScope.launch {
            try {
                val features = withContext(Dispatchers.IO) {
                    val url = "https://raw.githubusercontent.com/fraxen/tectonicplates/master/GeoJSON/PB2002_boundaries.json"
                    val json = URL(url).readText()
                    val collection = FeatureCollection.fromJson(json)
                    collection.features() ?: emptyList()
                }

                addTectonicPlatesLayer(features)
                activeLayers.add("tectonic-plates")

                // Zoom out to see tectonic plates globally
                mapView.getMapboxMap().setCamera(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(Point.fromLngLat(0.0, 20.0))
                        .zoom(2.0)
                        .build()
                )

                Toast.makeText(this@MapsActivity, "✅ Tectonic plate boundaries loaded", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MapsActivity, "❌ Error loading tectonic plates: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addTectonicPlatesLayer(features: List<Feature>) {
        mapView.getMapboxMap().getStyle { style ->
            val sourceId = "tectonic-plates-source"
            val layerId = "tectonic-plates-layer"

            style.addSource(geoJsonSource(sourceId) {
                featureCollection(FeatureCollection.fromFeatures(features))
            })

            style.addLayer(lineLayer(layerId, sourceId) {
                lineColor("#FF9800")
                lineWidth(2.0)
                lineOpacity(0.8)
            })
        }
    }

    private fun loadRivers() {
        Toast.makeText(this, "Rivers layer (custom data needed)", Toast.LENGTH_SHORT).show()
        activeLayers.add("rivers")
    }

    private fun removeLayer(layerName: String) {
        mapView.getMapboxMap().getStyle { style ->
            val sourceId = "$layerName-source"
            val layerId = "$layerName-layer"

            try {
                style.removeStyleLayer(layerId)
                style.removeStyleSource(sourceId)
            } catch (e: Exception) {
                // Layer might not exist
            }
        }
        activeLayers.remove(layerName)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}