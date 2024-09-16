package com.example.majiraapp.ui

import android.content.Context
import android.content.SharedPreferences

class WeatherPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    fun getTemperatureUnit(): TemperatureUnit {
        val value = prefs.getString("temperature_unit", TemperatureUnit.CELSIUS.name)
        return TemperatureUnit.valueOf(value ?: TemperatureUnit.CELSIUS.name)
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        prefs.edit().putString("temperature_unit", unit.name).apply()
    }

    fun getLocationEnabled(): Boolean {
        return prefs.getBoolean("location_enabled", true)
    }

    fun setLocationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("location_enabled", enabled).apply()
    }

    fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean("notifications_enabled", true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun getThemeMode(): Boolean {
        return prefs.getBoolean("dark_mode", false) // False for light mode, true for dark mode
    }

    fun setThemeMode(darkMode: Boolean) {
        prefs.edit().putBoolean("dark_mode", darkMode).apply()
    }

    fun getAppRating(): Float {
        return prefs.getFloat("app_rating", 0f) // Default rating is 0
    }

    fun setAppRating(rating: Float) {
        prefs.edit().putFloat("app_rating", rating).apply()
    }
}
