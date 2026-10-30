package id.maripartner.fuel.feature.vehicles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.FuelTextField
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType

/**
 * Layar Kendaraan Saya (Garasi) — Sesuai Panduan Desain Resmi Mari Partner Fuel
 */
@Composable
fun VehiclesScreen(
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToEditVehicle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VehiclesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var vehicleToDelete by remember { mutableStateOf<Vehicle?>(null) }
    var vehicleToArchive by remember { mutableStateOf<Vehicle?>(null) }

    // Selalu sinkronkan data saat layar aktif/kembali
    LaunchedEffect(Unit) {
        viewModel.onEvent(VehicleUiEvent.Refresh)
    }

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

    // Filter lokal berdasarkan tab dan kata kunci pencarian
    val displayedVehicles = remember(uiState.filteredVehicles, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.filteredVehicles
        } else {
            val query = searchQuery.trim()
            uiState.filteredVehicles.filter { v ->
                v.name.contains(query, ignoreCase = true) ||
                    (v.licensePlate?.contains(query, ignoreCase = true) == true) ||
                    (v.make?.contains(query, ignoreCase = true) == true) ||
                    (v.model?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Kendaraan Saya",
                actions = {
                    IconButton(onClick = { viewModel.onEvent(VehicleUiEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat Ulang Data",
                            tint = MariBrandNavy
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Page Title & Eyebrow
                item {
                    VehicleHeaderSection(onNavigateToAddVehicle = onNavigateToAddVehicle)
                }

                // 2. Search & Tab Filter Row
                item {
                    VehicleFilterSection(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        activeTab = uiState.filterTab,
                        onTabSelected = { viewModel.onEvent(VehicleUiEvent.SetFilterTab(it)) },
                        activeCount = uiState.vehicles.count { !it.isArchived },
                        archivedCount = uiState.vehicles.count { it.isArchived },
                        totalCount = uiState.vehicles.size
                    )
                }

                // 3. Status Konten: Loading, Empty, atau Daftar Kendaraan
                when {
                    uiState.isLoading && uiState.vehicles.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = MariPrimaryBlue,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = "Memuat daftar kendaraan…",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MariMutedLight
                                    )
                                }
                            }
                        }
                    }

                    displayedVehicles.isEmpty() -> {
                        item {
                            val isSearching = searchQuery.isNotBlank()
                            val title = if (isSearching) {
                                "Kendaraan Tidak Ditemukan"
                            } else {
                                when (uiState.filterTab) {
                                    VehicleFilterTab.ACTIVE -> "Belum Ada Kendaraan Aktif"
                                    VehicleFilterTab.ARCHIVED -> "Belum Ada Kendaraan Diarsipkan"
                                    VehicleFilterTab.ALL -> "Belum Ada Kendaraan"
                                }
                            }
                            val subtitle = if (isSearching) {
                                "Tidak ditemukan kendaraan yang cocok dengan kata kunci '$searchQuery'."
                            } else {
                                when (uiState.filterTab) {
                                    VehicleFilterTab.ACTIVE -> "Tambahkan kendaraan aktif Anda untuk mulai mencatat pengeluaran BBM."
                                    VehicleFilterTab.ARCHIVED -> "Kendaraan yang Anda arsipkan dari garasi akan tampil di sini."
                                    VehicleFilterTab.ALL -> "Daftar kendaraan Anda masih kosong. Tambahkan kendaraan pertama sekarang."
                                }
                            }

                            FuelEmptyState(
                                title = title,
                                subtitle = subtitle,
                                icon = Icons.Default.DirectionsCar,
                                actionButtonText = if (!isSearching && uiState.filterTab != VehicleFilterTab.ARCHIVED) {
                                    "＋ Tambah Kendaraan Baru"
                                } else null,
                                onActionClick = if (!isSearching && uiState.filterTab != VehicleFilterTab.ARCHIVED) {
                                    onNavigateToAddVehicle
                                } else null,
                                modifier = Modifier.padding(vertical = 32.dp)
                            )
                        }
                    }

                    else -> {
                        items(
                            items = displayedVehicles,
                            key = { it.id }
                        ) { vehicle ->
                            VehicleCard(
                                vehicle = vehicle,
                                onEdit = { onNavigateToEditVehicle(vehicle.id) },
                                onArchive = { vehicleToArchive = vehicle },
                                onDelete = { vehicleToDelete = vehicle }
                            )
                        }

                        // Add Vehicle Card di bagian akhir daftar sesuai desain referensi
                        item {
                            AddVehicleOutlineCard(onClick = onNavigateToAddVehicle)
                        }
                    }
                }
            }
        }
    }

    // Modal Konfirmasi Arsip / Aktifkan via FuelSweetAlertDialog
    vehicleToArchive?.let { vehicle ->
        val willArchive = !vehicle.isArchived
        FuelSweetAlertDialog(
            show = true,
            type = SweetAlertType.WARNING,
            title = if (willArchive) "Arsipkan Kendaraan?" else "Aktifkan Kembali?",
            message = if (willArchive) {
                "Kendaraan '${vehicle.name}' tidak akan muncul sebagai opsi default pada pencatatan transaksi baru, tetapi riwayat transaksi lama tetap tersimpan utuh."
            } else {
                "Kendaraan '${vehicle.name}' akan kembali aktif dan dapat dipilih saat mencatat transaksi BBM baru."
            },
            confirmText = if (willArchive) "Ya, Arsipkan" else "Ya, Aktifkan",
            dismissText = "Batal",
            onConfirm = {
                viewModel.onEvent(VehicleUiEvent.ArchiveVehicle(vehicle.id, willArchive))
                vehicleToArchive = null
            },
            onDismiss = { vehicleToArchive = null }
        )
    }

    // Modal Konfirmasi Hapus Permanen via FuelSweetAlertDialog
    vehicleToDelete?.let { vehicle ->
        FuelSweetAlertDialog(
            show = true,
            type = SweetAlertType.ERROR,
            title = "Hapus Kendaraan?",
            message = "Anda yakin ingin menghapus '${vehicle.name}' secara permanen? Penghapusan permanen hanya diizinkan jika kendaraan belum memiliki riwayat transaksi BBM.",
            confirmText = "Ya, Hapus",
            dismissText = "Batal",
            onConfirm = {
                viewModel.onEvent(VehicleUiEvent.DeleteVehicle(vehicle.id))
                vehicleToDelete = null
            },
            onDismiss = { vehicleToDelete = null }
        )
    }
}

