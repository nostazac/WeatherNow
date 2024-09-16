package com.example.majiraapp.ui.cities

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.majiraapp.ui.data.City
import com.example.majiraapp.ui.service.CitySearchApiService
import com.example.majiraapp.ui.service.WeatherApiService
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "CitiesViewModel"

class CitiesViewModel(application: Application) : AndroidViewModel(application) {
    private val _cities = MutableLiveData<List<City>>()
    val cities: LiveData<List<City>> = _cities
    private val _selectedCities = MutableLiveData<MutableList<City>>()
    val selectedCities: LiveData<MutableList<City>> = _selectedCities
    private val sharedPreferences = application.getSharedPreferences("selected_cities_prefs", Context.MODE_PRIVATE)
    private val citySearchApiService = CitySearchApiService.create()
    private val weatherApiService = WeatherApiService.create()
    private val apiKey = "f9e40eeb2ac6b5e12c0b750cace611fd"
    private val _kenyanCities = MutableLiveData<List<City>>()
    val kenyanCities: LiveData<List<City>> = _kenyanCities


    private val kenyanCitiesList = listOf(
        City("Nairobi, Kenya", -1.2921, 36.8219),
        City("Mombasa, Kenya", -4.0435, 39.6682),
        City("Kisumu, Kenya", -0.1022, 34.7617),
        City("Nakuru, Kenya", -0.3031, 36.0800),
        City("Eldoret, Kenya", 0.5143, 35.2698)
    )

    init {
        Log.d(TAG, "Initializing CitiesViewModel")
        _selectedCities.value = loadSelectedCities()
        _kenyanCities.value = kenyanCitiesList
        fetchWeatherForKenyanCities()
        fetchWeatherForSelectedCities()
    }


    fun toggleCitySelection(city: City) {
        Log.d(TAG, "Toggling selection for city: ${city.name}")
        val currentSelected = _selectedCities.value.orEmpty().toMutableList()

        if (currentSelected.contains(city)) {
            currentSelected.remove(city)
            Log.d(TAG, "Removed city: ${city.name}")
        } else if (currentSelected.size < 5) {
            viewModelScope.launch {
                try {
                    val updatedCity = fetchWeatherForCity(city)
                    currentSelected.add(updatedCity)
                    Log.d(TAG, "Added city: ${updatedCity.name}, Temp: ${updatedCity.temperature}°C, Time: ${updatedCity.time}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding city: ${e.message}")
                }
            }
        } else {
            Log.d(TAG, "Cannot add more than 5 cities")
        }
        _selectedCities.value = currentSelected
        saveSelectedCities(currentSelected)
    }

    fun filterCities(query: String) {
        Log.d(TAG, "Filtering cities with query: $query")
        viewModelScope.launch {
            try {
                val cityResponses = citySearchApiService.searchCities(query, limit = 5, apiKey = apiKey)
                val cities = cityResponses.mapNotNull { response ->
                    if (response.lat != null && response.lon != null) {
                        City(
                            name = "${response.name}, ${response.country}",
                            latitude = response.lat,
                            longitude = response.lon
                        )
                    } else {
                        null
                    }
                }
                _cities.value = cities
                Log.d(TAG, "Filtered cities: ${cities.map { it.name }}")
            } catch (e: Exception) {
                Log.e(TAG, "Error filtering cities: ${e.message}")
            }
        }
    }

    fun removeCity(city: City) {
        Log.d(TAG, "Removing city: ${city.name}")
        val currentCities = _cities.value.orEmpty().toMutableList()
        currentCities.remove(city)
        _cities.value = currentCities

        val currentSelectedCities = _selectedCities.value.orEmpty().toMutableList()
        currentSelectedCities.remove(city)
        _selectedCities.value = currentSelectedCities

        saveSelectedCities(currentSelectedCities)
    }

    private fun saveSelectedCities(cities: List<City>) {
        Log.d(TAG, "Saving selected cities: ${cities.map { it.name }}")
        val cityData = cities.joinToString(";") { city ->
            "${city.name}|${city.latitude}|${city.longitude}|${city.temperature}|${city.time}"
        }

        sharedPreferences.edit().apply {
            putString("selected_cities", cityData)
            apply()
        }
    }

    private fun loadSelectedCities(): MutableList<City> {
        Log.d(TAG, "Loading selected cities")
        val cityData = sharedPreferences.getString("selected_cities", "").orEmpty()

        return if (cityData.isNotEmpty()) {
            cityData.split(";").mapNotNull { data ->
                val parts = data.split("|")
                if (parts.size == 5) {
                    City(
                        name = parts[0],
                        latitude = parts[1].toDoubleOrNull() ?: 0.0,
                        longitude = parts[2].toDoubleOrNull() ?: 0.0,
                        temperature = parts[3].toIntOrNull() ?: 0,
                        time = parts[4],
                        isSaved = true
                    )
                } else {
                    null
                }
            }.toMutableList()
        } else {
            mutableListOf()
        }
    }

    private fun getCurrentTimeForCity(timezoneOffset: Int): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.SECOND, timezoneOffset)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    private suspend fun fetchWeatherForCity(city: City): City {
        val response = weatherApiService.getWeatherForCity(city.name, apiKey)
        return city.copy(
            temperature = response.main.temp.toInt(),
            time = getCurrentTimeForCity(response.timezone),
            isSaved = true
        )
    }

    fun fetchWeatherForSelectedCities() {
        viewModelScope.launch {
            val updatedCities = _selectedCities.value?.map { city ->
                try {
                    fetchWeatherForCity(city)
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching weather for ${city.name}: ${e.message}")
                    city
                }
            }
            _selectedCities.value = updatedCities?.toMutableList()
            saveSelectedCities(updatedCities ?: emptyList())
        }
    }
    private fun fetchWeatherForKenyanCities() {
        viewModelScope.launch {
            val updatedCities = kenyanCitiesList.map { city ->
                try {
                    fetchWeatherForCity(city)
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching weather for ${city.name}: ${e.message}")
                    city
                }
            }
            _kenyanCities.value = updatedCities
        }
    }
}