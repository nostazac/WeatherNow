package com.example.majiraapp.ui.maps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.majiraapp.R
import com.example.majiraapp.databinding.FragmentMapBinding
import com.example.majiraapp.ui.cities.CitiesViewModel
import com.example.majiraapp.ui.shared.SharedViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import kotlin.math.max
import kotlin.math.min

private const val TAG = "MapFragment"

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val citiesViewModel: CitiesViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView: Inflating MapFragment layout")
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up MapFragment")

        setupMap()
        observeUserLocation()
        observeKenyanCities()
    }

    private fun setupMap() {
        Log.d(TAG, "setupMap: Initializing map")
        Configuration.getInstance().userAgentValue = requireActivity().packageName
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(5.0)
    }

    private fun observeUserLocation() {
        Log.d(TAG, "observeUserLocation: Observing user location changes")
        sharedViewModel.latitude.observe(viewLifecycleOwner) { latitude ->
            sharedViewModel.longitude.observe(viewLifecycleOwner) { longitude ->
                if (isValidLatLong(latitude, longitude)) {
                    Log.d(TAG, "User location updated: ($latitude, $longitude)")
                    val userLocation = GeoPoint(latitude, longitude)
                    binding.mapView.controller.animateTo(userLocation)

                    sharedViewModel.temperature.observe(viewLifecycleOwner) { temperature ->
                        Log.d(TAG, "Adding user location marker with temperature: $temperature")
                        addMarker(latitude, longitude, temperature, "Current Location", isUserLocation = true)
                    }
                } else {
                    Log.e(TAG, "Invalid user location: ($latitude, $longitude)")
                    Toast.makeText(context, "Invalid user location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeKenyanCities() {
        Log.d(TAG, "observeKenyanCities: Observing changes in Kenyan cities")
        citiesViewModel.kenyanCities.observe(viewLifecycleOwner) { cities ->
            Log.d(TAG, "Kenyan cities updated: ${cities.map { it.name }}")
            binding.mapView.overlays.clear()

            // Re-add user location marker
            sharedViewModel.latitude.value?.let { lat ->
                sharedViewModel.longitude.value?.let { lon ->
                    if (isValidLatLong(lat, lon)) {
                        sharedViewModel.temperature.value?.let { temp ->
                            Log.d(TAG, "Re-adding user location marker")
                            addMarker(lat, lon, temp, "Your Location", isUserLocation = true)
                        }
                    }
                }
            }

            // Commented out the code that adds markers for Kenyan cities
            /*
            //Add markers for Kenyan cities
            cities.forEach { city ->
                Log.d(TAG, "Adding city to map: ${city.name} at (${city.latitude}, ${city.longitude})")
                addMarker(city.latitude, city.longitude, city.temperature.toString(), city.name)
            }

            // Create bounding box
            val points = cities.map { GeoPoint(it.latitude, it.longitude) }
            val boundingBox = createSafeBoundingBox(points)
            Log.d(TAG, "Zooming to bounding box: $boundingBox")
            binding.mapView.zoomToBoundingBox(boundingBox, true, 100)
            */

            binding.mapView.invalidate()
        }
    }


    private fun isValidLatLong(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }

    private fun createSafeBoundingBox(points: List<GeoPoint>): BoundingBox {
        if (points.isEmpty()) {
            Log.w(TAG, "No points provided for bounding box, using default values")
            return BoundingBox(85.0, 180.0, -85.0, -180.0)
        }

        var north = points.maxOfOrNull { it.latitude } ?: 0.0
        var south = points.minOfOrNull { it.latitude } ?: 0.0
        var east = points.maxOfOrNull { it.longitude } ?: 0.0
        var west = points.minOfOrNull { it.longitude } ?: 0.0

        // Ensure the values are within the valid range
        north = north.coerceIn(-85.0, 85.0)
        south = south.coerceIn(-85.0, 85.0)
        east = east.coerceIn(-180.0, 180.0)
        west = west.coerceIn(-180.0, 180.0)

        // Ensure north is greater than south
        if (north <= south) {
            Log.w(TAG, "North is not greater than south, adjusting values")
            val temp = north
            north = (south + 0.1).coerceAtMost(85.0)
            south = (temp - 0.1).coerceAtLeast(-85.0)
        }

        // Ensure east is greater than west
        if (east <= west) {
            Log.w(TAG, "East is not greater than west, adjusting values")
            val temp = east
            east = (west + 0.1).coerceAtMost(180.0)
            west = (temp - 0.1).coerceAtLeast(-180.0)
        }

        Log.d(TAG, "Safe BoundingBox Values: North: $north, South: $south, East: $east, West: $west")

        return BoundingBox(north, east, south, west)
    }


    private fun addMarker(latitude: Double, longitude: Double, temperature: String, title: String, isUserLocation: Boolean = false) {
        Log.d(TAG, "Adding marker: $title at ($latitude, $longitude)")
        val marker = Marker(binding.mapView).apply {
            position = GeoPoint(latitude, longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
            snippet = "Temperature: $temperature°C"

            icon = if (isUserLocation) {
                resources.getDrawable(R.drawable.ic_user_location, null)
            } else {
                resources.getDrawable(R.drawable.ic_city_location, null)
            }

            infoWindow = CustomInfoWindow(binding.mapView)

            setOnMarkerClickListener { marker, _ ->
                Log.d(TAG, "Marker clicked: ${marker.title}")
                marker.showInfoWindow()
                true
            }
        }

        binding.mapView.overlays.add(marker)
        Log.d(TAG, "Marker added successfully: $title")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Resuming map")
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Pausing map")
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up MapFragment")
        binding.mapView.overlays.clear()
        _binding = null
    }
}

class CustomInfoWindow(mapView: MapView) : InfoWindow(R.layout.info_window, mapView) {
    override fun onOpen(item: Any?) {
        Log.d(TAG, "CustomInfoWindow: Opening info window")
        val marker = item as? Marker ?: return
        val titleView = mView.findViewById<TextView>(R.id.info_window_title)
        val temperatureView = mView.findViewById<TextView>(R.id.info_window_temperature)
        titleView.text = marker.title
        temperatureView.text = marker.snippet
        Log.d(TAG, "Info window opened for: ${marker.title}")
    }

    override fun onClose() {
        Log.d(TAG, "CustomInfoWindow: Closing info window")
        // Clear the content of the info window
        val titleView = mView.findViewById<TextView>(R.id.info_window_title)
        val temperatureView = mView.findViewById<TextView>(R.id.info_window_temperature)
        titleView.text = ""
        temperatureView.text = ""

        // Remove the info window from its parent
        (mView.parent as? ViewGroup)?.removeView(mView)
    }
}