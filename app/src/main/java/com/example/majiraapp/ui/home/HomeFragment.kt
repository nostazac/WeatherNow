package com.example.majiraapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.majiraapp.R
import com.example.majiraapp.ui.database.WeatherDao
import com.example.majiraapp.ui.database.WeatherDatabase
import com.example.majiraapp.ui.database.WeatherEntity
import com.example.majiraapp.ui.shared.SharedViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var cityNameTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var chanceOfRainTextView: TextView
    private lateinit var time9amTextView: TextView
    private lateinit var temp9amTextView: TextView
    private lateinit var time12pmTextView: TextView
    private lateinit var temp12pmTextView: TextView
    private lateinit var time3pmTextView: TextView
    private lateinit var temp3pmTextView: TextView
    private lateinit var weatherDatabase: WeatherDatabase
    private lateinit var weatherDao: WeatherDao

    private val weatherUpdateJob = Job()
    private val weatherUpdateScope = CoroutineScope(Dispatchers.Main + weatherUpdateJob)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize the database
        weatherDatabase = WeatherDatabase.getDatabase(requireContext())
        weatherDao = weatherDatabase.weatherDao()

        cityNameTextView = view.findViewById(R.id.cityName)
        temperatureTextView = view.findViewById(R.id.temperature)
        chanceOfRainTextView = view.findViewById(R.id.chanceOfRain)
        time9amTextView = view.findViewById(R.id.time9am)
        temp9amTextView = view.findViewById(R.id.temp9am)
        time12pmTextView = view.findViewById(R.id.time12pm)
        temp12pmTextView = view.findViewById(R.id.temp12pm)
        time3pmTextView = view.findViewById(R.id.time3pm)
        temp3pmTextView = view.findViewById(R.id.temp3pm)

        if (hasLocationPermission()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }

        startWeatherUpdates()

        // Load weather data if available
        lifecycleScope.launch {
            val savedWeather = weatherDao.getWeatherData()
            savedWeather?.let {
                updateWeatherUI(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        weatherUpdateJob.cancel()
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private val locationPermissionRequest = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Location permission is required to show weather data.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val latitude = it.latitude
                            val longitude = it.longitude
                            sharedViewModel.setLocation(latitude, longitude)
                            updateCityName(latitude, longitude)
                            updateWeatherForLocation(latitude, longitude)
                        } ?: run {
                            Toast.makeText(
                                requireContext(),
                                "Unable to get location. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error getting location: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: SecurityException) {
                Toast.makeText(
                    requireContext(),
                    "Location permission is required. Please grant the permission in settings.",
                    Toast.LENGTH_LONG
                ).show()
                requestLocationPermission()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun updateCityName(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val cityName = addresses?.getOrNull(0)?.locality ?: "Unknown Location"
            cityNameTextView.text = cityName
            sharedViewModel.setCityName(cityName)
        } catch (e: IOException) {
            // Handle the case when offline or Geocoder is unavailable
            val lastKnownCity = sharedViewModel.cityName.value ?: "Unknown Location"
            cityNameTextView.text = lastKnownCity
            sharedViewModel.setCityName(lastKnownCity)
            Toast.makeText(requireContext(), "Unable to get city name. Using last known location.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWeatherForLocation(latitude: Double, longitude: Double) {
        val apiKey = "f9e40eeb2ac6b5e12c0b750cace611fd"
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&appid=$apiKey&units=metric"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to fetch weather data. Using cached data if available.", Toast.LENGTH_SHORT).show()
                    loadCachedWeatherData()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonData ->
                    try {
                        val json = JSONObject(jsonData)
                        val list = json.getJSONArray("list")

                        val currentTemp = list.getJSONObject(0).getJSONObject("main").getDouble("temp").toString() + "°"
                        val rainProbability = (list.getJSONObject(0).getDouble("pop") * 100).toInt()

                        val forecast9am = getForecastForTime(list, 9)
                        val forecast12pm = getForecastForTime(list, 12)
                        val forecast3pm = getForecastForTime(list, 15)

                        val weatherEntity = WeatherEntity(
                            id = 1,  // Assuming id 1 for simplicity
                            cityName = cityNameTextView.text.toString(),
                            currentTemp = currentTemp,
                            rainProbability = rainProbability,
                            forecast9am = forecast9am,
                            forecast12pm = forecast12pm,
                            forecast3pm = forecast3pm
                        )

                        lifecycleScope.launch {
                            weatherDao.insertWeatherData(weatherEntity)
                        }

                        requireActivity().runOnUiThread {
                            temperatureTextView.text = currentTemp
                            chanceOfRainTextView.text = "Chance of rain: $rainProbability%"

                            time9amTextView.text = "9:00 AM"
                            temp9amTextView.text = forecast9am

                            time12pmTextView.text = "12:00 PM"
                            temp12pmTextView.text = forecast12pm

                            time3pmTextView.text = "3:00 PM"
                            temp3pmTextView.text = forecast3pm

                            sharedViewModel.setTemperature(currentTemp)
                            sharedViewModel.setRainProbability(rainProbability)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Error parsing weather data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun loadCachedWeatherData() {
        lifecycleScope.launch {
            val cachedWeather = weatherDao.getWeatherData()
            cachedWeather?.let {
                updateWeatherUI(it)
            } ?: run {
                Toast.makeText(requireContext(), "No cached weather data available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getForecastForTime(list: JSONArray, hour: Int): String {
        for (i in 0 until list.length()) {
            val forecast = list.getJSONObject(i)
            val forecastTime = forecast.getString("dt_txt")
            if (forecastTime.contains("${hour.toString().padStart(2, '0')}:00:00")) {
                return forecast.getJSONObject("main").getDouble("temp").toString() + "°"
            }
        }
        return "N/A"
    }

    private fun startWeatherUpdates() {
        weatherUpdateScope.launch {
            while (isActive) {
                getCurrentLocation()
                delay(30 * 60 * 1000) // Update every 30 minutes
            }
        }
    }

    private fun updateWeatherUI(weather: WeatherEntity) {
        sharedViewModel.setCityName(weather.cityName)
        sharedViewModel.setTemperature(weather.currentTemp)
        sharedViewModel.setRainProbability(weather.rainProbability)
        sharedViewModel.setForecast9am(weather.forecast9am)
        sharedViewModel.setForecast12pm(weather.forecast12pm)
        sharedViewModel.setForecast3pm(weather.forecast3pm)
    }

}