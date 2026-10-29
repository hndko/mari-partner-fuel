package id.maripartner.fuel.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTopBar

@Composable
fun DashboardScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToManualInput: () -> Unit,
    onNavigateToProPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card Ringkasan Bulan Ini
            FuelCard {
                Text(
                    text = "Pengeluaran Bulan Ini",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.formatIdr(0L),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                            text = CurrencyFormatter.formatLiters(0.0),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column {
                        Text(
                            text = "Rata-rata/Liter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatIdr(0L),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column {
                        Text(
                            text = "Transaksi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "0 kali",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Action Quick Buttons
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

            // Bagian Transaksi Terakhir
            Text(
                text = "Transaksi Terbaru",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            FuelCard {
                FuelEmptyState(
                    title = "Belum Ada Transaksi",
                    subtitle = "Mulai catat pengeluaran BBM Anda dengan scan struk SPBU atau input manual.",
                    icon = Icons.Default.Receipt,
                    actionButtonText = "Scan Struk Pertama",
                    onActionClick = onNavigateToScan
                )
            }
        }
    }
}
