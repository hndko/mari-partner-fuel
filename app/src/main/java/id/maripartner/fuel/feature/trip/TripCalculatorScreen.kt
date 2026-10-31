package id.maripartner.fuel.feature.trip

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.domain.model.IndonesianFuelPrices
import java.util.Locale

@Composable
fun TripCalculatorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProPlans: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TripCalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Kalkulator Perjalanan",
                onNavigateBack = onNavigateBack
            )
        },
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
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Kartu Hasil Estimasi Biaya & Split Bill Utama (Highlight)
                uiState.calculationResult?.let { result ->
                    FuelCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MariPrimaryBlue.copy(alpha = 0.12f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Route,
                                                contentDescription = null,
                                                tint = MariPrimaryBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "ESTIMASI BIAYA PERJALANAN",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MariPrimaryBlue
                                        )
                                        Text(
                                            text = "${"%.1f".format(Locale.US, result.distanceKm).replace('.', ',')} km (${result.fuelTypeName})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MariMutedLight
                                        )
                                    }
                                }

                                if (uiState.isProUser) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF15803D)
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Total Biaya
                            Text(
                                text = "Total Kebutuhan Biaya BBM",
                                style = MaterialTheme.typography.labelMedium,
                                color = MariMutedLight
                            )
                            Text(
                                text = CurrencyFormatter.formatIdr(result.totalFuelCostIdr),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MariBrandNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Grid Indikator: Patungan, Liter, CO2
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Patungan
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bagi Tagihan (${result.passengerCount} Org)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MariMutedLight
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatIdr(result.costPerPassengerIdr),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MariPrimaryBlue
                                        )
                                    )
                                    Text(
                                        text = "/ orang",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MariMutedLight
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(36.dp)
                                        .background(MariBorderLight)
                                )

                                // Kebutuhan Liter
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "Kebutuhan Liter",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MariMutedLight
                                    )
                                    Text(
                                        text = "${"%.2f".format(Locale.US, result.requiredLiters).replace('.', ',')} L",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MariBrandNavy
                                        )
                                    )
                                    Text(
                                        text = "Bahan Bakar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MariMutedLight
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(36.dp)
                                        .background(MariBorderLight)
                                )

                                // Emisi CO2
                                Column(
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "Emisi CO2",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MariMutedLight
                                    )
                                    Text(
                                        text = "${"%.1f".format(Locale.US, result.estimatedCo2EmissionKg).replace('.', ',')} kg",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF059669)
                                        )
                                    )
                                    Text(
                                        text = "Jejak Karbon",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MariMutedLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Tombol Bagikan Rincian Patungan
                            FuelPrimaryButton(
                                text = "Bagikan Rincian ke Teman (WhatsApp)",
                                onClick = {
                                    val summary = viewModel.getShareableSummary()
                                    shareTripSummary(context, summary)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 2. Form Input Jarak Tempuh
                FuelCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Jarak Perjalanan (km)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MariBrandNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.distanceKmInput,
                            onValueChange = { viewModel.onEvent(TripCalculatorUiEvent.DistanceChanged(it)) },
                            placeholder = { Text("Contoh: 75") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Route, contentDescription = null, tint = MariPrimaryBlue)
                            },
                            trailingIcon = {
                                Text("km", style = MaterialTheme.typography.bodyMedium, color = MariMutedLight, modifier = Modifier.padding(end = 12.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MariPrimaryBlue,
                                unfocusedBorderColor = MariBorderLight
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Preset Jarak Cepat
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            val presets = listOf(15, 30, 50, 100, 250, 500)
                            items(presets) { km ->
                                val isSelected = uiState.distanceKm.toInt() == km
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onEvent(TripCalculatorUiEvent.DistanceChanged(km.toString())) },
                                    label = { Text("$km km") },
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
                }

                // 3. Form Pilihan Kendaraan & Efisiensi Konsumsi
                FuelCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kendaraan & Efisiensi Konsumsi",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MariBrandNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.availableVehicles.isNotEmpty()) {
                            Text(
                                text = "Pilih Armada Kendaraan Anda:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MariMutedLight
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 0.dp)
                            ) {
                                items(uiState.availableVehicles) { vehicle ->
                                    val isSelected = uiState.selectedVehicleId == vehicle.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onEvent(TripCalculatorUiEvent.VehicleSelected(vehicle.id)) },
                                        label = { Text(vehicle.name) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isSelected) MariPrimaryBlue else MariMutedLight
                                            )
                                        },
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
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Input Angka Efisiensi km/Liter
                        OutlinedTextField(
                            value = uiState.efficiencyInput,
                            onValueChange = { viewModel.onEvent(TripCalculatorUiEvent.EfficiencyChanged(it)) },
                            label = { Text("Konsumsi Rata-rata (km/Liter)") },
                            placeholder = { Text("Contoh: 12.0") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = MariPrimaryBlue)
                            },
                            trailingIcon = {
                                Text("km/L", style = MaterialTheme.typography.bodyMedium, color = MariMutedLight, modifier = Modifier.padding(end = 12.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MariPrimaryBlue,
                                unfocusedBorderColor = MariBorderLight
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Panduan umum: Motor matic ~40-45 km/L • Mobil kota ~12-14 km/L • Mobil diesel ~14-18 km/L",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MariMutedLight
                        )
                    }
                }

                // 4. Form Jenis & Harga Bahan Bakar Indonesia
                FuelCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Jenis & Harga Bahan Bakar (Indonesia)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MariBrandNavy
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            items(IndonesianFuelPrices.presets) { preset ->
                                val isSelected = !uiState.isCustomPrice && uiState.selectedPreset.name == preset.name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onEvent(TripCalculatorUiEvent.PresetSelected(preset)) },
                                    label = { Text("${preset.name} (${CurrencyFormatter.formatThousands(preset.priceIdr)})") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) MariPrimaryBlue else MariMutedLight
                                        )
                                    },
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Price Input
                        OutlinedTextField(
                            value = if (uiState.isCustomPrice) uiState.customPriceInput else uiState.selectedPreset.priceIdr.toString(),
                            onValueChange = { viewModel.onEvent(TripCalculatorUiEvent.CustomPriceChanged(it)) },
                            label = { Text("Harga per Liter (Rp)") },
                            placeholder = { Text("Contoh: 10000") },
                            leadingIcon = {
                                Text("Rp", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MariPrimaryBlue, modifier = Modifier.padding(start = 12.dp))
                            },
                            trailingIcon = {
                                Text("/ Liter", style = MaterialTheme.typography.bodyMedium, color = MariMutedLight, modifier = Modifier.padding(end = 12.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MariPrimaryBlue,
                                unfocusedBorderColor = MariBorderLight
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 5. Form Bagi Tagihan (Split Bill - Jumlah Penumpang)
                FuelCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Bagi Tagihan (Split Bill)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MariBrandNavy
                                )
                                Text(
                                    text = "Hitung patungan adil antar penumpang perjalanan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MariMutedLight
                                )
                            }

                            // Stepper Controls (- / +)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MariBorderLight.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            viewModel.onEvent(TripCalculatorUiEvent.PassengerCountChanged(uiState.passengerCount - 1))
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", tint = MariBrandNavy)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MariPrimaryBlue.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, MariPrimaryBlue.copy(alpha = 0.3f)),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Group,
                                            contentDescription = null,
                                            tint = MariPrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${uiState.passengerCount} Orang",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MariPrimaryBlue
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MariPrimaryBlue,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            viewModel.onEvent(TripCalculatorUiEvent.PassengerCountChanged(uiState.passengerCount + 1))
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Pro Banner Promo jika belum Pro
                if (!uiState.isProUser) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProPlans() }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tingkatkan ke Mari Pro",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Kelola kendaraan tak terbatas dan dapatkan rekomendasi audit efisiensi BBM otomatis.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

private fun shareTripSummary(context: Context, summaryText: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        val chooser = Intent.createChooser(intent, "Bagikan Estimasi Biaya Perjalanan")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Gagal membagikan rincian: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
