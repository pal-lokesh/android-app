package com.startup.recordservice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.startup.recordservice.ui.screens.auth.LoginScreen
import com.startup.recordservice.ui.screens.auth.SignupScreen
import com.startup.recordservice.ui.screens.client.ClientDashboardScreen
import com.startup.recordservice.ui.screens.client.ClientExploreScreen
import com.startup.recordservice.ui.screens.client.BusinessDetailScreen
import com.startup.recordservice.ui.screens.vendor.VendorDashboardScreen
import com.startup.recordservice.ui.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ClientDashboard : Screen("client_dashboard")
    object VendorDashboard : Screen("vendor_dashboard")
    object ClientExplore : Screen("client_explore")
    object BusinessDetail : Screen("business_detail/{businessId}") {
        fun createRoute(businessId: String) = "business_detail/$businessId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { userType ->
                    try {
                        val destination = if (userType == "VENDOR") Screen.VendorDashboard.route
                            else Screen.ClientDashboard.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NavGraph", "Navigation error: ${e.message}", e)
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSuccess = { userType ->
                    try {
                        val destination = if (userType == "VENDOR") Screen.VendorDashboard.route
                            else Screen.ClientDashboard.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NavGraph", "Navigation error: ${e.message}", e)
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ClientDashboard.route) {
            ClientDashboardScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToExplore = {
                    navController.navigate(Screen.ClientExplore.route)
                },
                onBusinessClick = { businessId ->
                    try {
                        if (businessId.isNotBlank()) {
                            navController.navigate(Screen.BusinessDetail.createRoute(businessId))
                        } else {
                            android.util.Log.e("NavGraph", "Cannot navigate: businessId is blank")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NavGraph", "Navigation error: ${e.message}", e)
                    }
                }
            )
        }
        
        composable(Screen.ClientExplore.route) {
            ClientExploreScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onBusinessClick = { businessId ->
                    try {
                        if (businessId.isNotBlank()) {
                            navController.navigate(Screen.BusinessDetail.createRoute(businessId))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NavGraph", "Navigation error: ${e.message}", e)
                    }
                }
            )
        }
        
        composable(
            route = Screen.BusinessDetail.route,
            arguments = listOf(
                navArgument("businessId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            BusinessDetailScreen(
                businessId = businessId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.VendorDashboard.route) {
            VendorDashboardScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
