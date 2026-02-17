package com.startup.recordservice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.startup.recordservice.ui.viewmodel.AuthViewModel
import com.startup.recordservice.ui.viewmodel.StartupViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val startupViewModel: StartupViewModel = hiltViewModel()
    
    // Check if user is logged in on app start
    val isLoggedIn = authViewModel.isLoggedIn()
    val userType = authViewModel.getCurrentUserType()
    
    // Fetch startup data immediately when app starts (if logged in)
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // Pre-fetch data on startup for better UX
            startupViewModel.loadStartupData()
        }
    }
    
    val startDestination = if (isLoggedIn && userType != null) {
        if (userType == "VENDOR") Screen.VendorDashboard.route
        else Screen.ClientDashboard.route
    } else {
        Screen.Login.route
    }
    
    NavGraph(
        navController = navController,
        startDestination = startDestination,
        authViewModel = authViewModel
    )
}
