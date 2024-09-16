package com.example.majiraapp.ui.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.HttpException
import java.io.IOException
import android.util.Log

// Define the data classes based on the API response
data class WeatherResponse(
    val name: String,
    val main: Main,
    val timezone: Int
)

data class Main(
    val temp: Double
)

// Define the Weather API service interface
interface WeatherApiService {
    @GET("weather")
    suspend fun getWeatherForCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // For temperature in Celsius
    ): WeatherResponse

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

        fun create(): WeatherApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}

// Use this function to handle network errors and API responses
suspend fun fetchWeatherForCity(service: WeatherApiService, cityName: String, apiKey: String) {
    try {
        val response = service.getWeatherForCity(cityName, apiKey)
        // Handle the response data here, e.g., update UI or ViewModel
        Log.d("WeatherApiService", "Weather data: ${response.name}, ${response.main.temp}°C")
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        Log.e("WeatherApiService", "HTTP error: ${e.code()} ${e.message()}. Response body: $errorBody")
    } catch (e: IOException) {
        Log.e("WeatherApiService", "Network error: ${e.message}")
    } catch (e: Exception) {
        Log.e("WeatherApiService", "Unknown error: ${e.message}")
    }
}
