package id.maripartner.fuel.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.maripartner.fuel.data.repository.AuthRepositoryImpl
import id.maripartner.fuel.feature.analytics.AnalyticsScreen
import id.maripartner.fuel.feature.auth.AuthUiEvent
import id.maripartner.fuel.feature.auth.AuthViewModel
import id.maripartner.fuel.feature.auth.ForgotPasswordScreen
import id.maripartner.fuel.feature.auth.LoginScreen
import id.maripartner.fuel.feature.auth.RegisterScreen
import id.maripartner.fuel.feature.dashboard.DashboardScreen
import id.maripartner.fuel.feature.profile.ProfileScreen
import id.maripartner.fuel.feature.splash.SplashScreen
import id.maripartner.fuel.feature.transactions.TransactionsScreen
import id.maripartner.fuel.feature.vehicles.VehiclesScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoute.Splash.route,
    authViewModel: AuthViewModel = viewModel()
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
            // 1. Splash Screen dengan pemulihan sesi otomatis
            composable(NavRoute.Splash.route) {
                SplashScreen(
                    onSplashComplete = {
                        val session = AuthRepositoryImpl().getCurrentSession()
                        val targetRoute = if (session != null) {
                            NavRoute.Dashboard.route
                        } else {
                            NavRoute.Login.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(NavRoute.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Layar Autentikasi
            composable(NavRoute.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoute.Dashboard.route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavRoute.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(NavRoute.ForgotPassword.route)
                    },
                    viewModel = authViewModel
                )
            }

            composable(NavRoute.Register.route) {
                RegisterScreen(
                    onNavigateBackToLogin = {
                        navController.popBackStack()
                    },
                    viewModel = authViewModel
                )
            }

            composable(NavRoute.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBackToLogin = {
                        navController.popBackStack()
                    },
                    viewModel = authViewModel
                )
            }

            // 3. Destinasi 5 Tab Bottom Navigation
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
                        // Handled in vehicles phase
                    }
                )
            }

            composable(NavRoute.Profile.route) {
                ProfileScreen(
                    onNavigateToProPlans = {
                        navController.navigate(NavRoute.ProPlans.route)
                    },
                    onLogout = {
                        authViewModel.onEvent(AuthUiEvent.SignOut)
                        navController.navigate(NavRoute.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Sub-alur placeholder
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
