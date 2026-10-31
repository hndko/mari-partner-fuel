package id.maripartner.fuel.feature.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelErrorState
import id.maripartner.fuel.core.designsystem.component.FuelLoadingState
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariDangerBg
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.core.designsystem.theme.MariWarning
import id.maripartner.fuel.core.designsystem.theme.MariWarningBg
import id.maripartner.fuel.domain.model.AnalyticsPeriod
import id.maripartner.fuel.domain.model.AnalyticsReport
import id.maripartner.fuel.domain.model.AnomalySeverity
import id.maripartner.fuel.domain.model.FuelAnomalyResult
import id.maripartner.fuel.domain.model.TimePeriodExpense
import id.maripartner.fuel.domain.model.VehicleEfficiencySummary
import id.maripartner.fuel.domain.model.VehicleSpendingSummary
import java.util.Locale

@Composable
fun AnalyticsScreen(
    onNavigateToCreateTransaction: () -> Unit = {},
    onNavigateToTripCalculator: () -> Unit = {},
    onNavigateToProPlans: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Analitik BBM",
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(AnalyticsUiEvent.Refresh) },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MariPrimaryBlue
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Segarkan",
                                tint = MariTextLight
                            )
                        }
                    }
                }
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
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
            ) {
                // 1. Header Visual Sesuai Official UI Reference
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "INSIGHT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MariPrimaryBlue
                    )
                    Text(
                        text = "Analitik BBM",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MariTextLight
                        )
                    )
                    Text(
                        text = "Pahami pola pengeluaran dan efisiensi konsumsi kendaraanmu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MariMutedLight
                    )
                }

                // 2. Baris Filter Periode Cepat
                PeriodFilterBar(
                    selectedPeriod = uiState.selectedPeriod,
                    onSelectPeriod = { viewModel.onEvent(AnalyticsUiEvent.SelectPeriod(it)) }
                )

                // 3. Filter Kendaraan (Jika ada lebih dari 1 kendaraan)
                if (uiState.availableVehicles.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedVehicleId == null,
                            onClick = { viewModel.onEvent(AnalyticsUiEvent.SelectVehicle(null)) },
                            label = { Text("Semua Kendaraan") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MariPrimaryBlue.copy(alpha = 0.12f),
                                selectedLabelColor = MariPrimaryBlue
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.selectedVehicleId == null,
                                borderColor = if (uiState.selectedVehicleId == null) MariPrimaryBlue else MariBorderLight
                            )
                        )

                        uiState.availableVehicles.forEach { vehicle ->
                            val isSelected = uiState.selectedVehicleId == vehicle.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val newId = if (isSelected) null else vehicle.id
                                    viewModel.onEvent(AnalyticsUiEvent.SelectVehicle(newId))
                                },
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
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 4. Konten Analitik Berdasarkan Status UI
                when (val state = uiState.reportState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            FuelLoadingState(message = "Menghitung analitik & rasio efisiensi BBM...")
                        }
                    }

                    is UiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            FuelErrorState(
                                message = state.message,
                                onRetry = { viewModel.loadReport() }
                            )
                        }
                    }

                    is UiState.Empty -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FuelEmptyState(
                                    title = "Belum Ada Data Transaksi",
                                    subtitle = "Tidak ditemukan catatan transaksi BBM pada periode ${uiState.selectedPeriod.label}. Mulai catat pengisian BBM untuk melihat visualisasi analitik.",
                                    icon = Icons.Default.BarChart,
                                    actionButtonText = "＋ Catat Transaksi BBM",
                                    onActionClick = onNavigateToCreateTransaction
                                )
                            }
                        }
                    }

                    is UiState.Success -> {
                        val report = state.data
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // A. 4-Card KPI Grid (Ringkasan Metrik Finansial)
                            AnalyticsKpiGrid(report = report)

                            // Diagnosa Cerdas Anomali Efisiensi Konsumsi BBM (Prioritas 3)
                            uiState.fuelAnomalyResult?.let { anomaly ->
                                if (anomaly.hasAnomaly) {
                                    AnalyticsAnomalyDiagnosisCard(anomaly = anomaly)
                                }
                            }

                            // Pintasan Cerdas Kalkulator Biaya Perjalanan (Prioritas 2)
                            TripCalculatorShortcutCard(onNavigateToCalculator = onNavigateToTripCalculator)

                            // B. Visualisasi Grafik Tren Pengeluaran (Bar Chart)
                            if (report.timeSeriesExpenses.isNotEmpty()) {
                                ExpensesTimeSeriesCard(
                                    periodLabel = report.period.label,
                                    timeSeries = report.timeSeriesExpenses
                                )
                            }

                            // C. Distribusi Pengeluaran per Kendaraan
                            if (report.vehicleDistribution.isNotEmpty()) {
                                VehicleDistributionCard(distribution = report.vehicleDistribution)
                            }

                            // D. Estimasi Efisiensi BBM (km/L)
                            FuelEfficiencyCard(efficiencies = report.efficiencySummaries)

                            // E. Smart AI Insight Card
                            SmartInsightCard(report = report)

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    is UiState.Idle -> Unit
                }
            }
        }
    }
}

