package com.example.majiraapp.ui.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CitySearchApiService {
    @GET("geo/1.0/direct")
    suspend fun searchCities(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<CityResponse>

    companion object {
        fun create(): CitySearchApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(CitySearchApiService::class.java)
        }
    }
}

data class CityResponse(
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double
)
