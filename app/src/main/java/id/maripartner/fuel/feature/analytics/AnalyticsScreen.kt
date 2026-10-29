package id.maripartner.fuel.feature.analytics

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelErrorState
import id.maripartner.fuel.core.designsystem.component.FuelLoadingState
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.domain.model.AnalyticsPeriod
import id.maripartner.fuel.domain.model.AnalyticsReport
import id.maripartner.fuel.domain.model.TimePeriodExpense
import id.maripartner.fuel.domain.model.VehicleEfficiencySummary
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FuelTopBar(title = "Analitik & Konsumsi BBM")
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Periode Horizontal Chips
            PeriodFilterBar(
                selectedPeriod = uiState.selectedPeriod,
                onSelectPeriod = { viewModel.onEvent(AnalyticsUiEvent.SelectPeriod(it)) }
            )

            when (val state = uiState.reportState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FuelLoadingState(message = "Menghitung analitik pengeluaran...")
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FuelEmptyState(
                            title = "Tidak Ada Transaksi",
                            subtitle = "Tidak ditemukan catatan transaksi BBM pada periode ${uiState.selectedPeriod.label}. Coba ubah filter periode waktu.",
                            icon = Icons.Default.BarChart
                        )
                    }
                }
                is UiState.Success -> {
                    val report = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Ringkasan Metrik Finansial Periode
                        AnalyticsSummaryCard(report = report)

                        // 2. Visualisasi Grafik Tren Pengeluaran
                        if (report.timeSeriesExpenses.isNotEmpty()) {
                            ExpensesTimeSeriesChart(timeSeries = report.timeSeriesExpenses)
                        }

                        // 3. Distribusi Pengeluaran per Kendaraan
                        if (report.vehicleDistribution.isNotEmpty()) {
                            VehicleDistributionSection(distribution = report.vehicleDistribution)
                        }

                        // 4. Estimasi Efisiensi BBM (km/L)
                        FuelEfficiencySection(efficiencies = report.efficiencySummaries)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                is UiState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun PeriodFilterBar(
    selectedPeriod: AnalyticsPeriod,
    onSelectPeriod: (AnalyticsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnalyticsPeriod.entries.filter { it != AnalyticsPeriod.CUSTOM }.forEach { period ->
            val isSelected = period == selectedPeriod
            FilterChip(
                selected = isSelected,
                onClick = { onSelectPeriod(period) },
                label = { Text(period.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun AnalyticsSummaryCard(report: AnalyticsReport) {
    FuelCard {
        Text(
            text = "Total Pengeluaran (${report.period.label})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = CurrencyFormatter.formatIdr(report.totalExpenseIdr),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // Perbandingan antarperiode
        Spacer(modifier = Modifier.height(6.dp))
        if (report.previousPeriodExpenseIdr != null && report.periodChangePercentage != null) {
            val pct = report.periodChangePercentage
            val isIncrease = pct > 0
            val changeSign = if (isIncrease) "+" else ""
            val badgeColor = if (isIncrease) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
            val icon = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = badgeColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$changeSign${String.format(Locale.US, "%.1f", pct)}% dibanding periode sebelumnya",
                    style = MaterialTheme.typography.bodySmall,
                    color = badgeColor,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Text(
                text = "Belum ada pembanding periode sebelumnya",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Liter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatLiters(report.totalVolumeLiters),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column {
                Text(
                    text = "Rata-rata/Transaksi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatIdr(report.averageExpensePerTransactionIdr),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column {
                Text(
                    text = "Rata-rata/Liter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = report.averagePricePerLiterIdr?.let {
                        CurrencyFormatter.formatIdr(it.toLong())
                    } ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ExpensesTimeSeriesChart(timeSeries: List<TimePeriodExpense>) {
    FuelCard {
        Text(
            text = "Tren Pengeluaran BBM",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        val maxAmount = timeSeries.maxOfOrNull { it.amountIdr }?.takeIf { it > 0 } ?: 1L

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            timeSeries.forEach { item ->
                val barFraction = (item.amountIdr.toFloat() / maxAmount.toFloat()).coerceIn(0.05f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(38.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((120 * barFraction).dp)
                                .background(
                                    color = if (item.amountIdr > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleDistributionSection(
    distribution: List<id.maripartner.fuel.domain.model.VehicleSpendingSummary>
) {
    FuelCard {
        Text(
            text = "Distribusi Pengeluaran per Kendaraan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        distribution.forEach { item ->
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.vehicleName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${CurrencyFormatter.formatIdr(item.totalAmountIdr)} (${String.format(Locale.US, "%.1f", item.percentageOfTotal)}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (item.percentageOfTotal / 100f).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FuelEfficiencySection(
    efficiencies: List<VehicleEfficiencySummary>
) {
    FuelCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Estimasi Efisiensi BBM (km/L)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dihitung dari selisih odometer berurutan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (efficiencies.isEmpty()) {
            Text(
                text = "Belum ada transaksi dengan odometer tercatat.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            efficiencies.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.vehicleName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (item.isSufficientData && item.averageKmPerLiter != null) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", item.averageKmPerLiter)} km/L",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Belum Cukup Data",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        item.explanationNote?.let { note ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
