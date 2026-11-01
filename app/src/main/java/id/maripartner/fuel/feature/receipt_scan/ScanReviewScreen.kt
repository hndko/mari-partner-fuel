package id.maripartner.fuel.feature.receipt_scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.FuelTextField
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.component.ThousandSeparatorVisualTransformation
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess

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
        ?: if (state.availableVehicles.isEmpty()) "Kendaraan belum tersedia" else "Pilih Kendaraan"

    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                MariBrandNavy,
                MariPrimaryBlue.copy(alpha = 0.95f),
                MariPrimaryBlue
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val screenHeight = maxHeight
            val density = LocalDensity.current
            var headerHeightDp by remember { mutableStateOf(160.dp) }

            // Latar gradien atas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(brush = gradientBrush)
            )

            val remainingCardMinHeight = (screenHeight - headerHeightDp + 36.dp).coerceAtLeast(320.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Hero Gradient Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            val measuredDp = with(density) { size.height.toDp() }
                            if (measuredDp > 0.dp) {
                                headerHeightDp = measuredDp
                            }
                        }
                        .background(brush = gradientBrush)
                        .statusBarsPadding()
                        .padding(top = 10.dp, bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.20f))
                                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Kembali",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Human-in-the-Loop",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.size(44.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Tinjau Hasil Scan Struk",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 22.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Periksa dan koreksi data sebelum disimpan ke database transaksi",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // 2. Elevated White Rounded Sheet Container (Formulir Review)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-18).dp)
                        .defaultMinSize(minHeight = remainingCardMinHeight),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = remainingCardMinHeight),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 680.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(
                                    top = 22.dp,
                                    bottom = innerPadding.calculateBottomPadding() + 24.dp
                                ),
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
                                text = "Foto Struk (Pemindaian Sementara)",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "Hanya diproses untuk membaca data struk. Foto tidak disimpan permanen di server demi privasi & efisiensi penyimpanan.",
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
                    if (state.availableVehicles.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Kendaraan belum tersedia", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                            onClick = { vehicleDropdownExpanded = false }
                        )
                    } else {
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
            }

            if (state.availableVehicles.size == 1) {
                Text(
                    text = "✓ Kendaraan utama aktif terpilih otomatis",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = MariSuccess,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            // 5. Total Pembayaran (Rp) (Format otomatis pemisah ribuan)
            val formattedDisplay = state.totalAmountRaw.toLongOrNull()?.let {
                CurrencyFormatter.formatIdr(it)
            } ?: "Rp 0"

            FuelTextField(
                value = state.totalAmountRaw,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    viewModel.onEvent(ReceiptScanUiEvent.TotalAmountChanged(clean))
                },
                label = "TOTAL PEMBAYARAN (RP) *",
                placeholder = "misal: 50.000",
                visualTransformation = ThousandSeparatorVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            if (state.totalAmountRaw.isNotBlank()) {
                Text(
                    text = "Terbaca: $formattedDisplay",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

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
            FuelTextField(
                value = state.odometerKmRaw,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    viewModel.onEvent(ReceiptScanUiEvent.OdometerChanged(clean))
                },
                label = "ODOMETER KENDARAAN (KM)",
                placeholder = "misal: 15.420",
                leadingIcon = Icons.Default.Speed,
                visualTransformation = ThousandSeparatorVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // 8.1 Nama Pengemudi / Shift
            FuelTextField(
                value = state.driverName,
                onValueChange = { viewModel.onEvent(ReceiptScanUiEvent.DriverNameChanged(it)) },
                label = "NAMA PENGEMUDI / SHIFT (OPSIONAL)",
                placeholder = "misal: Budi (Shift Pagi)",
                leadingIcon = Icons.Default.Badge,
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
}
}
}
}

    // Modal Persetujuan Penggunaan AI Cloud
    FuelSweetAlertDialog(
        show = state.showAiConsentDialog,
        type = SweetAlertType.INFO,
        title = "Gunakan Bantuan AI Cloud?",
        message = "Teks hasil pembacaan struk akan dikirimkan secara aman ke Supabase Edge Function untuk dianalisis oleh AI (Gemini 1.5 Flash).\n\n" +
            "• Privasi Terjamin: Nomor polisi dan identitas akun Anda TIDAK PERNAH dikirimkan.\n" +
            "• Verifikasi Manusia: Hasil pembacaan tetap wajib Anda periksa sebelum disimpan.\n" +
            "• Kuota Terhitung: Menggunakan 1 kuota scan AI bulanan dari paket Anda.",
        confirmText = "Kirim ke AI Cloud",
        dismissText = "Batal",
        onConfirm = { viewModel.onEvent(ReceiptScanUiEvent.ConfirmAiOptimization) },
        onDismiss = { viewModel.onEvent(ReceiptScanUiEvent.DismissAiConsent) }
    )

    // Modal Peringatan Odometer Mundur
    FuelSweetAlertDialog(
        show = state.showOdometerWarningDialog,
        type = SweetAlertType.WARNING,
        title = "Peringatan Odometer Mundur",
        message = "Odometer yang Anda masukkan (${state.pendingOdometerValue?.toLong()} km) lebih rendah dari catatan terakhir kendaraan (${state.latestRecordedOdometer?.toLong() ?: 0} km).\n\nApakah spidometer telah diganti/reset, atau ingin memperbaiki angka?",
        confirmText = "Tetap Simpan",
        dismissText = "Perbaiki Angka",
        onConfirm = { viewModel.onEvent(ReceiptScanUiEvent.ConfirmLowerOdometer) },
        onDismiss = { viewModel.onEvent(ReceiptScanUiEvent.DismissOdometerWarning) }
    )

    // Modal Peringatan Transaksi Serupa / Duplikat
    FuelSweetAlertDialog(
        show = state.showDuplicateWarningDialog,
        type = SweetAlertType.WARNING,
        title = "Peringatan Transaksi Serupa",
        message = state.duplicateWarningMessage ?: "Ditemukan transaksi serupa di waktu berdekatan. Apakah Anda yakin ingin tetap menyimpan?",
        confirmText = "Tetap Simpan",
        dismissText = "Periksa Kembali",
        onConfirm = { viewModel.onEvent(ReceiptScanUiEvent.ConfirmDuplicateSubmission) },
        onDismiss = { viewModel.onEvent(ReceiptScanUiEvent.DismissDuplicateWarning) }
    )
}
