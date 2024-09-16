package com.example.majiraapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.majiraapp.databinding.ActivityMainBinding
import com.example.majiraapp.ui.cities.CityAdapter
import com.example.majiraapp.ui.data.City
import com.example.majiraapp.ui.service.WeatherService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var searchEditText: EditText
    private lateinit var citiesRecyclerView: RecyclerView
    private lateinit var cityAdapter: CityAdapter
    private val cities = mutableListOf<City>()
    private val weatherService = WeatherService()

    private fun loadWeatherData() {
        GlobalScope.launch {
            cities.forEach { city ->
                val weather = weatherService.getWeatherForCity(city.name)
                runOnUiThread {
                    city.temperature = weather.temperature.toInt()
                    cityAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Log.d("MainActivity", "onCreate called")

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("MainActivity", "User not logged in, redirecting to com.example.majiraapp.LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        Log.d("MainActivity", "User is logged in: ${currentUser.email}")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_cities, R.id.navigation_map, R.id.navigation_settings
            )
        )
        // Remove this line if you don't want to use the ActionBar
        // setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        Log.d("MainActivity", "MainActivity setup completed")

        loadWeatherData()
    }


}
