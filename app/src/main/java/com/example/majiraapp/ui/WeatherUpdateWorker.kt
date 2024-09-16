package com.example.majiraapp.ui

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.majiraapp.ui.data.City
import com.example.majiraapp.ui.service.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val weatherApiService = WeatherApiService.create()
    private val apiKey = "f9e40eeb2ac6b5e12c0b750cace611fd"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Fetch the updated weather for each city from the ViewModel or SharedPreferences
            val cities = loadSelectedCities() // Retrieve saved cities

            cities.forEach { city ->
                try {
                    // Fetch updated weather info for each city
                    val response = weatherApiService.getWeatherForCity(city.name, apiKey)
                    val updatedCity = city.copy(
                        temperature = response.main.temp.toInt(),
                        time = getCurrentTimeForCity(response.timezone)
                    )
                    // Save the updated city info
                    saveCity(updatedCity)
                } catch (e: Exception) {
                    // Log error or handle failed request for this city
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun loadSelectedCities(): List<City> {
        // Load selected cities from database or shared preferences
        // Assuming this method is implemented
        return listOf()
    }

    private fun saveCity(city: City) {
        // Save the updated city data
    }

    private fun getCurrentTimeForCity(timezoneOffset: Int): String {
        // Logic to get local time for the city
        return "12:00"  // Example
    }
}
