package com.example.majiraapp.ui.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class WeatherEntity(
    @PrimaryKey val id: Int,
    val cityName: String,
    val currentTemp: String,
    val rainProbability: Int,
    val forecast9am: String,
    val forecast12pm: String,
    val forecast3pm: String
)
