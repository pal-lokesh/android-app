package com.startup.recordservice

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.startup.recordservice.ui.navigation.AppNavigation
import com.startup.recordservice.ui.theme.RecordServiceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Log.d(TAG, "MainActivity onCreate started")
            val splashScreen = installSplashScreen()
            super.onCreate(savedInstanceState)
            Log.d(TAG, "MainActivity super.onCreate completed")
            
            setContent {
                Log.d(TAG, "MainActivity setContent called")
                RecordServiceTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
            Log.d(TAG, "MainActivity onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "FATAL ERROR in MainActivity.onCreate", e)
            e.printStackTrace()
            throw e
        }
    }
}
