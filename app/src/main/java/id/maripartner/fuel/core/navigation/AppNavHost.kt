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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import id.maripartner.fuel.data.repository.AuthRepositoryImpl
import id.maripartner.fuel.feature.analytics.AnalyticsScreen
import id.maripartner.fuel.feature.auth.AuthUiEvent
import id.maripartner.fuel.feature.auth.AuthViewModel
import id.maripartner.fuel.feature.auth.ForgotPasswordScreen
import id.maripartner.fuel.feature.auth.LoginScreen
import id.maripartner.fuel.feature.auth.RegisterScreen
import id.maripartner.fuel.feature.dashboard.DashboardScreen
import id.maripartner.fuel.feature.profile.ProfileScreen
import id.maripartner.fuel.feature.receipt_scan.ReceiptScanScreen
import id.maripartner.fuel.feature.receipt_scan.ReceiptScanViewModel
import id.maripartner.fuel.feature.receipt_scan.ScanReviewScreen
import id.maripartner.fuel.feature.reminders.ReminderFormScreen
import id.maripartner.fuel.feature.reminders.RemindersScreen
import id.maripartner.fuel.feature.reminders.RemindersViewModel
import id.maripartner.fuel.feature.splash.SplashScreen
import id.maripartner.fuel.feature.transactions.TransactionDetailScreen
import id.maripartner.fuel.feature.subscription.ProPlansScreen
import id.maripartner.fuel.feature.subscription.checkout.CheckoutScreen
import id.maripartner.fuel.feature.subscription.qris.QrisPaymentScreen
import id.maripartner.fuel.feature.subscription.status.PaymentStatusScreen
import id.maripartner.fuel.feature.transactions.TransactionFormScreen
import id.maripartner.fuel.feature.transactions.TransactionsScreen
import id.maripartner.fuel.feature.vehicles.VehicleFormScreen
import id.maripartner.fuel.feature.vehicles.VehiclesScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoute.Splash.route,
    authViewModel: AuthViewModel = viewModel(),
    receiptScanViewModel: ReceiptScanViewModel = viewModel(),
    remindersViewModel: RemindersViewModel = viewModel()
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
                        navController.navigate(NavRoute.TransactionForm.createRoute())
                    },
                    onNavigateToProPlans = {
                        navController.navigate(NavRoute.ProPlans.route)
                    },
                    onNavigateToReminders = {
                        navController.navigate(NavRoute.Reminders.route)
                    },
                    onNavigateToTransactions = {
                        navController.navigate(NavRoute.Transactions.route)
                    }
                )
            }

            composable(NavRoute.Transactions.route) {
                TransactionsScreen(
                    onNavigateToCreate = {
                        navController.navigate(NavRoute.TransactionForm.createRoute())
                    },
                    onNavigateToDetail = { id ->
                        navController.navigate(NavRoute.TransactionDetail.createRoute(id))
                    }
                )
            }

            composable(NavRoute.Analytics.route) {
                AnalyticsScreen()
            }

            composable(NavRoute.Vehicles.route) {
                VehiclesScreen(
                    onNavigateToAddVehicle = {
                        navController.navigate(NavRoute.VehicleForm.createRoute())
                    },
                    onNavigateToEditVehicle = { id ->
                        navController.navigate(NavRoute.VehicleForm.createRoute(id))
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

            // 4. Sub-alur Manajemen Kendaraan
            composable(
                route = NavRoute.VehicleForm.route,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getString("id")
                VehicleFormScreen(
                    vehicleId = vehicleId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 5. Sub-alur Transaksi BBM Manual & Detail
            composable(
                route = NavRoute.TransactionForm.route,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("id")
                TransactionFormScreen(
                    transactionId = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoute.TransactionDetail.route,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                TransactionDetailScreen(
                    transactionId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { txId ->
                        navController.navigate(NavRoute.TransactionForm.createRoute(txId))
                    }
                )
            }

            // 6. Sub-alur Pindai Struk OCR & Review (Human-in-the-Loop)
            composable(NavRoute.ReceiptScan.route) {
                ReceiptScanScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReview = { navController.navigate(NavRoute.ScanReview.route) },
                    viewModel = receiptScanViewModel
                )
            }

            composable(NavRoute.ScanReview.route) {
                ScanReviewScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        navController.navigate(NavRoute.Transactions.route) {
                            popUpTo(NavRoute.Dashboard.route)
                        }
                    },
                    viewModel = receiptScanViewModel
                )
            }

            // 7. Sub-alur Pengingat Perawatan Kendaraan
            composable(NavRoute.Reminders.route) {
                RemindersScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreate = { navController.navigate(NavRoute.ReminderForm.createRoute()) },
                    onNavigateToEdit = { id -> navController.navigate(NavRoute.ReminderForm.createRoute(id)) },
                    viewModel = remindersViewModel
                )
            }

            composable(
                route = NavRoute.ReminderForm.route,
                arguments = listOf(navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val reminderId = backStackEntry.arguments?.getString("id")
                ReminderFormScreen(
                    reminderId = reminderId,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    viewModel = remindersViewModel
                )
            }

            // Halaman Paket & Langganan (Tahap 6 & 7)
            composable(NavRoute.ProPlans.route) {
                ProPlansScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCheckout = { planCode ->
                        navController.navigate(NavRoute.Checkout.createRoute(planCode))
                    }
                )
            }

            // Halaman Checkout Pembayaran (Tahap 7)
            composable(NavRoute.Checkout.route) { backStackEntry ->
                val planCode = backStackEntry.arguments?.getString("planId") ?: "pro_monthly"
                CheckoutScreen(
                    planCode = planCode,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToQris = { ref ->
                        navController.navigate(NavRoute.QrisPayment.createRoute(ref))
                    },
                    onNavigateToSuccess = { ref ->
                        navController.navigate(NavRoute.PaymentStatus.createRoute(ref))
                    }
                )
            }

            // Halaman Pembayaran QRIS (Tahap 7)
            composable(NavRoute.QrisPayment.route) { backStackEntry ->
                val paymentRef = backStackEntry.arguments?.getString("paymentReference").orEmpty()
                QrisPaymentScreen(
                    paymentReference = paymentRef,
                    onNavigateBack = { navController.popBackStack() },
                    onPaymentSuccess = { ref ->
                        navController.navigate(NavRoute.PaymentStatus.createRoute(ref))
                    }
                )
            }

            // Halaman Status Pembayaran Berhasil (Tahap 7)
            composable(NavRoute.PaymentStatus.route) { backStackEntry ->
                val paymentRef = backStackEntry.arguments?.getString("paymentReference").orEmpty()
                PaymentStatusScreen(
                    paymentReference = paymentRef,
                    onNavigateToDashboard = {
                        navController.navigate(NavRoute.Dashboard.route) {
                            popUpTo(NavRoute.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
