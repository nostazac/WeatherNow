package com.example.majiraapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.majiraapp.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       supportActionBar?.hide()
        actionBar?.hide()
        setContentView(R.layout.activity_splash)

        // Hide the status bar for a fullscreen splash
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        // Use a coroutine to delay the splash screen
        lifecycleScope.launch {
            delay(2000) // 2 seconds delay
            startActivity(Intent(this@SplashActivity, StepActivity::class.java))
            finish()
        }
    }
}