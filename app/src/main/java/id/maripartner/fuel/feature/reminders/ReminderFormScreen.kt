package id.maripartner.fuel.feature.reminders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.FuelTextField
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariDangerBg
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.core.designsystem.theme.MariWarning
import id.maripartner.fuel.core.designsystem.theme.MariWarningBg
import id.maripartner.fuel.domain.model.ReminderType
import id.maripartner.fuel.domain.model.VehicleType
import java.time.LocalDate
import java.util.Locale

private val QUICK_SERVICE_PRESETS = listOf(
    "Ganti Oli Mesin",
    "Servis Berkala",
    "Cek Rem & Kampas",
    "Ganti Busi",
    "Rotasi Ban",
    "Filter Udara",
    "Kuras Radiator"
)

/**
 * Layar Tambah / Edit Pengingat Servis Perawatan — Desain Resmi Mari Partner Fuel
 */
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
    val snackbarHostState = remember { SnackbarHostState() }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }

    val isEditMode = !reminderId.isNullOrBlank()

    LaunchedEffect(reminderId) {
        viewModel.initFormForEdit(reminderId)
    }

    LaunchedEffect(formState.errorMessage) {
        formState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onFormEvent(ReminderFormUiEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = if (isEditMode) "Edit Pengingat Servis" else "Tambah Pengingat Servis",
                onNavigateBack = onNavigateBack
            )
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
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // 1. KENDARAAN TERPILIH
                // ==========================================
                FuelCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "KENDARAAN *",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MariPrimaryBlue
                        )

                        val selectedVehicle = formState.availableVehicles.firstOrNull { it.id == formState.vehicleId }

                        if (formState.availableVehicles.isEmpty()) {
                            // Tampilan Ramah jika Kendaraan Belum Ada
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MariWarningBg,
                                border = BorderStroke(1.dp, MariWarning.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MariWarning.copy(alpha = 0.15f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.WarningAmber,
                                                contentDescription = null,
                                                tint = MariWarning,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Kendaraan belum tersedia",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MariWarning
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Tambahkan kendaraan terlebih dahulu di Garasi sebelum membuat pengingat servis.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            // Dropdown Pemilihan Kendaraan
                            ExposedDropdownMenuBox(
                                expanded = vehicleDropdownExpanded,
                                onExpandedChange = { vehicleDropdownExpanded = !vehicleDropdownExpanded }
                            ) {
                                val vehicleDisplayName = selectedVehicle?.let {
                                    "${it.name} (${it.licensePlate ?: "-"})"
                                } ?: "Pilih Kendaraan"

                                FuelTextField(
                                    value = vehicleDisplayName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = "PILIH KENDARAAN",
                                    leadingIcon = if (selectedVehicle?.vehicleType == VehicleType.MOTORCYCLE) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleDropdownExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = vehicleDropdownExpanded,
                                    onDismissRequest = { vehicleDropdownExpanded = false }
                                ) {
                                    formState.availableVehicles.forEach { vehicle ->
                                        DropdownMenuItem(
                                            text = {
                                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                    Text(
                                                        text = vehicle.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Text(
                                                        text = "Plat: ${vehicle.licensePlate ?: "-"} • Odometer: ${vehicle.lastOdometerKm?.let { String.format(Locale.US, "%,.0f", it).replace(',', '.') } ?: "0"} km",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (vehicle.vehicleType == VehicleType.MOTORCYCLE) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    tint = MariPrimaryBlue
                                                )
                                            },
                                            onClick = {
                                                viewModel.onFormEvent(ReminderFormUiEvent.VehicleSelected(vehicle.id))
                                                vehicleDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Info Odometer Terkini Kendaraan
                            selectedVehicle?.lastOdometerKm?.let { lastKm ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = MariPrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Odometer kendaraan saat ini: ${String.format(Locale.US, "%,.0f", lastKm).replace(',', '.')} km",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 2. INFORMASI PERAWATAN
                // ==========================================
                FuelCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "INFORMASI SERVIS *",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MariPrimaryBlue
                        )

                        // Quick Suggestion Chips
                        Text(
                            text = "Pilih Cepat Rekomendasi Servis:",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QUICK_SERVICE_PRESETS.forEach { preset ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MariBorderLight),
                                    modifier = Modifier.clickable {
                                        viewModel.onFormEvent(ReminderFormUiEvent.TitleChanged(preset))
                                    }
                                ) {
                                    Text(
                                        text = preset,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Input Nama Servis
                        FuelTextField(
                            value = formState.title,
                            onValueChange = { viewModel.onFormEvent(ReminderFormUiEvent.TitleChanged(it)) },
                            label = "NAMA PENGINGAT / SERVIS *",
                            placeholder = "misal: Ganti Oli Mesin 10W-40",
                            leadingIcon = Icons.Default.Build,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ==========================================
                // 3. PEMICU JATUH TEMPO
                // ==========================================
                FuelCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "PEMICU JATUH TEMPO *",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MariPrimaryBlue
                        )

                        // Segmented Button Pilihan Tipe Pemicu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderType.entries.forEach { type ->
                                val isSelected = formState.reminderType == type
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MariPrimaryBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = if (isSelected) null else BorderStroke(1.dp, MariBorderLight),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.onFormEvent(ReminderFormUiEvent.ReminderTypeChanged(type))
                                        }
                                ) {
                                    Text(
                                        text = type.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.8.dp,
                            color = MariBorderLight.copy(alpha = 0.6f)
                        )

                        // Input Tanggal (jika DATE atau BOTH)
                        if (formState.reminderType == ReminderType.DATE || formState.reminderType == ReminderType.BOTH) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                FuelTextField(
                                    value = formState.targetDate?.toString().orEmpty(),
                                    onValueChange = { dateStr ->
                                        try {
                                            val d = LocalDate.parse(dateStr.trim())
                                            viewModel.onFormEvent(ReminderFormUiEvent.TargetDateChanged(d))
                                        } catch (e: Exception) {
                                            // Pengguna sedang mengetik format
                                        }
                                    },
                                    label = "TARGET TANGGAL JATUH TEMPO (YYYY-MM-DD) *",
                                    placeholder = "misal: 2026-11-20",
                                    leadingIcon = Icons.Default.CalendarToday,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Preset Tanggal Cepat
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "+1 Bulan" to 1L,
                                        "+3 Bulan" to 3L,
                                        "+6 Bulan" to 6L,
                                        "+1 Tahun" to 12L
                                    ).forEach { (label, months) ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = BorderStroke(0.8.dp, MariBorderLight),
                                            modifier = Modifier.clickable {
                                                val target = LocalDate.now().plusMonths(months)
                                                viewModel.onFormEvent(ReminderFormUiEvent.TargetDateChanged(target))
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp),
                                                color = MariPrimaryBlue,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Input Odometer (jika ODOMETER atau BOTH)
                        if (formState.reminderType == ReminderType.ODOMETER || formState.reminderType == ReminderType.BOTH) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                FuelTextField(
                                    value = formState.targetOdometerStr,
                                    onValueChange = { viewModel.onFormEvent(ReminderFormUiEvent.TargetOdometerChanged(it)) },
                                    label = "TARGET ODOMETER KILOMETER *",
                                    placeholder = "misal: 25000",
                                    leadingIcon = Icons.Default.Speed,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                val selectedVehicle = formState.availableVehicles.firstOrNull { it.id == formState.vehicleId }
                                val currentOdo = selectedVehicle?.lastOdometerKm ?: 0.0

                                // Preset Offset Odometer Cepat
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(1000.0, 2500.0, 5000.0, 10000.0).forEach { offset ->
                                        val targetKm = currentOdo + offset
                                        val label = "+${String.format(Locale.US, "%,.0f", offset).replace(',', '.')} km"

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = BorderStroke(0.8.dp, MariBorderLight),
                                            modifier = Modifier.clickable {
                                                viewModel.onFormEvent(
                                                    ReminderFormUiEvent.TargetOdometerChanged(String.format(Locale.US, "%.0f", targetKm))
                                                )
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp),
                                                color = MariPrimaryBlue,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. CATATAN TAMBAHAN
                // ==========================================
                FuelCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CATATAN TAMBAHAN (OPSIONAL)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MariPrimaryBlue
                        )

                        FuelTextField(
                            value = formState.notes,
                            onValueChange = { viewModel.onFormEvent(ReminderFormUiEvent.NotesChanged(it)) },
                            label = "Spesifikasi oli, bengkel langganan, suku cadang...",
                            placeholder = "misal: Oli Motul 1 liter + ganti filter oli mesin",
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Pesan Error jika ada
                formState.errorMessage?.let { err ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MariDangerBg,
                        border = BorderStroke(1.dp, MariDanger.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = MariDanger,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = err,
                                color = MariDanger,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                // ==========================================
                // 5. TOMBOL AKSI
                // ==========================================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FuelPrimaryButton(
                        text = if (isEditMode) "Perbarui Pengingat Servis" else "Simpan Jadwal Pengingat",
                        onClick = { viewModel.onFormEvent(ReminderFormUiEvent.SaveReminder) },
                        isLoading = formState.isLoading,
                        enabled = !formState.isLoading && formState.availableVehicles.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    FuelSecondaryButton(
                        text = "Batal",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Modal Alert Berhasil Simpan SweetAlert
    FuelSweetAlertDialog(
        show = formState.showSuccessDialog,
        type = SweetAlertType.SUCCESS,
        title = if (isEditMode) "Pengingat Diperbarui!" else "Pengingat Disimpan!",
        message = "Jadwal servis kendaraan Anda telah tersimpan dengan aman dan alarm notifikasi otomatis telah dijadwalkan.",
        confirmText = "Selesai",
        onConfirm = {
            viewModel.onFormEvent(ReminderFormUiEvent.DismissSuccessDialog)
            onSaved()
        }
    )
}
