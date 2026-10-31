package id.maripartner.fuel.feature.dashboard.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import id.maripartner.fuel.domain.model.FuelBrandPrice
import id.maripartner.fuel.domain.model.IndonesianFuelMarketData
import id.maripartner.fuel.domain.usecase.fuel.CalculateFuelSavingsArbitrageUseCase

@Composable
fun FuelPriceRadarDialog(
    onDismiss: () -> Unit
) {
    val grades = listOf("RON 92", "RON 95", "RON 98", "RON 90", "Diesel")
    var selectedGrade by remember { mutableStateOf("RON 92") }

    val tankPresets = listOf(
        4.0 to "Motor (4L)",
        35.0 to "City Car (35L)",
        45.0 to "MPV/SUV (45L)",
        60.0 to "SUV Besar (60L)"
    )
    var tankCapacity by remember { mutableDoubleStateOf(45.0) }

    val arbitrageUseCase = remember { CalculateFuelSavingsArbitrageUseCase() }

    val filteredPrices = remember(selectedGrade) {
        IndonesianFuelMarketData.currentPrices.filter { it.gradeCategory == selectedGrade }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = MariPrimaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Radar Harga BBM & Arbitrase",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Bandingkan harga BBM resmi di SPBU Indonesia dan kalkulasikan selisih penghematan saat isi tangki penuh.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Kategori Bahan Bakar (RON / Diesel)
                Text(
                    text = "Pilih Kategori RON / Jenis BBM:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    grades.forEach { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pilihan Kapasitas Tangki Penuh
                Text(
                    text = "Kapasitas Tangki Kendaraan:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tankPresets.forEach { (capacity, label) ->
                        FilterChip(
                            selected = tankCapacity == capacity,
                            onClick = { tankCapacity = capacity },
                            label = { Text(label, fontSize = 10.5.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Daftar Harga Per Brand
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        filteredPrices.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.brand} — ${item.fuelName}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    if (item.isSubsidized) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Subsidi",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MariSuccess,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = "${CurrencyFormatter.formatRupiah(item.pricePerLiterIdr)}/L",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MariPrimaryBlue
                                    )
                                )
                            }
                            if (idx < filteredPrices.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                // Kalkulasi Arbitrase Penghematan (Jika ada minimal 2 brand di grade ini)
                if (filteredPrices.size >= 2) {
                    val lowest = filteredPrices.minByOrNull { it.pricePerLiterIdr }
                    val highest = filteredPrices.maxByOrNull { it.pricePerLiterIdr }

                    if (lowest != null && highest != null && lowest != highest) {
                        val arbitrage = arbitrageUseCase(lowest, highest, tankCapacity)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MariSuccess.copy(alpha = 0.1f))
                                .border(1.dp, MariSuccess.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = MariSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Potensi Penghematan Tangki Penuh",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MariSuccess
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Isi di ${lowest.brand} menghemat ${CurrencyFormatter.formatRupiah(arbitrage.savingsPerFullTankIdr)} per tangki (${tankCapacity.toInt()}L) dibanding ${highest.brand}, atau hemat ${CurrencyFormatter.formatRupiah(arbitrage.estimatedMonthlySavingsIdr)} per bulan!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MariPrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tutup Radar")
            }
        }
    )
}
