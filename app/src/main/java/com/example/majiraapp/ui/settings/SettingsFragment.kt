package com.example.majiraapp.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.majiraapp.R
import com.example.majiraapp.databinding.FragmentSettingsBinding
import com.example.majiraapp.ui.TemperatureUnit
import com.example.majiraapp.ui.WeatherPreferences
import com.example.majiraapp.ui.shared.SharedViewModel

class SettingsFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var prefs: WeatherPreferences

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, enable location
            prefs.setLocationEnabled(true)
            binding.locationSwitch.isChecked = true
            sharedViewModel.setLocationEnabled(true)
        } else {
            // Permission denied, keep location disabled
            prefs.setLocationEnabled(false)
            binding.locationSwitch.isChecked = false
            sharedViewModel.setLocationEnabled(false)
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, enable notifications
            prefs.setNotificationsEnabled(true)
            binding.notificationsSwitch.isChecked = true
            sharedViewModel.setNotificationsEnabled(true)
        } else {
            // Permission denied, keep notifications disabled
            prefs.setNotificationsEnabled(false)
            binding.notificationsSwitch.isChecked = false
            sharedViewModel.setNotificationsEnabled(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        prefs = WeatherPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
        setListeners()
    }

    private fun loadSettings() {
        binding.locationSwitch.isChecked = prefs.getLocationEnabled()
        binding.notificationsSwitch.isChecked = prefs.getNotificationsEnabled()
        binding.themeSwitch.isChecked = prefs.getThemeMode()
        binding.temperatureRadioGroup.check(
            if (prefs.getTemperatureUnit() == TemperatureUnit.CELSIUS)
                R.id.celsiusRadioButton
            else
                R.id.fahrenheitRadioButton
        )
        binding.ratingBar.rating = prefs.getAppRating()
    }

    private fun setListeners() {
        binding.locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestLocationPermission()
            } else {
                prefs.setLocationEnabled(false)
                sharedViewModel.setLocationEnabled(false)
            }
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestNotificationPermission()
            } else {
                prefs.setNotificationsEnabled(false)
                sharedViewModel.setNotificationsEnabled(false)
            }
        }

        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setThemeMode(isChecked)
            sharedViewModel.setThemeMode(isChecked)
            updateTheme(isChecked)
        }

        binding.temperatureRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val unit = if (checkedId == R.id.celsiusRadioButton) TemperatureUnit.CELSIUS else TemperatureUnit.FAHRENHEIT
            prefs.setTemperatureUnit(unit)
            sharedViewModel.setTemperatureUnit(unit)
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            prefs.setAppRating(rating)
            sharedViewModel.setAppRating(rating)
        }
    }

    private fun updateTheme(darkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        requireActivity().recreate()
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                prefs.setLocationEnabled(true)
                sharedViewModel.setLocationEnabled(true)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Explain to the user why we need the permission
                showLocationPermissionExplanationDialog()
            }
            else -> {
                // Request the permission
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun showLocationPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Needed")
            .setMessage("We need access to your location to provide you with precise and accurate weather information for your area. This allows us to give you the most relevant forecasts and weather alerts.")
            .setPositiveButton("OK") { _, _ ->
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                binding.locationSwitch.isChecked = false
            }
            .create()
            .show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                    prefs.setNotificationsEnabled(true)
                    sharedViewModel.setNotificationsEnabled(true)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explain to the user why we need the permission
                    // You might want to show a dialog here similar to location permission
                }
                else -> {
                    // Request the permission
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For versions below Android 13, no runtime permission is needed
            prefs.setNotificationsEnabled(true)
            sharedViewModel.setNotificationsEnabled(true)
        }
    }
}