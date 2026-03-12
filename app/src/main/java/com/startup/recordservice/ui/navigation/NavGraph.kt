package com.startup.recordservice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavOptionsBuilder
import com.startup.recordservice.ui.screens.auth.LoginScreen
import com.startup.recordservice.ui.screens.auth.SignupScreen
import com.startup.recordservice.ui.screens.client.ClientDashboardScreen
import com.startup.recordservice.ui.screens.client.ClientExploreScreen
import com.startup.recordservice.ui.screens.client.ClientOrderHistoryScreen
import com.startup.recordservice.ui.screens.client.ClientProfileScreen
import com.startup.recordservice.ui.screens.client.BusinessDetailScreen
import com.startup.recordservice.ui.screens.client.OrderDetailsScreen
import com.startup.recordservice.ui.screens.vendor.AvailabilityManagementScreen
import com.startup.recordservice.ui.screens.vendor.VendorDashboardScreen
import com.startup.recordservice.ui.screens.vendor.VendorProfileScreen
import com.startup.recordservice.ui.screens.vendor.VendorTab
import com.startup.recordservice.ui.screens.vendor.CreateBusinessScreen
import com.startup.recordservice.ui.screens.vendor.CreateInventoryScreen
import com.startup.recordservice.ui.screens.vendor.CreateThemeScreen
import com.startup.recordservice.ui.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ClientDashboard : Screen("client_dashboard")
    object VendorDashboard : Screen("vendor_dashboard")
    object VendorOrders : Screen("vendor_orders")
    object VendorThemes : Screen("vendor_themes")
    object VendorInventory : Screen("vendor_inventory")
    object VendorAvailability : Screen("vendor_availability")
    object VendorProfile : Screen("vendor_profile")
    object ClientExplore : Screen("client_explore")
    object ClientProfile : Screen("client_profile")
    object ClientOrders : Screen("client_orders")
    object ClientOrderDetails : Screen("client_order_details/{orderId}") {
        fun createRoute(orderId: String) = "client_order_details/$orderId"
    }
    object CreateBusiness : Screen("create_business")
    object CreateInventory : Screen("create_inventory")
    object CreateTheme : Screen("create_theme")
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
    fun NavHostController.navigateVendor(route: String) {
        navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Screen.VendorDashboard.route) { saveState = true }
        }
    }

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
                            else Screen.ClientExplore.route
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
                            else Screen.ClientExplore.route
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
                onNavigateToOrders = {
                    navController.navigate(Screen.ClientOrders.route)
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
                },
                onProfileClick = {
                    navController.navigate(Screen.ClientProfile.route)
                }
            )
        }

        composable(Screen.ClientOrders.route) {
            ClientOrderHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onViewDetails = { orderId ->
                    navController.navigate(Screen.ClientOrderDetails.createRoute(orderId))
                }
            )
        }

        composable(
            route = Screen.ClientOrderDetails.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailsScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() }
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
                },
                onCreateBusinessClick = {
                    navController.navigate(Screen.CreateBusiness.route)
                },
                onAddInventoryClick = {
                    navController.navigate(Screen.CreateInventory.route)
                },
                onAddThemeClick = {
                    navController.navigate(Screen.CreateTheme.route)
                },
                onDashboardClick = { /* already on dashboard */ },
                onAvailabilityClick = {
                    navController.navigateVendor(Screen.VendorAvailability.route)
                },
                onOrdersClick = {
                    navController.navigateVendor(Screen.VendorOrders.route)
                },
                onInventoryTabClick = {
                    navController.navigateVendor(Screen.VendorInventory.route)
                },
                onThemeTabClick = {
                    navController.navigateVendor(Screen.VendorThemes.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.VendorProfile.route)
                },
                currentTab = VendorTab.DASHBOARD
            )
        }

        // For now, reuse VendorDashboardScreen for these tabs (you can swap to dedicated screens later)
        composable(Screen.VendorOrders.route) {
            VendorDashboardScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCreateBusinessClick = {
                    navController.navigate(Screen.CreateBusiness.route)
                },
                onAddInventoryClick = {
                    navController.navigate(Screen.CreateInventory.route)
                },
                onAddThemeClick = {
                    navController.navigate(Screen.CreateTheme.route)
                },
                onDashboardClick = {
                    navController.navigateVendor(Screen.VendorDashboard.route)
                },
                onAvailabilityClick = {
                    navController.navigateVendor(Screen.VendorAvailability.route)
                },
                onOrdersClick = { /* already on orders tab */ },
                onInventoryTabClick = {
                    navController.navigateVendor(Screen.VendorInventory.route)
                },
                onThemeTabClick = {
                    navController.navigateVendor(Screen.VendorThemes.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.VendorProfile.route)
                },
                currentTab = VendorTab.ORDERS
            )
        }

        composable(Screen.VendorInventory.route) {
            VendorDashboardScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCreateBusinessClick = {
                    navController.navigate(Screen.CreateBusiness.route)
                },
                onAddInventoryClick = {
                    navController.navigate(Screen.CreateInventory.route)
                },
                onAddThemeClick = {
                    navController.navigate(Screen.CreateTheme.route)
                },
                onDashboardClick = {
                    navController.navigateVendor(Screen.VendorDashboard.route)
                },
                onAvailabilityClick = {
                    navController.navigateVendor(Screen.VendorAvailability.route)
                },
                onOrdersClick = {
                    navController.navigateVendor(Screen.VendorOrders.route)
                },
                onInventoryTabClick = { /* already on inventory tab */ },
                onThemeTabClick = {
                    navController.navigateVendor(Screen.VendorThemes.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.VendorProfile.route)
                },
                currentTab = VendorTab.INVENTORY
            )
        }

        composable(Screen.VendorThemes.route) {
            VendorDashboardScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCreateBusinessClick = {
                    navController.navigate(Screen.CreateBusiness.route)
                },
                onAddInventoryClick = {
                    navController.navigate(Screen.CreateInventory.route)
                },
                onAddThemeClick = {
                    navController.navigate(Screen.CreateTheme.route)
                },
                onDashboardClick = {
                    navController.navigateVendor(Screen.VendorDashboard.route)
                },
                onAvailabilityClick = {
                    navController.navigateVendor(Screen.VendorAvailability.route)
                },
                onOrdersClick = {
                    navController.navigateVendor(Screen.VendorOrders.route)
                },
                onInventoryTabClick = {
                    navController.navigateVendor(Screen.VendorInventory.route)
                },
                onThemeTabClick = { /* already on theme tab */ },
                onProfileClick = {
                    navController.navigate(Screen.VendorProfile.route)
                },
                currentTab = VendorTab.THEME
            )
        }

        composable(Screen.VendorAvailability.route) {
            AvailabilityManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onDashboardClick = { navController.navigateVendor(Screen.VendorDashboard.route) },
                onOrdersClick = { navController.navigateVendor(Screen.VendorOrders.route) },
                onThemeTabClick = { navController.navigateVendor(Screen.VendorThemes.route) },
                onInventoryTabClick = { navController.navigateVendor(Screen.VendorInventory.route) },
                onAvailabilityClick = { /* already on availability */ }
            )
        }

        composable(Screen.VendorProfile.route) {
            VendorProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateBusiness.route) {
            CreateBusinessScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateInventory.route) {
            CreateInventoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateTheme.route) {
            CreateThemeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ClientProfile.route) {
            ClientProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.ClientOrders.route)
                }
            )
        }
    }
}
