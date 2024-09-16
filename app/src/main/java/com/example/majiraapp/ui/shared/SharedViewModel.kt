package com.example.majiraapp.ui.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.majiraapp.ui.TemperatureUnit

class SharedViewModel : ViewModel() {

    // Latitude and Longitude
    private val _latitude = MutableLiveData<Double>()
    val latitude: LiveData<Double> = _latitude

    private val _longitude = MutableLiveData<Double>()
    val longitude: LiveData<Double> = _longitude

    // Weather details
    private val _temperature = MutableLiveData<String>()
    val temperature: LiveData<String> = _temperature

    private val _cityName = MutableLiveData<String>()
    val cityName: LiveData<String> = _cityName

    private val _rainProbability = MutableLiveData<Int>()
    val rainProbability: LiveData<Int> = _rainProbability

    // Forecast for specific times of the day
    private val _forecast9am = MutableLiveData<String>()
    val forecast9am: LiveData<String> = _forecast9am

    private val _forecast12pm = MutableLiveData<String>()
    val forecast12pm: LiveData<String> = _forecast12pm

    private val _forecast3pm = MutableLiveData<String>()
    val forecast3pm: LiveData<String> = _forecast3pm

    // User preferences
    private val _location = MutableLiveData<String>()
    val location: LiveData<String> = _location

    private val _timeFormat = MutableLiveData<Boolean>()
    val timeFormat: LiveData<Boolean> = _timeFormat

    private val _weatherAwareness = MutableLiveData<Boolean>()
    val weatherAwareness: LiveData<Boolean> = _weatherAwareness

    private val _temperatureUnit = MutableLiveData<TemperatureUnit>()
    val temperatureUnit: LiveData<TemperatureUnit> = _temperatureUnit

    private val _locationEnabled = MutableLiveData<Boolean>()
    val locationEnabled: LiveData<Boolean> = _locationEnabled

    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled

    private val _themeMode = MutableLiveData<Boolean>()
    val themeMode: LiveData<Boolean> = _themeMode

    private val _appRating = MutableLiveData<Float>()
    val appRating: LiveData<Float> = _appRating

    // Function to set values and notify observers
    fun setLocation(lat: Double, lon: Double) {
        _latitude.value = lat
        _longitude.value = lon
    }

    fun setTemperature(temp: String) {
        _temperature.value = temp
    }

    fun setCityName(name: String) {
        _cityName.value = name
    }

    fun setRainProbability(probability: Int) {
        _rainProbability.value = probability
    }

    fun setForecast9am(temp: String) {
        _forecast9am.value = temp
    }

    fun setForecast12pm(temp: String) {
        _forecast12pm.value = temp
    }

    fun setForecast3pm(temp: String) {
        _forecast3pm.value = temp
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _temperatureUnit.value = unit
    }

    fun setWeatherAwareness(enabled: Boolean) {
        _weatherAwareness.value = enabled
    }

    fun setTimeFormat(is24Hour: Boolean) {
        _timeFormat.value = is24Hour
    }

    fun setLocationEnabled(enabled: Boolean) {
        _locationEnabled.value = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun setThemeMode(darkMode: Boolean) {
        _themeMode.value = darkMode
    }

    fun setAppRating(rating: Float) {
        _appRating.value = rating
    }
}
