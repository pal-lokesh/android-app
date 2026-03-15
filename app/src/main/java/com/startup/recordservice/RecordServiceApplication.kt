package com.startup.recordservice

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RecordServiceApplication : Application() {
    
    companion object {
        private const val TAG = "RecordServiceApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Set up global exception handler to catch crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e(TAG, "Uncaught exception in thread: ${thread.name}", exception)
            exception.printStackTrace()
            // Call the default handler
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            defaultHandler?.uncaughtException(thread, exception)
        }
        
        Log.d(TAG, "Application onCreate completed")
    }
}
