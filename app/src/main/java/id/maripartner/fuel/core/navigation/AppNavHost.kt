package id.maripartner.fuel.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.maripartner.fuel.feature.analytics.AnalyticsScreen
import id.maripartner.fuel.feature.auth.LoginScreen
import id.maripartner.fuel.feature.dashboard.DashboardScreen
import id.maripartner.fuel.feature.profile.ProfileScreen
import id.maripartner.fuel.feature.splash.SplashScreen
import id.maripartner.fuel.feature.transactions.TransactionsScreen
import id.maripartner.fuel.feature.vehicles.VehiclesScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoute.Splash.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = BottomNavItem.items.map { it.route }
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoute.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoute.Splash.route) {
                SplashScreen(
                    onSplashComplete = {
                        navController.navigate(NavRoute.Dashboard.route) {
                            popUpTo(NavRoute.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoute.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoute.Dashboard.route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        // In phase 2 register will be added
                    }
                )
            }

            // Bottom Navigation Destinations
            composable(NavRoute.Dashboard.route) {
                DashboardScreen(
                    onNavigateToScan = {
                        navController.navigate(NavRoute.ReceiptScan.route)
                    },
                    onNavigateToManualInput = {
                        navController.navigate(NavRoute.TransactionForm.route)
                    },
                    onNavigateToProPlans = {
                        navController.navigate(NavRoute.ProPlans.route)
                    }
                )
            }

            composable(NavRoute.Transactions.route) {
                TransactionsScreen(
                    onNavigateToCreate = {
                        navController.navigate(NavRoute.TransactionForm.route)
                    }
                )
            }

            composable(NavRoute.Analytics.route) {
                AnalyticsScreen()
            }

            composable(NavRoute.Vehicles.route) {
                VehiclesScreen(
                    onNavigateToAddVehicle = {
                        // Vehicles flow
                    }
                )
            }

            composable(NavRoute.Profile.route) {
                ProfileScreen(
                    onNavigateToProPlans = {
                        navController.navigate(NavRoute.ProPlans.route)
                    },
                    onLogout = {
                        navController.navigate(NavRoute.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Placeholder for sub-flows to be implemented in respective phases
            composable(NavRoute.ReceiptScan.route) {
                DashboardScreen(
                    onNavigateToScan = {},
                    onNavigateToManualInput = {},
                    onNavigateToProPlans = {}
                )
            }

            composable(NavRoute.TransactionForm.route) {
                TransactionsScreen(onNavigateToCreate = {})
            }

            composable(NavRoute.ProPlans.route) {
                ProfileScreen(onNavigateToProPlans = {}, onLogout = {})
            }
        }
    }
}
