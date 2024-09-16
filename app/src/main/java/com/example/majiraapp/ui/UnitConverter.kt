package com.example.majiraapp.ui

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }
enum class WindSpeedUnit { KMH, MS, KNOTS }
enum class PressureUnit { HPA, INCHES, KPA, MM }
enum class PrecipitationUnit { MILLIMETERS, INCHES }
enum class DistanceUnit { KILOMETERS, MILES }

object UnitConverter {
    fun convertTemperature(value: Double, from: TemperatureUnit, to: TemperatureUnit): Double {
        return when {
            from == TemperatureUnit.CELSIUS && to == TemperatureUnit.FAHRENHEIT -> value * 9/5 + 32
            from == TemperatureUnit.FAHRENHEIT && to == TemperatureUnit.CELSIUS -> (value - 32) * 5/9
            else -> value
        }
    }

    // ... Add conversion methods for other units (wind speed, pressure, precipitation, distance)
}