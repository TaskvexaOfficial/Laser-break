package com.example

import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.LaserBreakTheme
import com.example.ui.AppNavigation
import com.example.audio.SoundManager

class MainActivity : ComponentActivity() {
    private lateinit var soundManager: SoundManager

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(soundManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)
        lifecycle.addObserver(soundManager)

        MobileAds.initialize(this) {}
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        
        setContent {
            LaserBreakTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(soundManager = soundManager)
                }
            }
        }
    }
}