/**
 * Header Section dengan Eyebrow GARASI & Tombol Tambah Cepat
 */
@Composable
private fun VehicleHeaderSection(onNavigateToAddVehicle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "GARASI",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                ),
                color = MariAccentCyan
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Kendaraan Saya",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MariBrandNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kelola kendaraan dan pantau konsumsi BBM masing-masing.",
                style = MaterialTheme.typography.bodySmall,
                color = MariMutedLight
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        FuelPrimaryButton(
            text = "＋ Tambah",
            onClick = onNavigateToAddVehicle,
            modifier = Modifier.widthIn(max = 130.dp)
        )
    }
}

/**
 * Search & Tab Filter Controls
 */
@Composable
private fun VehicleFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeTab: VehicleFilterTab,
    onTabSelected: (VehicleFilterTab) -> Unit,
    activeCount: Int,
    archivedCount: Int,
    totalCount: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar
        FuelTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = "PENCARIAN KENDARAAN",
            placeholder = "Cari nama, plat nomor, atau merek...",
            leadingIcon = Icons.Default.Search,
            trailingIcon = if (searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus Pencarian",
                            tint = MariMutedLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null
        )

        // Filter Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Triple(VehicleFilterTab.ACTIVE, "Aktif", activeCount),
                Triple(VehicleFilterTab.ARCHIVED, "Diarsipkan", archivedCount),
                Triple(VehicleFilterTab.ALL, "Semua", totalCount)
            )

            tabs.forEach { (tab, label, count) ->
                val isSelected = activeTab == tab
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MariPrimaryBlue.copy(alpha = 0.12f) else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MariPrimaryBlue else MariBorderLight
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onTabSelected(tab) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MariPrimaryBlue else MariMutedLight
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MariPrimaryBlue else MariBorderLight
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) Color.White else MariMutedLight
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kartu Kendaraan Individual — Sesuai Desain Resmi Mari Partner Fuel
 */
