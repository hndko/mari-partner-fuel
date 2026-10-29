package id.maripartner.fuel.feature.reminders

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.domain.model.ReminderType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFormScreen(
    reminderId: String?,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: RemindersViewModel,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsState()
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(reminderId) {
        viewModel.initFormForEdit(reminderId)
    }

    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = if (reminderId.isNullOrBlank()) "Tambah Pengingat" else "Edit Pengingat",
                onNavigateBack = onNavigateBack
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Pemilihan Kendaraan
            FuelCard {
                Text(
                    text = "Kendaraan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val selectedVehicle = formState.availableVehicles.firstOrNull { it.id == formState.vehicleId }

                ExposedDropdownMenuBox(
                    expanded = vehicleDropdownExpanded,
                    onExpandedChange = { vehicleDropdownExpanded = !vehicleDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedVehicle?.name ?: "Pilih Kendaraan",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Pilih Kendaraan") }
                    )
                    ExposedDropdownMenu(
                        expanded = vehicleDropdownExpanded,
                        onDismissRequest = { vehicleDropdownExpanded = false }
                    ) {
                        formState.availableVehicles.forEach { v ->
                            DropdownMenuItem(
                                text = { Text("${v.name} (${v.licensePlate ?: "-"})") },
                                onClick = {
                                    viewModel.onFormEvent(ReminderFormUiEvent.VehicleSelected(v.id))
                                    vehicleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. Detail Pengingat
            FuelCard {
                Text(
                    text = "Informasi Perawatan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Suggestion chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Ganti Oli Mesin", "Servis Berkala", "Cek Rem & Kampas", "Ganti Busi", "Rotasi Ban").forEach { s ->
                        AssistChip(
                            onClick = { viewModel.onFormEvent(ReminderFormUiEvent.TitleChanged(s)) },
                            label = { Text(s) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = { viewModel.onFormEvent(ReminderFormUiEvent.TitleChanged(it)) },
                    label = { Text("Nama Pengingat / Jenis Servis") },
                    placeholder = { Text("misal: Ganti Oli Mesin 10W-40") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Pemicu Jatuh Tempo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReminderType.entries.forEach { type ->
                        FilterChip(
                            selected = formState.reminderType == type,
                            onClick = { viewModel.onFormEvent(ReminderFormUiEvent.ReminderTypeChanged(type)) },
                            label = { Text(type.label) }
                        )
                    }
                }

                // Input Tanggal jika DATE atau BOTH
                if (formState.reminderType == ReminderType.DATE || formState.reminderType == ReminderType.BOTH) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = formState.targetDate?.toString().orEmpty(),
                        onValueChange = { dateStr ->
                            try {
                                val d = LocalDate.parse(dateStr.trim())
                                viewModel.onFormEvent(ReminderFormUiEvent.TargetDateChanged(d))
                            } catch (e: Exception) {
                                // input sedang diketik
                            }
                        },
                        label = { Text("Target Tanggal (YYYY-MM-DD)") },
                        placeholder = { Text("misal: 2026-11-15") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Input Odometer jika ODOMETER atau BOTH
                if (formState.reminderType == ReminderType.ODOMETER || formState.reminderType == ReminderType.BOTH) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = formState.targetOdometerStr,
                        onValueChange = { viewModel.onFormEvent(ReminderFormUiEvent.TargetOdometerChanged(it)) },
                        label = { Text("Target Odometer Kilometer") },
                        placeholder = { Text("misal: 15000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = { viewModel.onFormEvent(ReminderFormUiEvent.NotesChanged(it)) },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    placeholder = { Text("misal: Oli merk Motul 1 liter dan ganti filter oli") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Pesan Error jika ada
            formState.errorMessage?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Tombol Simpan
            FuelPrimaryButton(
                text = if (reminderId.isNullOrBlank()) "Simpan Pengingat" else "Perbarui Pengingat",
                onClick = { viewModel.onFormEvent(ReminderFormUiEvent.SaveReminder) },
                isLoading = formState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
