package id.maripartner.fuel.feature.vehicles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelLoadingState
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.FuelTextField
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.component.ThousandSeparatorVisualTransformation
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.domain.model.VehicleType

private val POPULAR_FUEL_TYPES = listOf(
    "Pertalite",
    "Pertamax",
    "Pertamax Turbo",
    "Solar",
    "Dexlite",
    "Pertamina Dex"
)

/**
 * Layar Tambah / Edit Kendaraan — Desain Resmi Mari Partner Fuel
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VehicleFormScreen(
    vehicleId: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VehicleFormViewModel = viewModel()
) {
    val state by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vehicleId) {
        if (!vehicleId.isNullOrBlank()) {
            viewModel.loadVehicle(vehicleId)
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
        }
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = if (state.isEditMode) "Edit Kendaraan" else "Tambah Kendaraan",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        if (state.isLoadingData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                FuelLoadingState(message = "Memuat data kendaraan…")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Header Area: Eyebrow GARASI & Judul
                    Column {
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
                            text = if (state.isEditMode) "Edit Kendaraan" else "Tambah Kendaraan Baru",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MariBrandNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Isi informasi dasar kendaraanmu. Kamu bisa mengubahnya kapan saja.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MariMutedLight
                        )
                    }

                    // 2. Formulir Utama dalam Kartu FuelCard
                    FuelCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        contentPadding = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Bagian A: Jenis Kendaraan (Radio Pills)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "JENIS KENDARAAN *",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MariMutedLight
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    VehicleType.entries.forEach { type ->
                                        val isSelected = state.vehicleType == type
                                        val typeIcon = when (type) {
                                            VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
                                            VehicleType.CAR -> Icons.Default.DirectionsCar
                                            VehicleType.OTHER -> Icons.Default.DirectionsCar
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) MariPrimaryBlue.copy(alpha = 0.12f) else Color.Transparent,
                                            border = BorderStroke(
                                                1.5.dp,
                                                if (isSelected) MariPrimaryBlue else MariBorderLight
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { viewModel.onEvent(VehicleFormEvent.TypeChanged(type)) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = typeIcon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MariPrimaryBlue else MariMutedLight,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = type.displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) MariPrimaryBlue else MariTextLight
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MariBorderLight)

                            // Bagian B: Identitas Utama Kendaraan
                            FuelTextField(
                                value = state.name,
                                onValueChange = { viewModel.onEvent(VehicleFormEvent.NameChanged(it)) },
                                label = "Nama Panggilan Kendaraan *",
                                placeholder = "Contoh: Toyota Avanza, Vario Harian",
                                leadingIcon = Icons.Default.DirectionsCar,
                                errorMessage = state.nameError
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FuelTextField(
                                    value = state.licensePlate,
                                    onValueChange = { viewModel.onEvent(VehicleFormEvent.LicensePlateChanged(it)) },
                                    label = "Nomor Polisi",
                                    placeholder = "Contoh: B 1234 ABC",
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                    modifier = Modifier.weight(1f)
                                )

                                FuelTextField(
                                    value = state.productionYear,
                                    onValueChange = { viewModel.onEvent(VehicleFormEvent.YearChanged(it)) },
                                    label = "Tahun Pembuatan",
                                    placeholder = "Contoh: 2022",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    errorMessage = state.yearError,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FuelTextField(
                                    value = state.make,
                                    onValueChange = { viewModel.onEvent(VehicleFormEvent.MakeChanged(it)) },
                                    label = "Merek (Opsional)",
                                    placeholder = "Contoh: Toyota / Honda",
                                    modifier = Modifier.weight(1f)
                                )

                                FuelTextField(
                                    value = state.model,
                                    onValueChange = { viewModel.onEvent(VehicleFormEvent.ModelChanged(it)) },
                                    label = "Model (Opsional)",
                                    placeholder = "Contoh: Avanza / Vario 160",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(color = MariBorderLight)

                            // Bagian C: Bahan Bakar & Odometer
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FuelTextField(
                                    value = state.defaultFuelType,
                                    onValueChange = { viewModel.onEvent(VehicleFormEvent.DefaultFuelChanged(it)) },
                                    label = "Jenis Bahan Bakar Utama",
                                    placeholder = "Pilih rekomendasi di bawah atau ketik kustom",
                                    leadingIcon = Icons.Default.LocalGasStation
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    POPULAR_FUEL_TYPES.forEach { fuel ->
                                        val isSelected = state.defaultFuelType.equals(fuel, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) MariPrimaryBlue.copy(alpha = 0.12f) else MariBorderLight.copy(alpha = 0.4f),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) MariPrimaryBlue else Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { viewModel.onEvent(VehicleFormEvent.DefaultFuelChanged(fuel)) }
                                        ) {
                                            Text(
                                                text = fuel,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = if (isSelected) MariPrimaryBlue else MariMutedLight,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FuelTextField(
                                    value = state.tankCapacityLiters,
                                    onValueChange = { viewModel.onEvent(VehicleFormEvent.TankCapacityChanged(it)) },
                                    label = "Kapasitas Tangki (L)",
                                    placeholder = "Contoh: 45",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    errorMessage = state.tankError,
                                    modifier = Modifier.weight(1f)
                                )

                                FuelTextField(
                                    value = state.lastOdometerKm,
                                    onValueChange = { input ->
                                        viewModel.onEvent(VehicleFormEvent.OdometerChanged(input.filter { it.isDigit() }))
                                    },
                                    label = "Odometer Terkini (km)",
                                    placeholder = "Contoh: 24.500",
                                    leadingIcon = Icons.Default.Speed,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    visualTransformation = ThousandSeparatorVisualTransformation(),
                                    errorMessage = state.odometerError,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(color = MariBorderLight)

                            // Bagian D: Catatan Tambahan
                            FuelTextField(
                                value = state.notes,
                                onValueChange = { viewModel.onEvent(VehicleFormEvent.NotesChanged(it)) },
                                label = "Catatan Tambahan (Opsional)",
                                placeholder = "Contoh: Jadwal servis berkala tiap 5.000 km di bengkel resmi",
                                singleLine = false,
                                maxLines = 3
                            )
                        }
                    }

                    // 3. Tombol Aksi Formulir: Simpan & Batal
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FuelPrimaryButton(
                            text = if (state.isEditMode) "Simpan Perubahan" else "Simpan Kendaraan",
                            onClick = { viewModel.onEvent(VehicleFormEvent.Submit) },
                            isLoading = state.isSubmitting,
                            enabled = !state.isSubmitting && state.name.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        FuelSecondaryButton(
                            text = "Batal",
                            onClick = onNavigateBack,
                            enabled = !state.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Alert jika ada error saat simpan (misal batas kuota Free Tier)
    state.errorMessage?.let { errorMsg ->
        FuelSweetAlertDialog(
            show = true,
            type = SweetAlertType.WARNING,
            title = "Perhatian",
            message = errorMsg,
            confirmText = "Mengerti",
            onConfirm = { viewModel.onEvent(VehicleFormEvent.ClearError) },
            onDismiss = { viewModel.onEvent(VehicleFormEvent.ClearError) }
        )
    }
}
