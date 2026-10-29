package id.maripartner.fuel.feature.analytics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelTopBar

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            FuelTopBar(title = "Analitik & Konsumsi BBM")
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FuelEmptyState(
                title = "Data Belum Cukup",
                subtitle = "Analitik konsumsi km/liter dan grafik pengeluaran akan otomatis dihitung setelah Anda memiliki minimal 2 transaksi dengan odometer.",
                icon = Icons.Default.BarChart
            )
        }
    }
}
