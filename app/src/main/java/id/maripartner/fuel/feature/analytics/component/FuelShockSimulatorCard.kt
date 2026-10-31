package id.maripartner.fuel.feature.analytics.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import id.maripartner.fuel.domain.model.FuelShockSimulationResult

@Composable
fun FuelShockSimulatorCard(
    result: FuelShockSimulationResult?,
    selectedHike: Double,
    onHikeSelected: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val hikeOptions = listOf(500.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, MariBorderLight, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFFEE2E2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MariDanger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Simulator Lonjakan Harga BBM",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MariBrandNavy
                        )
                        Text(
                            text = "Proyeksi beban finansial & strategi mitigasi",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            HorizontalDivider(color = MariBorderLight.copy(alpha = 0.5f))

            // Selector Pilihan Kenaikan Harga (Rp/Liter)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PILIH SKENARIO KENAIKAN HARGA (PER LITER):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF4B5563)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(hikeOptions) { hike ->
                        val isSelected = selectedHike == hike
                        FilterChip(
                            selected = isSelected,
                            onClick = { onHikeSelected(hike) },
                            label = {
                                Text(
                                    text = "+Rp ${"%.0f".format(hike)}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MariDanger,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF9FAFB),
                                labelColor = MariBrandNavy
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MariDanger else MariBorderLight
                            )
                        )
                    }
                }
            }

            if (result != null) {
                // Ringkasan Kartu Dampak
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dampak Bulanan
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Beban Tambahan / Bln",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "+${CurrencyFormatter.formatRupiah(result.monthlyImpactIdr)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            text = "Naik +${"%.1f".format(result.percentIncrease)}% dari anggaran",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = Color(0xFFB91C1C)
                        )
                    }

                    // Dampak Tahunan
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Akumulasi / Tahun",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Color(0xFF4B5563)
                        )
                        Text(
                            text = "+${CurrencyFormatter.formatRupiah(result.yearlyImpactIdr)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                            color = MariBrandNavy
                        )
                        Text(
                            text = "Berdasar ${"%.0f".format(result.currentMonthlyLiters)} Liter/bln",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                // Tips Mitigasi
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Rekomendasi Strategi Mitigasi:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MariBrandNavy
                        )
                    }

                    result.actionableTips.forEach { tip ->
                        Text(
                            text = "• $tip",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF374151)
                        )
                    }
                }
            }
        }
    }
}
