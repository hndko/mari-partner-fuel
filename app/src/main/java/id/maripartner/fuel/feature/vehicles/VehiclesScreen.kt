package id.maripartner.fuel.feature.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType

@Composable
fun VehiclesScreen(
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToEditVehicle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VehiclesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var vehicleToDelete by remember { mutableStateOf<Vehicle?>(null) }
    var vehicleToArchive by remember { mutableStateOf<Vehicle?>(null) }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(VehicleUiEvent.DismissMessage)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(VehicleUiEvent.DismissMessage)
        }
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Kendaraan Saya",
                actions = {
                    IconButton(onClick = { viewModel.onEvent(VehicleUiEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Muat Ulang"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Tabs: Aktif, Diarsipkan, Semua
            TabRow(
                selectedTabIndex = uiState.filterTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                VehicleFilterTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.filterTab == tab,
                        onClick = { viewModel.onEvent(VehicleUiEvent.SetFilterTab(tab)) },
                        text = {
                            Text(
                                text = tab.displayName,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            // Konten Berdasarkan Status UI
            when {
                uiState.isLoading && uiState.vehicles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.filteredVehicles.isEmpty() -> {
                    val emptySubtitle = when (uiState.filterTab) {
                        VehicleFilterTab.ACTIVE -> "Tambahkan kendaraan aktif Anda untuk mulai mencatat pengeluaran BBM."
                        VehicleFilterTab.ARCHIVED -> "Belum ada kendaraan yang diarsipkan."
                        VehicleFilterTab.ALL -> "Daftar kendaraan masih kosong."
                    }
                    FuelEmptyState(
                        title = "Tidak Ada Kendaraan",
                        subtitle = emptySubtitle,
                        icon = Icons.Default.DirectionsCar,
                        actionButtonText = if (uiState.filterTab == VehicleFilterTab.ACTIVE) "Tambah Kendaraan Baru" else null,
                        onActionClick = if (uiState.filterTab == VehicleFilterTab.ACTIVE) onNavigateToAddVehicle else null
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredVehicles,
                            key = { it.id }
                        ) { vehicle ->
                            VehicleCard(
                                vehicle = vehicle,
                                onEdit = { onNavigateToEditVehicle(vehicle.id) },
                                onArchive = { vehicleToArchive = vehicle },
                                onDelete = { vehicleToDelete = vehicle }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Arsip / Aktifkan
    vehicleToArchive?.let { vehicle ->
        val willArchive = !vehicle.isArchived
        val dialogTitle = if (willArchive) "Arsipkan Kendaraan?" else "Aktifkan Kembali Kendaraan?"
        val dialogBody = if (willArchive) {
            "Kendaraan '${vehicle.name}' tidak akan muncul sebagai opsi default pada pencatatan transaksi baru, tetapi riwayat transaksi lama tetap tersimpan utuh."
        } else {
            "Kendaraan '${vehicle.name}' akan kembali aktif dan dapat dipilih saat mencatat transaksi BBM baru."
        }

        AlertDialog(
            onDismissRequest = { vehicleToArchive = null },
            title = { Text(dialogTitle) },
            text = { Text(dialogBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(VehicleUiEvent.ArchiveVehicle(vehicle.id, willArchive))
                        vehicleToArchive = null
                    }
                ) {
                    Text(if (willArchive) "Arsipkan" else "Aktifkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToArchive = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Permanen
    vehicleToDelete?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            title = { Text("Hapus Kendaraan?") },
            text = {
                Text(
                    "Anda yakin ingin menghapus '${vehicle.name}'? Penghapusan permanen hanya diizinkan jika kendaraan belum memiliki riwayat transaksi BBM."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(VehicleUiEvent.DeleteVehicle(vehicle.id))
                        vehicleToDelete = null
                    }
                ) {
                    Text(
                        "Hapus",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (vehicle.isArchived) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikon Tipe Kendaraan
                val icon: ImageVector = when (vehicle.vehicleType) {
                    VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
                    VehicleType.CAR -> Icons.Default.DirectionsCar
                    VehicleType.OTHER -> Icons.Default.DirectionsCar
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = vehicle.vehicleType.displayName,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = vehicle.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (vehicle.isArchived) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Diarsipkan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = vehicle.displaySubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Chip Odometer & Jenis BBM
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (vehicle.lastOdometerKm != null && vehicle.lastOdometerKm >= 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Odometer",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${vehicle.lastOdometerKm.toLong()} km",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                if (!vehicle.defaultFuelType.isNullOrBlank()) {
                    Text(
                        text = vehicle.defaultFuelType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Kendaraan",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = if (vehicle.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = if (vehicle.isArchived) "Aktifkan" else "Arsipkan",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Kendaraan",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
