package id.maripartner.fuel.feature.owner.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.ui.graphics.vector.ImageVector

enum class OwnerTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Ringkasan", Icons.Default.Dashboard),
    PLANS("Paket & Harga", Icons.Default.PriceChange),
    USERS("Pengguna", Icons.Default.Group),
    TRANSACTIONS("Transaksi", Icons.Default.Payments)
}
