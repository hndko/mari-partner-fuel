package id.maripartner.fuel.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomNavItem(
        route = NavRoute.Dashboard.route,
        title = "Beranda",
        icon = Icons.Default.Dashboard
    )

    data object Transactions : BottomNavItem(
        route = NavRoute.Transactions.route,
        title = "Riwayat",
        icon = Icons.AutoMirrored.Filled.ReceiptLong
    )

    data object Analytics : BottomNavItem(
        route = NavRoute.Analytics.route,
        title = "Analitik",
        icon = Icons.Default.BarChart
    )

    data object Vehicles : BottomNavItem(
        route = NavRoute.Vehicles.route,
        title = "Kendaraan",
        icon = Icons.Default.DirectionsCar
    )

    data object Profile : BottomNavItem(
        route = NavRoute.Profile.route,
        title = "Profil",
        icon = Icons.Default.Person
    )

    companion object {
        val items = listOf(Dashboard, Transactions, Analytics, Vehicles, Profile)
    }
}
