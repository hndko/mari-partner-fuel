package id.maripartner.fuel.feature.vehicles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelTopBar

@Composable
fun VehiclesScreen(
    onNavigateToAddVehicle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            FuelTopBar(title = "Kendaraan Saya")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddVehicle,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Kendaraan")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FuelEmptyState(
                title = "Belum Ada Kendaraan",
                subtitle = "Tambahkan motor atau mobil Anda untuk memisahkan pencatatan BBM dan melacak konsumsi tiap kendaraan.",
                icon = Icons.Default.DirectionsCar,
                actionButtonText = "Tambah Kendaraan Baru",
                onActionClick = onNavigateToAddVehicle
            )
        }
    }
}
