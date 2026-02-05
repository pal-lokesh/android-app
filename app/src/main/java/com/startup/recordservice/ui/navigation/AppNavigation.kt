package com.startup.recordservice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.startup.recordservice.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    // Check if user is logged in on app start
    val isLoggedIn = authViewModel.isLoggedIn()
    val userType = authViewModel.getCurrentUserType()
    
    val startDestination = if (isLoggedIn && userType != null) {
        if (userType == "VENDOR") Screen.VendorDashboard.route
        else Screen.ClientDashboard.route
    } else {
        Screen.Login.route
    }
    
    // Handle 401 errors - logout and navigate to login
    LaunchedEffect(Unit) {
        // This will be triggered when AuthInterceptor detects 401
        // The ViewModel should observe token changes and handle logout
    }
    
    NavGraph(
        navController = navController,
        startDestination = startDestination,
        authViewModel = authViewModel
    )
}
