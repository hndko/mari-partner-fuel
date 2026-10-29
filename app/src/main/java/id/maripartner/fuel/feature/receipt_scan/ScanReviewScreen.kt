package id.maripartner.fuel.feature.receipt_scan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReviewScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: ReceiptScanViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccessSaved) {
        if (state.isSuccessSaved) {
            onSaveSuccess()
        }
    }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReceiptScanUiEvent.DismissMessage)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReceiptScanUiEvent.DismissMessage)
        }
    }

    val selectedVehicleName = state.availableVehicles.firstOrNull { it.id == state.selectedVehicleId }?.name
        ?: if (state.availableVehicles.isEmpty()) "Memuat kendaraan..." else "Pilih Kendaraan"

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Tinjau Hasil Scan Struk",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // 1. Banner Info Status Hasil Scan & Human-in-the-Loop
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Konfirmasi Hasil Ekstraksi",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Silakan periksa kembali angka di bawah ini. Transaksi hanya akan disimpan setelah Anda mengonfirmasi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Pratinjau Thumbnail Foto Struk
            if (state.selectedBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = state.selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Foto Struk",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Foto Struk Terlampir",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "Akan disimpan secara privat di server Supabase",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Tombol Ekstraksi Bantuan AI Cloud (Dengan Konfirmasi Pengguna)
            OutlinedButton(
                onClick = { viewModel.onEvent(ReceiptScanUiEvent.RequestAiOptimization) },
                enabled = !state.isAiProcessing && !state.isSubmittingTransaction,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isAiProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Memproses via AI Cloud...")
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Optimalkan dengan AI Cloud (Gemini)")
                }
            }

            // 4. Pemilih Kendaraan Aktif
            ExposedDropdownMenuBox(
                expanded = vehicleDropdownExpanded,
                onExpandedChange = { vehicleDropdownExpanded = !vehicleDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedVehicleName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kendaraan *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )

                ExposedDropdownMenu(
                    expanded = vehicleDropdownExpanded,
                    onDismissRequest = { vehicleDropdownExpanded = false }
                ) {
                    state.availableVehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text("${vehicle.name} (${vehicle.displaySubtitle})") },
                            onClick = {
                                viewModel.onEvent(ReceiptScanUiEvent.VehicleSelected(vehicle.id))
                                vehicleDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 5. Total Pembayaran (Rp) (Format otomatis Rupiah)
            val formattedDisplay = state.totalAmountRaw.toLongOrNull()?.let {
                CurrencyFormatter.formatIdr(it)
            } ?: "Rp 0"

            OutlinedTextField(
                value = state.totalAmountRaw,
                onValueChange = { viewModel.onEvent(ReceiptScanUiEvent.TotalAmountChanged(it)) },
                label = { Text("Total Pembayaran (Rp) *") },
                placeholder = { Text("misal: 50000") },
                supportingText = {
                    if (state.totalAmountRaw.isNotBlank()) {
                        Text("Terformat: $formattedDisplay")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 6. Volume Liter & Harga per Liter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.volumeLitersRaw,
                    onValueChange = { viewModel.onEvent(ReceiptScanUiEvent.VolumeLitersChanged(it)) },
                    label = { Text("Volume (Liter)") },
                    placeholder = { Text("misal: 3.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.pricePerLiterRaw,
                    onValueChange = { viewModel.onEvent(ReceiptScanUiEvent.PricePerLiterChanged(it)) },
                    label = { Text("Harga / Liter") },
                    placeholder = { Text("misal: 10000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Peringatan Selisih Nominal Volume x Harga
            state.discrepancyWarning?.let { warning ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // 7. Jenis BBM & SPBU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.fuelType,
                    onValueChange = { viewModel.onEvent(ReceiptScanUiEvent.FuelTypeChanged(it)) },
                    label = { Text("Jenis BBM") },
                    placeholder = { Text("Pertalite") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.stationName,
                    onValueChange = { viewModel.onEvent(ReceiptScanUiEvent.StationNameChanged(it)) },
                    label = { Text("SPBU / Lokasi") },
                    placeholder = { Text("SPBU Pertamina 31.xxx") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // 8. Odometer
            OutlinedTextField(
                value = state.odometerKmRaw,
                onValueChange = { viewModel.onEvent(ReceiptScanUiEvent.OdometerChanged(it)) },
                label = { Text("Odometer Kendaraan (km)") },
                placeholder = { Text("misal: 15420") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 9. Catatan Tambahan
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.onEvent(ReceiptScanUiEvent.NotesChanged(it)) },
                label = { Text("Catatan Tambahan (Opsional)") },
                placeholder = { Text("misal: Pengisian saat tugas kantor") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 10. Tombol Konfirmasi dan Simpan (Protected against double submit)
            FuelPrimaryButton(
                text = "Konfirmasi & Simpan Transaksi",
                onClick = { viewModel.onEvent(ReceiptScanUiEvent.ConfirmAndSave) },
                isLoading = state.isSubmittingTransaction,
                enabled = !state.isSubmittingTransaction && state.selectedVehicleId.isNotBlank() && state.totalAmountRaw.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Dialog Persetujuan Penggunaan AI Cloud
    if (state.showAiConsentDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ReceiptScanUiEvent.DismissAiConsent) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gunakan Bantuan AI Cloud?")
                }
            },
            text = {
                Text(
                    "Teks hasil pembacaan struk akan dikirimkan secara aman ke Supabase Edge Function untuk dianalisis oleh AI (Gemini 1.5 Flash).\n\n" +
                    "Privasi Terjamin: Nomor polisi kendaraan dan identitas pribadi akun Anda TIDAK PERNAH dikirimkan ke pihak ketiga."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(ReceiptScanUiEvent.ConfirmAiOptimization) }) {
                    Text("Kirim ke AI Cloud")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ReceiptScanUiEvent.DismissAiConsent) }) {
                    Text("Batal")
                }
            }
        )
    }
}
