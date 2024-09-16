package com.example.majiraapp.ui.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherService {
    suspend fun getWeatherForCity(cityName: String): Weather = withContext(Dispatchers.IO) {
        // Simulate network delay
        Thread.sleep(1000)
        // Return mock data
        Weather(cityName, 25.0, 1013.0, 5.0, 0.0)
    }
}

data class Weather(
    val cityName: String,
    val temperature: Double,
    val pressure: Double,
    val windSpeed: Double,
    val precipitation: Double
)
