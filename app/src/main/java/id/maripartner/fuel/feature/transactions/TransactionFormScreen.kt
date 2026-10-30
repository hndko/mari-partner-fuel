package id.maripartner.fuel.feature.transactions

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTextField
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    transactionId: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = viewModel()
) {
    val state by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var vehicleDropdownExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    viewModel.onEvent(TransactionFormEvent.ReceiptPicked(bytes, mime))
                }
            } catch (e: Exception) {
                // Tangani kegagalan pembacaan file
            }
        }
    }

    LaunchedEffect(transactionId) {
        if (!transactionId.isNullOrBlank()) {
            viewModel.loadTransaction(transactionId)
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(TransactionFormEvent.ClearError)
        }
    }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(TransactionFormEvent.DismissMessage)
        }
    }

    val selectedVehicle = remember(state.vehicleId, state.availableVehicles) {
        state.availableVehicles.firstOrNull { it.id == state.vehicleId }
    }

    val commonFuelTypes = listOf("Pertalite", "Pertamax", "Pertamax Turbo", "Solar", "Dexlite", "Shell Super")

    // Fungsi helper untuk membuka Date & Time Picker
    fun showDateTimePicker() {
        val currentDateTime = LocalDateTime.ofInstant(state.transactionAt, ZoneId.systemDefault())
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentDateTime.year)
            set(Calendar.MONTH, currentDateTime.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, currentDateTime.dayOfMonth)
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val picked = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                        viewModel.onEvent(
                            TransactionFormEvent.TransactionAtChanged(
                                picked.atZone(ZoneId.systemDefault()).toInstant()
                            )
                        )
                    },
                    currentDateTime.hour,
                    currentDateTime.minute,
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = if (state.isEditMode) "Edit Catatan BBM" else "Catat Pengisian BBM",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Visual Form Sesuai Reference
                Column {
                    Text(
                        text = if (state.isEditMode) "PERBARUI DATA" else "TRANSAKSI BARU",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MariPrimaryBlue
                    )
                    Text(
                        text = if (state.isEditMode) "Edit Pengisian BBM" else "Catat Pengisian BBM",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MariTextLight
                        )
                    )
                    Text(
                        text = "Masukkan data pengisian secara manual atau gunakan foto struk untuk membaca otomatis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MariMutedLight
                    )
                }

                // 2. Mode Switch (Input Manual vs Foto Struk)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MariBorderLight, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MariPrimaryBlue)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✎ Input Formulir",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✧ Scan via Foto",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MariTextLight
                        )
                    }
                }

                // 3. Panel Form Utama
                FuelCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // A. Pilih Kendaraan
                        Column {
                            Text(
                                text = "KENDARAAN *",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MariTextLight,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            ExposedDropdownMenuBox(
                                expanded = vehicleDropdownExpanded,
                                onExpandedChange = { vehicleDropdownExpanded = !vehicleDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedVehicle?.let { "${it.name} (${it.licensePlate})" } ?: "Pilih kendaraan",
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = MariPrimaryBlue
                                        )
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleDropdownExpanded) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MariPrimaryBlue,
                                        unfocusedBorderColor = MariBorderLight,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                )

                                ExposedDropdownMenu(
                                    expanded = vehicleDropdownExpanded,
                                    onDismissRequest = { vehicleDropdownExpanded = false }
                                ) {
                                    state.availableVehicles.forEach { vehicle ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = vehicle.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Text(
                                                        text = "${vehicle.licensePlate ?: ""} • ${vehicle.vehicleType.displayName}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MariMutedLight
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.onEvent(TransactionFormEvent.VehicleSelected(vehicle.id))
                                                vehicleDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            if (state.vehicleError != null) {
                                Text(
                                    text = state.vehicleError.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MariDanger,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                            }
                        }

                        // B. Tanggal & Waktu Pengisian
                        Column {
                            Text(
                                text = "TANGGAL & WAKTU PENGISIAN *",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MariTextLight,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MariBorderLight, RoundedCornerShape(12.dp))
                                    .clickable { showDateTimePicker() }
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MariPrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = DateUtils.formatDateTime(state.transactionAt),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MariTextLight
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Ubah",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MariPrimaryBlue
                                )
                            }
                        }

                        // C. Total Pembayaran (Rp)
                        val formattedDisplay = state.totalAmountRaw.toLongOrNull()?.let {
                            CurrencyFormatter.formatIdr(it)
                        } ?: "Rp 0"

                        FuelTextField(
                            value = state.totalAmountRaw,
                            onValueChange = { viewModel.onEvent(TransactionFormEvent.TotalAmountChanged(it)) },
                            label = "TOTAL PEMBAYARAN (RP) *",
                            placeholder = "Contoh: 250000",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            errorMessage = state.totalError,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (state.totalAmountRaw.isNotBlank() && state.totalError == null) {
                            Text(
                                text = "Terbaca: $formattedDisplay",
                                style = MaterialTheme.typography.labelSmall,
                                color = MariPrimaryBlue,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        // D. Volume (Liter) & Harga per Liter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FuelTextField(
                                value = state.volumeLitersRaw,
                                onValueChange = { viewModel.onEvent(TransactionFormEvent.VolumeLitersChanged(it)) },
                                label = "VOLUME (LITER)",
                                placeholder = "Contoh: 25.5",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )

                            FuelTextField(
                                value = state.pricePerLiterRaw,
                                onValueChange = { viewModel.onEvent(TransactionFormEvent.PricePerLiterChanged(it)) },
                                label = "HARGA / LITER (RP)",
                                placeholder = "Otomatis dihitung",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // E. Peringatan Diskrepansi (Jika ada selisih perhitungan)
                        state.discrepancyWarning?.let { warning ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF1F0))
                                    .border(1.dp, MariDanger.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MariDanger,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = warning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MariDanger
                                )
                            }
                        }

                        // F. Jenis BBM
                        Column {
                            FuelTextField(
                                value = state.fuelType,
                                onValueChange = { viewModel.onEvent(TransactionFormEvent.FuelTypeChanged(it)) },
                                label = "JENIS BAHAN BAKAR",
                                placeholder = "Pertalite / Pertamax / Solar",
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Chip Saran Cepat Jenis BBM
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(commonFuelTypes) { fuel ->
                                    val isSelected = state.fuelType.equals(fuel, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onEvent(TransactionFormEvent.FuelTypeChanged(fuel)) },
                                        label = { Text(fuel, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MariPrimaryBlue.copy(alpha = 0.12f),
                                            selectedLabelColor = MariPrimaryBlue
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) MariPrimaryBlue else MariBorderLight
                                        )
                                    )
                                }
                            }
                        }

                        // G. Nama SPBU / Stasiun
                        FuelTextField(
                            value = state.stationName,
                            onValueChange = { viewModel.onEvent(TransactionFormEvent.StationNameChanged(it)) },
                            label = "NAMA SPBU / LOKASI",
                            placeholder = "Contoh: SPBU Pertamina Dago",
                            leadingIcon = Icons.Default.LocalGasStation,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // H. Odometer Kendaraan (km)
                        FuelTextField(
                            value = state.odometerKmRaw,
                            onValueChange = { viewModel.onEvent(TransactionFormEvent.OdometerChanged(it)) },
                            label = "ODOMETER SAAT PENGISIAN (KM)",
                            placeholder = "Contoh: 24850",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = Icons.Default.Speed,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // I. Catatan Tambahan (Opsional)
                        FuelTextField(
                            value = state.notes,
                            onValueChange = { viewModel.onEvent(TransactionFormEvent.NotesChanged(it)) },
                            label = "CATATAN TAMBAHAN (OPSIONAL)",
                            placeholder = "Tambahkan catatan jika diperlukan",
                            singleLine = false,
                            maxLines = 3,
                            leadingIcon = Icons.AutoMirrored.Filled.Notes,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 4. Kotak Scan Struk On-Device (Opsional)
                FuelCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!state.isScanningOcr && !state.isSubmitting) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isScanningOcr) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MariPrimaryBlue
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Membaca teks struk dengan Google ML Kit on-device...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MariPrimaryBlue
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MariPrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Isi Cepat dari Foto Struk SPBU",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MariPrimaryBlue
                                        )
                                    )
                                    Text(
                                        text = "Membaca nominal & tanggal on-device · Foto tidak diunggah ke server",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MariMutedLight
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Tombol Aksi Bawah (Simpan & Batal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FuelSecondaryButton(
                        text = "Batal",
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    )

                    FuelPrimaryButton(
                        text = if (state.isEditMode) "Simpan Perubahan" else "Simpan Transaksi",
                        onClick = { viewModel.onEvent(TransactionFormEvent.Submit) },
                        isLoading = state.isSubmitting,
                        enabled = !state.isSubmitting && state.vehicleId.isNotBlank() && state.totalAmountRaw.isNotBlank(),
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
