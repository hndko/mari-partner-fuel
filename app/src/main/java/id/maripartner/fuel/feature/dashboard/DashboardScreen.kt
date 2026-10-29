package id.maripartner.fuel.feature.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.result.UiState
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelErrorState
import id.maripartner.fuel.core.designsystem.component.FuelLoadingState
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.domain.model.DashboardSummary
import id.maripartner.fuel.domain.model.FuelTransaction
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToManualInput: () -> Unit,
    onNavigateToProPlans: () -> Unit,
    onNavigateToReminders: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Mari Partner Fuel",
                actions = {
                    AssistChip(
                        onClick = onNavigateToProPlans,
                        label = { Text("Pro", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when (val state = uiState.summaryState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    FuelLoadingState(message = "Memuat data dashboard...")
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    FuelErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadDashboard() }
                    )
                }
            }
            is UiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionButtonsSection(
                        onNavigateToScan = onNavigateToScan,
                        onNavigateToManualInput = onNavigateToManualInput,
                        onNavigateToReminders = onNavigateToReminders
                    )

                    FuelEmptyState(
                        title = "Belum Ada Transaksi",
                        subtitle = "Mulai catat transaksi BBM pertama Anda dengan memindai struk SPBU atau masukkan secara manual.",
                        icon = Icons.Default.Receipt,
                        actionButtonText = "Catat Transaksi Sekarang",
                        onActionClick = onNavigateToManualInput
                    )
                }
            }
            is UiState.Success -> {
                val summary = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Ringkasan Pengeluaran Bulan Ini
                    MonthlySummaryCard(summary = summary)

                    // 2. Tombol Aksi Cepat
                    ActionButtonsSection(
                        onNavigateToScan = onNavigateToScan,
                        onNavigateToManualInput = onNavigateToManualInput,
                        onNavigateToReminders = onNavigateToReminders
                    )

                    // 3. Kendaraan dengan Pengeluaran Tertinggi
                    summary.topSpendingVehicle?.let { topVehicle ->
                        TopSpendingVehicleCard(
                            vehicle = topVehicle,
                            totalExpense = summary.totalExpenseIdr
                        )
                    }

                    // 4. Distribusi Pengeluaran Berdasarkan Kendaraan
                    if (summary.vehicleSummaries.isNotEmpty()) {
                        VehicleBreakdownSection(summaries = summary.vehicleSummaries)
                    }

                    // 5. Transaksi Terbaru
                    RecentTransactionsSection(
                        transactions = summary.recentTransactions,
                        onViewAll = onNavigateToTransactions
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            is UiState.Idle -> Unit
        }
    }
}

@Composable
private fun MonthlySummaryCard(summary: DashboardSummary) {
    FuelCard {
        Text(
            text = "Pengeluaran Bulan Ini",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = CurrencyFormatter.formatIdr(summary.totalExpenseIdr),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // Indikator perbandingan dengan bulan lalu
        Spacer(modifier = Modifier.height(6.dp))
        if (summary.hasComparisonData && summary.expenseChangePercentage != null) {
            val isIncrease = summary.expenseDifferenceIdr > 0
            val pct = summary.expenseChangePercentage
            val changeSign = if (isIncrease) "+" else ""
            val diffFormatted = CurrencyFormatter.formatIdr(kotlin.math.abs(summary.expenseDifferenceIdr))
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
                    text = "$changeSign${String.format(Locale.US, "%.1f", pct)}% ($diffFormatted) vs bulan lalu",
                    style = MaterialTheme.typography.bodySmall,
                    color = badgeColor,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Text(
                text = "Belum ada pembanding bulan lalu",
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
                    text = CurrencyFormatter.formatLiters(summary.totalVolumeLiters),
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
                    text = summary.averagePricePerLiterIdr?.let {
                        CurrencyFormatter.formatIdr(it.toLong())
                    } ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column {
                Text(
                    text = "Transaksi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${summary.transactionCount} kali",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (summary.hasIncompleteVolumeData) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sebagian transaksi tidak mencantumkan volume liter.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onNavigateToScan: () -> Unit,
    onNavigateToManualInput: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FuelPrimaryButton(
                text = "Scan Struk",
                onClick = onNavigateToScan,
                leadingIcon = Icons.Default.DocumentScanner,
                modifier = Modifier.weight(1f)
            )
            FuelSecondaryButton(
                text = "Manual",
                onClick = onNavigateToManualInput,
                leadingIcon = Icons.Default.Add,
                modifier = Modifier.weight(1f)
            )
        }
        FuelSecondaryButton(
            text = "Pengingat Servis & Oli",
            onClick = onNavigateToReminders,
            leadingIcon = Icons.Default.Build,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TopSpendingVehicleCard(
    vehicle: id.maripartner.fuel.domain.model.VehicleSpendingSummary,
    totalExpense: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pengeluaran Tertinggi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = vehicle.vehicleName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${CurrencyFormatter.formatIdr(vehicle.totalAmountIdr)} (${String.format(Locale.US, "%.1f", vehicle.percentageOfTotal)}% dari total)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun VehicleBreakdownSection(
    summaries: List<id.maripartner.fuel.domain.model.VehicleSpendingSummary>
) {
    FuelCard {
        Text(
            text = "Pengeluaran per Kendaraan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        summaries.forEach { item ->
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
                        text = CurrencyFormatter.formatIdr(item.totalAmountIdr),
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.transactionCount} transaksi • ${CurrencyFormatter.formatLiters(item.totalVolumeLiters)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionsSection(
    transactions: List<FuelTransaction>,
    onViewAll: () -> Unit
) {
    FuelCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaksi Terbaru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onViewAll() }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (transactions.isEmpty()) {
            Text(
                text = "Belum ada transaksi terbaru.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            transactions.forEachIndexed { index, tx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.stationName ?: "SPBU",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${tx.vehicleName ?: "Kendaraan"} • ${DateUtils.formatDateTime(tx.transactionAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = CurrencyFormatter.formatIdr(tx.totalAmountIdr),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (index < transactions.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}
