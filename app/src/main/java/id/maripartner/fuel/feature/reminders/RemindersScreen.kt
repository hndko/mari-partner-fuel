package id.maripartner.fuel.feature.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelErrorState
import id.maripartner.fuel.core.designsystem.component.FuelLoadingState
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.domain.model.MaintenanceReminder
import id.maripartner.fuel.domain.model.ReminderDueStatus
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: RemindersViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var reminderToDelete by remember { mutableStateOf<MaintenanceReminder?>(null) }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Pengingat Perawatan",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Pengingat")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row (Aktif vs Selesai)
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                ReminderTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.onEvent(RemindersUiEvent.SelectTab(tab)) },
                        text = { Text(tab.label) }
                    )
                }
            }

            when (val state = uiState.remindersState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        FuelLoadingState(message = "Memuat jadwal pengingat...")
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        FuelErrorState(
                            message = state.message,
                            onRetry = { viewModel.loadVehiclesAndReminders() }
                        )
                    }
                }
                is UiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        FuelEmptyState(
                            title = "Belum Ada Pengingat",
                            subtitle = "Buat pengingat untuk ganti oli mesin, servis berkala, atau pemeriksaan rem kendaraan Anda.",
                            icon = Icons.Default.Build,
                            actionButtonText = "Buat Pengingat",
                            onActionClick = onNavigateToCreate
                        )
                    }
                }
                is UiState.Success -> {
                    val filteredList = uiState.allReminders.filter { reminder ->
                        if (uiState.selectedTab == ReminderTab.ACTIVE) !reminder.isCompleted else reminder.isCompleted
                    }

                    if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            FuelEmptyState(
                                title = if (uiState.selectedTab == ReminderTab.ACTIVE) "Tidak Ada Jadwal Aktif" else "Belum Ada Riwayat Selesai",
                                subtitle = if (uiState.selectedTab == ReminderTab.ACTIVE) "Semua perawatan kendaraan Anda saat ini telah selesai!" else "Pengingat yang ditandai selesai akan tampil di sini.",
                                icon = Icons.Default.Build
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredList, key = { it.id }) { reminder ->
                                val dueStatus = uiState.dueStatusMap[reminder.id] ?: ReminderDueStatus.UPCOMING
                                val vehicle = uiState.vehiclesMap[reminder.vehicleId]
                                ReminderItemCard(
                                    reminder = reminder,
                                    dueStatus = dueStatus,
                                    vehicleName = vehicle?.name ?: reminder.vehicleName ?: "Kendaraan",
                                    currentOdometer = vehicle?.lastOdometerKm,
                                    onToggleComplete = {
                                        viewModel.onEvent(RemindersUiEvent.ToggleComplete(reminder.id, !reminder.isCompleted))
                                    },
                                    onEdit = { onNavigateToEdit(reminder.id) },
                                    onDelete = { reminderToDelete = reminder }
                                )
                            }
                        }
                    }
                }
                is UiState.Idle -> Unit
            }
        }
    }

    // Dialog Konfirmasi Hapus
    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text("Hapus Pengingat?") },
            text = { Text("Apakah Anda yakin ingin menghapus pengingat '${reminder.title}'? Jadwal alarm notifikasi terkait akan dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(RemindersUiEvent.DeleteReminder(reminder.id))
                        reminderToDelete = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ReminderItemCard(
    reminder: MaintenanceReminder,
    dueStatus: ReminderDueStatus,
    vehicleName: String,
    currentOdometer: Double?,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID")) }

    val (badgeBg, badgeTextColor) = when (dueStatus) {
        ReminderDueStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        ReminderDueStatus.DUE_SOON -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        ReminderDueStatus.UPCOMING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        ReminderDueStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    FuelCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox Icon Toggle
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (reminder.isCompleted) "Tandai aktif" else "Tandai selesai",
                    tint = if (reminder.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = dueStatus.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = vehicleName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Detail Pemicu Odometer / Tanggal
                if (reminder.hasDateTrigger && reminder.targetDate != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jatuh tempo: ${reminder.targetDate.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (reminder.hasOdometerTrigger && reminder.targetOdometerKm != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val currentOdoText = currentOdometer?.let { " (saat ini ${String.format(Locale.US, "%.0f", it)} km)" } ?: ""
                        Text(
                            text = "Target: ${String.format(Locale.US, "%.0f", reminder.targetOdometerKm)} km$currentOdoText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                reminder.notes?.let { note ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
