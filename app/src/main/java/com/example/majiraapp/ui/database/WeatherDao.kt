package com.example.majiraapp.ui.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weather: WeatherEntity)

    @Query("SELECT * FROM weather_table WHERE id = 1") // Assuming 1 for current weather
    suspend fun getWeatherData(): WeatherEntity?
}