/**
 * Filter Chip Bar untuk Periode Waktu
 */
@Composable
private fun PeriodFilterBar(
    selectedPeriod: AnalyticsPeriod,
    onSelectPeriod: (AnalyticsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnalyticsPeriod.entries.filter { it != AnalyticsPeriod.CUSTOM }.forEach { period ->
            val isSelected = period == selectedPeriod
            FilterChip(
                selected = isSelected,
                onClick = { onSelectPeriod(period) },
                label = { Text(period.label) },
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

/**
 * Grid 4 Kartu KPI Metrik (Total Pengeluaran, Total Volume, Rata-rata/Isi, Perubahan Periode)
 */
@Composable
private fun AnalyticsKpiGrid(report: AnalyticsReport) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Baris 1: Total Pengeluaran & Total Volume
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Total Pengeluaran
            FuelCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Total Pengeluaran",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatIdr(report.totalExpenseIdr),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MariPrimaryBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${report.transactionCount} transaksi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                }
            }

            // Card 2: Total Volume
            FuelCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Total Volume",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%.1f L".format(report.totalVolumeLiters),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MariBrandNavy
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (report.totalVolumeLiters > 0 && report.totalExpenseIdr > 0) {
                            "@ ${CurrencyFormatter.formatIdr((report.totalExpenseIdr / report.totalVolumeLiters).toLong())}/L"
                        } else "Seluruh kendaraan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                }
            }
        }

        // Baris 2: Rata-rata per Transaksi & Perubahan Periode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 3: Rata-rata per Transaksi
            FuelCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Rata-rata Transaksi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatIdr(report.averageExpensePerTransactionIdr),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MariTextLight
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Per pengisian BBM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                }
            }

            // Card 4: Perubahan Periode
            FuelCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Perubahan Periode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (report.periodChangePercentage != null) {
                        val pct = report.periodChangePercentage
                        val isIncrease = pct > 0
                        val color = if (isIncrease) MariDanger else MariSuccess
                        val icon = if (isIncrease) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
                        val sign = if (isIncrease) "+" else ""

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$sign${CurrencyFormatter.formatPercent(pct)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isIncrease) "Meningkat" else "Lebih hemat",
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    } else {
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MariMutedLight
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Data pembanding baru",
                            style = MaterialTheme.typography.labelSmall,
                            color = MariMutedLight
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visualisasi Grafik Batang Tren Pengeluaran
 */
@Composable
private fun ExpensesTimeSeriesCard(
    periodLabel: String,
    timeSeries: List<TimePeriodExpense>
) {
    FuelCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tren Pengeluaran BBM",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MariTextLight
                    )
                    Text(
                        text = "Histori aktivitas periode $periodLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                }

                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MariPrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            val maxAmount = timeSeries.maxOfOrNull { it.amountIdr }?.takeIf { it > 0 } ?: 1L

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                timeSeries.forEachIndexed { index, item ->
                    val barFraction = (item.amountIdr.toFloat() / maxAmount.toFloat()).coerceIn(0.06f, 1f)
                    val isLast = index == timeSeries.lastIndex

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(42.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height((110 * barFraction).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (item.amountIdr > 0) {
                                            if (isLast) {
                                                Brush.verticalGradient(listOf(MariAccentCyan, MariPrimaryBlue))
                                            } else {
                                                Brush.verticalGradient(
                                                    listOf(
                                                        MariPrimaryBlue.copy(alpha = 0.6f),
                                                        MariPrimaryBlue.copy(alpha = 0.3f)
                                                    )
                                                )
                                            }
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(
                                                    MariBorderLight,
                                                    MariBorderLight.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLast) MariPrimaryBlue else MariMutedLight
                            ),
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kartu Distribusi Pengeluaran per Kendaraan
 */
@Composable
private fun VehicleDistributionCard(
    distribution: List<VehicleSpendingSummary>
) {
    FuelCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pengeluaran per Kendaraan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MariTextLight
                    )
                    Text(
                        text = "Proporsi alokasi bahan bakar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                }

                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MariPrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val colorPalette = listOf(
                MariPrimaryBlue,
                Color(0xFF8B5CF6),
                Color(0xFF10B981),
                Color(0xFFF59E0B)
            )

            distribution.forEachIndexed { idx, item ->
                val barColor = colorPalette[idx % colorPalette.size]

                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(barColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.vehicleName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MariTextLight
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = CurrencyFormatter.formatIdr(item.totalAmountIdr),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MariTextLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${CurrencyFormatter.formatPercent(item.percentageOfTotal)}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MariMutedLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (item.percentageOfTotal / 100f).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = barColor,
                        trackColor = MariBorderLight
                    )
                }
            }
        }
    }
}

/**
 * Kartu Estimasi Efisiensi BBM (km/L)
 */
@Composable
private fun FuelEfficiencyCard(
    efficiencies: List<VehicleEfficiencySummary>
) {
    FuelCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFC9871C).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFFC9871C),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Efisiensi Konsumsi BBM",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MariTextLight
                        )
                        Text(
                            text = "Rasio jarak per liter (km/L)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MariMutedLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MariBorderLight)
            Spacer(modifier = Modifier.height(14.dp))

            if (efficiencies.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MariPrimaryBlue.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MariPrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Catat odometer saat mengisi BBM untuk menghitung rasio km/liter secara otomatis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MariMutedLight
                    )
                }
            } else {
                efficiencies.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.vehicleName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MariTextLight
                            )
                            item.explanationNote?.let { note ->
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MariMutedLight
                                )
                            }
                        }

                        if (item.isSufficientData && item.averageKmPerLiter != null) {
                            FuelStatusChip(
                                text = "${String.format(Locale.US, "%.1f", item.averageKmPerLiter)} km/L",
                                type = FuelChipType.Success
                            )
                        } else {
                            FuelStatusChip(
                                text = "Butuh 2x Odometer",
                                type = FuelChipType.Neutral
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kartu Smart AI Insight Sesuai Reference
 */
@Composable
private fun SmartInsightCard(report: AnalyticsReport) {
    FuelCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MariPrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Insight untukmu",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MariTextLight
                        )
                        Text(
                            text = "Ringkasan data analitik otomatis",
                            style = MaterialTheme.typography.labelSmall,
                            color = MariMutedLight
                        )
                    }
                }

                FuelStatusChip(text = "SMART INSIGHT", type = FuelChipType.AiTag)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MariBorderLight)
            Spacer(modifier = Modifier.height(14.dp))

            val (headline, description) = when {
                report.periodChangePercentage != null && report.periodChangePercentage < 0 -> {
                    "Pengeluaran BBM Menurun" to
                        "Pengeluaran pada periode ini ${CurrencyFormatter.formatPercent(kotlin.math.abs(report.periodChangePercentage))}% lebih rendah dibanding periode sebelumnya. Pertahankan gaya berkendara halus (eco-driving)!"
                }
                report.periodChangePercentage != null && report.periodChangePercentage > 0 -> {
                    "Pengeluaran Meningkat" to
                        "Pengeluaran meningkat ${CurrencyFormatter.formatPercent(report.periodChangePercentage)}% dibanding periode sebelumnya. Periksa jadwal servis dan tekanan angin ban untuk menghemat BBM."
                }
                report.transactionCount > 0 -> {
                    "Pola Pencatatan Teratur" to
                        "Anda telah mencatat ${report.transactionCount} transaksi pengisian BBM dengan total volume %.1f Liter. Terus catat secara berkala untuk akurasi insight lebih tajam.".format(report.totalVolumeLiters)
                }
                else -> {
                    "Mulai Catat BBM" to
                        "Belum ada data pengeluaran yang tercatat pada periode ini. Pindai struk SPBU atau catat transaksi secara manual untuk membuka analitik konsumsi."
                }
            }

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "✦",
                    style = MaterialTheme.typography.titleMedium,
                    color = MariPrimaryBlue,
                    modifier = Modifier.padding(top = 1.dp, end = 8.dp)
                )
                Column {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MariTextLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MariMutedLight,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Kartu Diagnosa Cerdas Anomali Efisiensi Konsumsi BBM (Prioritas 3)
 */
@Composable
private fun AnalyticsAnomalyDiagnosisCard(
    anomaly: FuelAnomalyResult,
    modifier: Modifier = Modifier
) {
    val isCritical = anomaly.severity == AnomalySeverity.CRITICAL
    val bgColor = if (isCritical) MariDangerBg else MariWarningBg
    val borderColor = if (isCritical) MariDanger.copy(alpha = 0.4f) else MariWarning.copy(alpha = 0.4f)
    val titleColor = if (isCritical) MariDanger else Color(0xFFB45309)
    val icon = if (isCritical) Icons.Default.Dangerous else Icons.Default.Warning

    FuelCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (isCritical) MariDanger.copy(alpha = 0.15f) else MariWarning.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isCritical) MariDanger else MariWarning,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isCritical) "DETEKSI ANOMALI KRITIS" else "PERINGATAN EFISIENSI BBM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = titleColor
                        )
                        Text(
                            text = anomaly.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MariBrandNavy
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCritical) MariDanger.copy(alpha = 0.2f) else MariWarning.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = if (isCritical) "KRITIS" else "WARNING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = anomaly.message,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (anomaly.technicalRecommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Rekomendasi Tindakan Teknis:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        anomaly.technicalRecommendations.forEach { rec ->
                            Text(
                                text = "• $rec",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kartu Pintasan Menuju Kalkulator Biaya Perjalanan (Prioritas 2)
 */
@Composable
private fun TripCalculatorShortcutCard(
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    FuelCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToCalculator)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MariPrimaryBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = MariPrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kalkulator Perjalanan & Split Bill",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MariBrandNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Hitung estimasi kebutuhan bensin, biaya patungan antar rekan, dan jejak karbon perjalananmu.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = MariMutedLight
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Buka →",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MariPrimaryBlue
            )
        }
    }
}
