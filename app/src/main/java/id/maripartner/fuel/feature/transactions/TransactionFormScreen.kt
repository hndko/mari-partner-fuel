package id.maripartner.fuel.feature.transactions

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTopBar

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
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(TransactionFormEvent.ClearError)
        }
    }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(TransactionFormEvent.DismissMessage)
        }
    }

    val selectedVehicleName = state.availableVehicles.firstOrNull { it.id == state.vehicleId }?.name
        ?: if (state.availableVehicles.isEmpty()) "Memuat kendaraan..." else "Pilih Kendaraan"

    Scaffold(
        topBar = {
            FuelTopBar(
                title = if (state.isEditMode) "Edit Pengeluaran BBM" else "Catat Pengeluaran BBM",
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
            // 1. Pemilih Kendaraan Aktif
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
                    isError = state.vehicleError != null,
                    supportingText = state.vehicleError?.let { { Text(it) } },
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
                                viewModel.onEvent(TransactionFormEvent.VehicleSelected(vehicle.id))
                                vehicleDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 2. Waktu Transaksi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Waktu Pengisian",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatDateTime(state.transactionAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 3. Total Pembayaran BBM (Wajib)
            val formattedDisplay = state.totalAmountRaw.toLongOrNull()?.let {
                CurrencyFormatter.formatIdr(it)
            } ?: "Rp 0"

            OutlinedTextField(
                value = state.totalAmountRaw,
                onValueChange = { viewModel.onEvent(TransactionFormEvent.TotalAmountChanged(it)) },
                label = { Text("Total Pembayaran (Rp) *") },
                placeholder = { Text("misal: 50000") },
                supportingText = {
                    if (state.totalError != null) {
                        Text(state.totalError.orEmpty())
                    } else if (state.totalAmountRaw.isNotBlank()) {
                        Text("Terformat: $formattedDisplay")
                    }
                },
                isError = state.totalError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Volume Liter & Harga per Liter (Opsional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.volumeLitersRaw,
                    onValueChange = { viewModel.onEvent(TransactionFormEvent.VolumeLitersChanged(it)) },
                    label = { Text("Volume (Liter)") },
                    placeholder = { Text("misal: 3.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.pricePerLiterRaw,
                    onValueChange = { viewModel.onEvent(TransactionFormEvent.PricePerLiterChanged(it)) },
                    label = { Text("Harga / Liter") },
                    placeholder = { Text("misal: 10000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Peringatan jika terdapat selisih perhitungan volume x harga terhadap total
            state.discrepancyWarning?.let { warning ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
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

            // 5. Jenis BBM & SPBU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.fuelType,
                    onValueChange = { viewModel.onEvent(TransactionFormEvent.FuelTypeChanged(it)) },
                    label = { Text("Jenis BBM") },
                    placeholder = { Text("Pertalite") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.stationName,
                    onValueChange = { viewModel.onEvent(TransactionFormEvent.StationNameChanged(it)) },
                    label = { Text("SPBU / Lokasi") },
                    placeholder = { Text("SPBU Pertamina 34.xxx") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // 6. Odometer
            OutlinedTextField(
                value = state.odometerKmRaw,
                onValueChange = { viewModel.onEvent(TransactionFormEvent.OdometerChanged(it)) },
                label = { Text("Odometer Kendaraan (km)") },
                placeholder = { Text("misal: 15420") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 7. Bantu Isi Otomatis via Foto Struk (Opsional via PhotoPicker)
            Text(
                text = "Bantu Isi via Foto Struk (Opsional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Kotak Tombol Pemilihan Foto Struk
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        if (!state.isScanningOcr && !state.isSubmitting) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.isScanningOcr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mengekstrak data struk on-device...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pilih Foto Struk dari Galeri",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Hanya membaca teks di perangkat, foto tidak disimpan di server",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 8. Catatan Tambahan
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.onEvent(TransactionFormEvent.NotesChanged(it)) },
                label = { Text("Catatan Tambahan (Opsional)") },
                placeholder = { Text("misal: Pengisian sebelum perjalanan luar kota") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 9. Tombol Simpan Transaksi (Double Submit Protected)
            FuelPrimaryButton(
                text = if (state.isEditMode) "Simpan Perubahan Transaksi" else "Simpan Transaksi BBM",
                onClick = { viewModel.onEvent(TransactionFormEvent.Submit) },
                isLoading = state.isSubmitting,
                enabled = !state.isSubmitting && state.vehicleId.isNotBlank() && state.totalAmountRaw.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
