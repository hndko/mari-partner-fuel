package id.maripartner.fuel.feature.reminders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelErrorState
import id.maripartner.fuel.core.designsystem.component.FuelLoadingState
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.FuelTextField
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.theme.MariAiTag
import id.maripartner.fuel.core.designsystem.theme.MariAiTagBg
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariDangerBg
import id.maripartner.fuel.core.designsystem.theme.MariInfo
import id.maripartner.fuel.core.designsystem.theme.MariInfoBg
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import id.maripartner.fuel.core.designsystem.theme.MariSuccessBg
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.core.designsystem.theme.MariWarning
import id.maripartner.fuel.core.designsystem.theme.MariWarningBg
import id.maripartner.fuel.domain.model.MaintenanceReminder
import id.maripartner.fuel.domain.model.ReminderDueStatus
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Layar Pengingat & Servis Perawatan Kendaraan — Standar Desain Resmi Mari Partner Fuel
 */
@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: RemindersViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var reminderToDelete by remember { mutableStateOf<MaintenanceReminder?>(null) }

    // Selalu perbarui data saat layar aktif
    LaunchedEffect(Unit) {
        viewModel.onEvent(RemindersUiEvent.Refresh)
    }

    LaunchedEffect(uiState.actionSuccessMessage) {
        uiState.actionSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(RemindersUiEvent.ClearSuccessMessage)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(RemindersUiEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Pengingat & Perawatan",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MariPrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Pengingat Servis"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                when (val state = uiState.remindersState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            FuelLoadingState(message = "Memuat data pengingat servis...")
                        }
                    }

                    is UiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            FuelErrorState(
                                message = state.message,
                                onRetry = { viewModel.loadVehiclesAndReminders(isRefresh = true) }
                            )
                        }
                    }

                    is UiState.Empty -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            FuelEmptyState(
                                title = "Belum Ada Pengingat Servis",
                                subtitle = "Buat jadwal ganti oli mesin, servis berkala, atau ganti kampas rem agar kendaraan selalu prima.",
                                icon = Icons.Default.Build,
                                actionButtonText = "Tambah Pengingat Pertama",
                                onActionClick = onNavigateToCreate
                            )
                        }
                    }

                    is UiState.Success -> {
                        val activeReminders = uiState.allReminders.filter { !it.isCompleted }
                        val completedReminders = uiState.allReminders.filter { it.isCompleted }

                        val overdueCount = activeReminders.count {
                            uiState.dueStatusMap[it.id] == ReminderDueStatus.OVERDUE
                        }
                        val dueSoonCount = activeReminders.count {
                            uiState.dueStatusMap[it.id] == ReminderDueStatus.DUE_SOON
                        }

                        // Filter berdasarkan Tab
                        val listByTab = if (uiState.selectedTab == ReminderTab.ACTIVE) activeReminders else completedReminders

                        // Filter berdasarkan Status Filter (hanya saat tab Aktif)
                        val listByStatus = if (uiState.selectedTab == ReminderTab.ACTIVE) {
                            when (uiState.selectedFilterStatus) {
                                ReminderFilterStatus.ALL -> listByTab
                                ReminderFilterStatus.DUE_SOON -> listByTab.filter {
                                    uiState.dueStatusMap[it.id] == ReminderDueStatus.DUE_SOON
                                }
                                ReminderFilterStatus.OVERDUE -> listByTab.filter {
                                    uiState.dueStatusMap[it.id] == ReminderDueStatus.OVERDUE
                                }
                            }
                        } else {
                            listByTab
                        }

                        // Filter berdasarkan Pencarian
                        val filteredList = if (uiState.searchQuery.isBlank()) {
                            listByStatus
                        } else {
                            val q = uiState.searchQuery.trim().lowercase(Locale.ROOT)
                            listByStatus.filter { reminder ->
                                val vName = uiState.vehiclesMap[reminder.vehicleId]?.name.orEmpty().lowercase(Locale.ROOT)
                                val vPlate = uiState.vehiclesMap[reminder.vehicleId]?.licensePlate.orEmpty().lowercase(Locale.ROOT)
                                reminder.title.lowercase(Locale.ROOT).contains(q) ||
                                        reminder.notes.orEmpty().lowercase(Locale.ROOT).contains(q) ||
                                        vName.contains(q) ||
                                        vPlate.contains(q)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Header Banner & KPI Summary
                            item(key = "kpi_header") {
                                RemindersHeaderKpi(
                                    totalActive = activeReminders.size,
                                    dueSoonCount = dueSoonCount,
                                    overdueCount = overdueCount,
                                    completedCount = completedReminders.size,
                                    onRefresh = { viewModel.onEvent(RemindersUiEvent.Refresh) },
                                    isRefreshing = uiState.isRefreshing
                                )
                            }

                            // 2. Tab Segmented Control (Aktif vs Selesai)
                            item(key = "tabs_section") {
                                RemindersTabSelector(
                                    selectedTab = uiState.selectedTab,
                                    activeCount = activeReminders.size,
                                    completedCount = completedReminders.size,
                                    onSelectTab = { viewModel.onEvent(RemindersUiEvent.SelectTab(it)) }
                                )
                            }

                            // 3. Search Bar
                            item(key = "search_bar") {
                                FuelTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.onEvent(RemindersUiEvent.SearchQueryChanged(it)) },
                                    label = "Cari pengingat servis atau nama kendaraan...",
                                    placeholder = "misal: Oli Mesin, Avanza, Rem",
                                    leadingIcon = Icons.Default.Search,
                                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                                        {
                                            IconButton(onClick = { viewModel.onEvent(RemindersUiEvent.SearchQueryChanged("")) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Hapus pencarian",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    } else null,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // 4. Sub-Filter Pills (saat tab aktif)
                            if (uiState.selectedTab == ReminderTab.ACTIVE) {
                                item(key = "status_filters") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (status in ReminderFilterStatus.entries) {
                                            val isSelected = uiState.selectedFilterStatus == status
                                            val count = when (status) {
                                                ReminderFilterStatus.ALL -> activeReminders.size
                                                ReminderFilterStatus.DUE_SOON -> dueSoonCount
                                                ReminderFilterStatus.OVERDUE -> overdueCount
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = if (isSelected) MariPrimaryBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                border = if (isSelected) null else BorderStroke(1.dp, MariBorderLight),
                                                modifier = Modifier.clickable {
                                                    viewModel.onEvent(RemindersUiEvent.SelectFilterStatus(status))
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = status.label,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                                                    ) {
                                                        Text(
                                                            text = "$count",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 10.sp
                                                            ),
                                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Daftar Kartu Pengingat
                            if (filteredList.isEmpty()) {
                                item(key = "empty_filter_result") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(56.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Build,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (uiState.selectedTab == ReminderTab.ACTIVE) "Tidak Ada Jadwal Servis Aktif" else "Belum Ada Riwayat Selesai",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (uiState.searchQuery.isNotBlank()) "Tidak ditemukan hasil untuk '${uiState.searchQuery}'" else "Jadwal servis kendaraan Anda saat ini kosong.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (uiState.searchQuery.isNotBlank() || uiState.selectedFilterStatus != ReminderFilterStatus.ALL) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                FuelSecondaryButton(
                                                    text = "Reset Filter & Pencarian",
                                                    onClick = {
                                                        viewModel.onEvent(RemindersUiEvent.SearchQueryChanged(""))
                                                        viewModel.onEvent(RemindersUiEvent.SelectFilterStatus(ReminderFilterStatus.ALL))
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(filteredList, key = { it.id }) { reminder ->
                                    val dueStatus = uiState.dueStatusMap[reminder.id] ?: ReminderDueStatus.UPCOMING
                                    val vehicle = uiState.vehiclesMap[reminder.vehicleId]
                                    ReminderCardItem(
                                        reminder = reminder,
                                        dueStatus = dueStatus,
                                        vehicle = vehicle,
                                        onToggleComplete = {
                                            viewModel.onEvent(
                                                RemindersUiEvent.ToggleComplete(reminder.id, !reminder.isCompleted)
                                            )
                                        },
                                        onEdit = { onNavigateToEdit(reminder.id) },
                                        onDelete = { reminderToDelete = reminder }
                                    )
                                }
                            }

                            // Spacer padding bawah agar tidak tertutup FAB
                            item(key = "bottom_spacer") {
                                Spacer(modifier = Modifier.height(64.dp))
                            }
                        }
                    }

                    is UiState.Idle -> Unit
                }
            }
        }
    }

    // Modal Dialog Konfirmasi Hapus SweetAlert
    reminderToDelete?.let { reminder ->
        FuelSweetAlertDialog(
            show = true,
            type = SweetAlertType.WARNING,
            title = "Hapus Pengingat Servis?",
            message = "Pengingat '${reminder.title}' akan dihapus secara permanen dari sistem dan alarm pengingat otomatis akan dibatalkan.",
            confirmText = "Hapus Permanen",
            dismissText = "Batal",
            onConfirm = {
                viewModel.onEvent(RemindersUiEvent.DeleteReminder(reminder.id))
                reminderToDelete = null
            },
            onDismiss = { reminderToDelete = null }
        )
    }
}

/**
 * Header KPI Summary & Refresh Button
 */
@Composable
private fun RemindersHeaderKpi(
    totalActive: Int,
    dueSoonCount: Int,
    overdueCount: Int,
    completedCount: Int,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MariBrandNavy,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MariPrimaryBlue.copy(alpha = 0.25f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "SERVIS & PERAWATAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFF67B0F8)
                        )
                        Text(
                            text = "Jadwal Perawatan Berkala",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat Ulang",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 KPI Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiPillItem(
                    label = "Aktif",
                    value = "$totalActive",
                    accentColor = Color.White,
                    bgColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
                KpiPillItem(
                    label = "Segera",
                    value = "$dueSoonCount",
                    accentColor = MariWarning,
                    bgColor = MariWarningBg.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                )
                KpiPillItem(
                    label = "Perlu Servis",
                    value = "$overdueCount",
                    accentColor = MariDanger,
                    bgColor = MariDangerBg.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                )
                KpiPillItem(
                    label = "Selesai",
                    value = "$completedCount",
                    accentColor = MariSuccess,
                    bgColor = MariSuccessBg.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KpiPillItem(
    label: String,
    value: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Tab Row Segmented (Aktif vs Selesai)
 */
@Composable
private fun RemindersTabSelector(
    selectedTab: ReminderTab,
    activeCount: Int,
    completedCount: Int,
    onSelectTab: (ReminderTab) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MariBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (tab in ReminderTab.entries) {
                val isSelected = selectedTab == tab
                val count = if (tab == ReminderTab.ACTIVE) activeCount else completedCount

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(tab) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MariPrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MariPrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) MariPrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kartu Item Pengingat Servis Perawatan
 */
@Composable
private fun ReminderCardItem(
    reminder: MaintenanceReminder,
    dueStatus: ReminderDueStatus,
    vehicle: Vehicle?,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val vehicleName = vehicle?.name ?: reminder.vehicleName ?: "Kendaraan"
    val vehiclePlate = vehicle?.licensePlate ?: reminder.vehiclePlate ?: "-"
    val currentOdo = vehicle?.lastOdometerKm

    val (chipType, chipText, chipIcon) = when (dueStatus) {
        ReminderDueStatus.OVERDUE -> Triple(FuelChipType.Danger, "Lewat Jatuh Tempo", Icons.Default.WarningAmber)
        ReminderDueStatus.DUE_SOON -> Triple(FuelChipType.Warning, "Segera Jatuh Tempo", Icons.Default.Info)
        ReminderDueStatus.UPCOMING -> Triple(FuelChipType.Info, "Terjadwal", Icons.AutoMirrored.Filled.EventNote)
        ReminderDueStatus.COMPLETED -> Triple(FuelChipType.Success, "Selesai", Icons.Default.CheckCircle)
    }

    FuelCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Kartu: Checkbox Icon + Judul + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Tactile Toggle Circle Button
                Surface(
                    shape = CircleShape,
                    color = if (reminder.isCompleted) MariSuccessBg else Color.Transparent,
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (reminder.isCompleted) MariSuccess else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onToggleComplete() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (reminder.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Tandai aktif kembali",
                                tint = MariSuccess,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Tandai selesai",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        FuelStatusChip(
                            text = chipText,
                            type = chipType,
                            icon = chipIcon
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Vehicle Tag Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (vehicle?.vehicleType == VehicleType.MOTORCYCLE) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$vehicleName ($vehiclePlate)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = MariPrimaryBlue
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.8.dp,
                color = MariBorderLight.copy(alpha = 0.7f)
            )

            // Trigger Details: Tanggal & Odometer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tanggal Trigger
                if (reminder.hasDateTrigger && reminder.targetDate != null) {
                    val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), reminder.targetDate)
                    val dateRelativeText = when {
                        daysUntil < 0 -> "Lewat ${-daysUntil} hari yang lalu"
                        daysUntil == 0L -> "Hari ini!"
                        daysUntil == 1L -> "Besok"
                        else -> "$daysUntil hari lagi"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MariPrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Jatuh Tempo:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = DateUtils.formatLocalDate(reminder.targetDate),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (!reminder.isCompleted) {
                            Text(
                                text = dateRelativeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (daysUntil <= 0) MariDanger else if (daysUntil <= 7) MariWarning else MariPrimaryBlue
                            )
                        }
                    }
                }

                // Odometer Trigger
                if (reminder.hasOdometerTrigger && reminder.targetOdometerKm != null) {
                    val targetKm = reminder.targetOdometerKm
                    val diffKm = currentOdo?.let { targetKm - it }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MariPrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Target Km:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format(Locale.US, "%,.0f", targetKm).replace(',', '.')} km",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (currentOdo != null && !reminder.isCompleted) {
                            val diffText = if (diffKm != null && diffKm <= 0) {
                                "Lewat ${String.format(Locale.US, "%,.0f", -diffKm).replace(',', '.')} km"
                            } else if (diffKm != null) {
                                "Sisa ${String.format(Locale.US, "%,.0f", diffKm).replace(',', '.')} km"
                            } else ""

                            Text(
                                text = diffText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (diffKm != null && diffKm <= 0) MariDanger else if (diffKm != null && diffKm <= 500) MariWarning else MariPrimaryBlue
                            )
                        }
                    }

                    if (currentOdo != null) {
                        Text(
                            text = "(Odometer kendaraan saat ini: ${String.format(Locale.US, "%,.0f", currentOdo).replace(',', '.')} km)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            // Catatan Tambahan jika ada
            if (!reminder.notes.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = reminder.notes,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Row: Toggle button label & Edit / Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (reminder.isCompleted) "Selesai dirawat" else "Belum dirawat",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (reminder.isCompleted) MariSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Pengingat",
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Pengingat",
                            tint = MariDanger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
