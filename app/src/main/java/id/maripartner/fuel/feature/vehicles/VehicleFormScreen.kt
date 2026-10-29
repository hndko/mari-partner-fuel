package id.maripartner.fuel.feature.vehicles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.domain.model.VehicleType

private val POPULAR_FUEL_TYPES = listOf("Pertalite", "Pertamax", "Pertamax Turbo", "Solar", "Dexlite", "Pertamina Dex")

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
            viewModel.onEvent(VehicleFormEvent.ClearError)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Jenis Kendaraan (Radio Filter Chips)
            Text(
                text = "Jenis Kendaraan *",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VehicleType.entries.forEach { type ->
                    val isSelected = state.vehicleType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onEvent(VehicleFormEvent.TypeChanged(type)) },
                        label = { Text(type.displayName) },
                        leadingIcon = {
                            val icon = when (type) {
                                VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
                                VehicleType.CAR -> Icons.Default.DirectionsCar
                                VehicleType.OTHER -> Icons.Default.DirectionsCar
                            }
                            Icon(imageVector = icon, contentDescription = null)
                        }
                    )
                }
            }

            // Nama Kendaraan (Wajib)
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(VehicleFormEvent.NameChanged(it)) },
                label = { Text("Nama Panggilan Kendaraan *") },
                placeholder = { Text("misal: Vario Harian, Avanza Hitam") },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Merek dan Model
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.make,
                    onValueChange = { viewModel.onEvent(VehicleFormEvent.MakeChanged(it)) },
                    label = { Text("Merek") },
                    placeholder = { Text("Honda / Toyota") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.model,
                    onValueChange = { viewModel.onEvent(VehicleFormEvent.ModelChanged(it)) },
                    label = { Text("Model") },
                    placeholder = { Text("Vario 160 / Avanza") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tahun Pembuatan & Nomor Polisi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.productionYear,
                    onValueChange = { viewModel.onEvent(VehicleFormEvent.YearChanged(it)) },
                    label = { Text("Tahun") },
                    placeholder = { Text("misal: 2022") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.yearError != null,
                    supportingText = state.yearError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.licensePlate,
                    onValueChange = { viewModel.onEvent(VehicleFormEvent.LicensePlateChanged(it)) },
                    label = { Text("Nomor Polisi") },
                    placeholder = { Text("B 1234 ABC") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Jenis Bahan Bakar Standar
            Column {
                OutlinedTextField(
                    value = state.defaultFuelType,
                    onValueChange = { viewModel.onEvent(VehicleFormEvent.DefaultFuelChanged(it)) },
                    label = { Text("Jenis Bahan Bakar Utama") },
                    placeholder = { Text("Pilih di bawah atau ketik kustom") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    POPULAR_FUEL_TYPES.forEach { fuel ->
                        FilterChip(
                            selected = state.defaultFuelType.equals(fuel, ignoreCase = true),
                            onClick = { viewModel.onEvent(VehicleFormEvent.DefaultFuelChanged(fuel)) },
                            label = { Text(fuel, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Kapasitas Tangki & Odometer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.tankCapacityLiters,
                    onValueChange = { viewModel.onEvent(VehicleFormEvent.TankCapacityChanged(it)) },
                    label = { Text("Kapasitas Tangki (L)") },
                    placeholder = { Text("misal: 4.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.lastOdometerKm,
                    onValueChange = { viewModel.onEvent(VehicleFormEvent.OdometerChanged(it)) },
                    label = { Text("Odometer Awal (km)") },
                    placeholder = { Text("misal: 15000") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Catatan
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.onEvent(VehicleFormEvent.NotesChanged(it)) },
                label = { Text("Catatan Tambahan (Opsional)") },
                placeholder = { Text("misal: Motor servis rutin tiap 2000 km") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol Simpan
            FuelPrimaryButton(
                text = if (state.isEditMode) "Simpan Perubahan" else "Tambah Kendaraan",
                onClick = { viewModel.onEvent(VehicleFormEvent.Submit) },
                isLoading = state.isSubmitting,
                enabled = !state.isSubmitting && state.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