@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicleIcon: ImageVector = when (vehicle.vehicleType) {
        VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
        VehicleType.CAR -> Icons.Default.DirectionsCar
        VehicleType.OTHER -> Icons.Default.DirectionsCar
    }

    FuelCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = 18.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Icon + Info Kendaraan + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MariPrimaryBlue.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vehicleIcon,
                        contentDescription = vehicle.vehicleType.displayName,
                        tint = MariPrimaryBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MariTextLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = buildString {
                            append(vehicle.licensePlate ?: "Tanpa Plat")
                            if (vehicle.productionYear != null) {
                                append(" • ${vehicle.productionYear}")
                            }
                            append(" • ${vehicle.vehicleType.displayName}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MariMutedLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (vehicle.isArchived) {
                    FuelStatusChip(text = "Diarsipkan", type = FuelChipType.Neutral)
                } else {
                    FuelStatusChip(text = "Aktif", type = FuelChipType.Success)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MariBorderLight)
            Spacer(modifier = Modifier.height(12.dp))

            // Grid Spesifikasi: BBM Favorit, Odometer, Kapasitas Tangki
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BBM Favorit
                SpecificationBadge(
                    icon = Icons.Default.LocalGasStation,
                    label = "BBM Favorit",
                    value = vehicle.defaultFuelType ?: "Pertalite",
                    modifier = Modifier.weight(1f)
                )

                // Odometer
                SpecificationBadge(
                    icon = Icons.Default.Speed,
                    label = "Odometer",
                    value = if (vehicle.lastOdometerKm != null && vehicle.lastOdometerKm >= 0) {
                        "${vehicle.lastOdometerKm.toLong()} km"
                    } else "-",
                    modifier = Modifier.weight(1f)
                )

                // Kapasitas Tangki (jika ada)
                if (vehicle.tankCapacityLiters != null && vehicle.tankCapacityLiters > 0) {
                    SpecificationBadge(
                        icon = Icons.Default.Info,
                        label = "Tangki",
                        value = "${vehicle.tankCapacityLiters} L",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Catatan jika ada
            if (!vehicle.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MariBorderLight.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MariMutedLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = vehicle.notes,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MariMutedLight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MariBorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row (Edit, Arsip, Hapus)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (vehicle.isArchived) "Tidak aktif untuk transaksi baru" else "Siap digunakan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MariMutedLight
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Edit
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Kendaraan",
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Tombol Arsipkan / Aktifkan
                    IconButton(onClick = onArchive) {
                        Icon(
                            imageVector = if (vehicle.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = if (vehicle.isArchived) "Aktifkan" else "Arsipkan",
                            tint = MariMutedLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Tombol Hapus Permanen
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Kendaraan",
                            tint = MariDanger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ubin Spesifikasi Kecil di dalam Kartu Kendaraan
 */
@Composable
private fun SpecificationBadge(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MariBorderLight.copy(alpha = 0.35f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MariPrimaryBlue,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MariMutedLight
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = MariTextLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Kartu Tambah Kendaraan Baru (Dashed / Outline Style di akhir daftar)
 */
@Composable
private fun AddVehicleOutlineCard(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MariPrimaryBlue.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MariPrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MariPrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tambah Kendaraan Baru",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MariPrimaryBlue
                    )
                )
                Text(
                    text = "Catat kendaraan agar transaksi BBM lebih terorganisir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MariMutedLight
                )
            }
        }
    }
}
