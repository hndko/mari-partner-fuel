package id.maripartner.fuel.feature.owner.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.domain.model.owner.DailyGrowthPoint
import id.maripartner.fuel.domain.model.owner.PlanConversionStats
import id.maripartner.fuel.domain.model.owner.SaasAdminMetrics
import id.maripartner.fuel.domain.model.owner.SaasKpi

@Composable
fun OwnerDashboardContent(
    metrics: SaasAdminMetrics,
    selectedTimeframe: String,
    onTimeframeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // 1. Header Banner & Filter Waktu
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEAB308).copy(alpha = 0.15f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "👑 SUPER ADMIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309),
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "MONITORING PERTUMBUHAN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MariPrimaryBlue
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ringkasan Eksekutif SaaS",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Rentang Waktu
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TimeframeChip(
                    label = "30 Hari Terakhir",
                    isSelected = selectedTimeframe == "30d",
                    onClick = { onTimeframeChanged("30d") }
                )
                TimeframeChip(
                    label = "7 Hari Terakhir",
                    isSelected = selectedTimeframe == "7d",
                    onClick = { onTimeframeChanged("7d") }
                )
                TimeframeChip(
                    label = "Semua Waktu",
                    isSelected = selectedTimeframe == "all",
                    onClick = { onTimeframeChanged("all") }
                )
            }
        }

        // 2. Grid Hero KPI (4 Kartu Ringkasan)
        KpiHeroGrid(kpi = metrics.kpi, conversionStats = metrics.conversionStats)

        // 3. Grafik Tren Pendaftaran Pengguna (Native Compose Canvas)
        RegistrationGrowthChartCard(growthData = metrics.growthTrend)

        // 4. Distribusi Paket & Rasio Konversi
        PlanConversionCard(
            conversionStats = metrics.conversionStats,
            totalUsers = metrics.kpi.totalUsers
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeframeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MariPrimaryBlue,
            selectedLabelColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun KpiHeroGrid(
    kpi: SaasKpi,
    conversionStats: PlanConversionStats
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KpiMetricCard(
                title = "Total Omset IDR",
                value = CurrencyFormatter.formatIdr(kpi.totalRevenueIdr),
                subtitle = "Dari transaksi paket berbayar",
                icon = Icons.Default.AttachMoney,
                iconTint = Color(0xFF0D9488),
                containerColor = Color(0xFFF0FDFA),
                modifier = Modifier.weight(1f)
            )

            KpiMetricCard(
                title = "Total Pengguna",
                value = "${kpi.totalUsers} User",
                subtitle = "${kpi.freeUsers} Free • ${kpi.activeProUsers} Pro",
                icon = Icons.Default.People,
                iconTint = MariPrimaryBlue,
                containerColor = Color(0xFFF0F9FF),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KpiMetricCard(
                title = "Pelanggan Pro Aktif",
                value = "${kpi.activeProUsers} Akun",
                subtitle = "${conversionStats.conversionRatePercent}% Rasio Konversi",
                icon = Icons.Default.WorkspacePremium,
                iconTint = Color(0xFFD97706),
                containerColor = Color(0xFFFFFBEB),
                modifier = Modifier.weight(1f)
            )

            KpiMetricCard(
                title = "Catatan BBM & Armada",
                value = "${kpi.totalTransactionsRecorded} Log",
                subtitle = "${kpi.totalVehiclesRegistered} Armada aktif",
                icon = Icons.Default.LocalGasStation,
                iconTint = Color(0xFF2563EB),
                containerColor = Color(0xFFEFF6FF),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RegistrationGrowthChartCard(
    growthData: List<DailyGrowthPoint>
) {
    FuelCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Tren Pendaftaran Pengguna",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Grafik registrasi pengguna baru harian",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val totalRegistrationsInPeriod = growthData.sumOf { it.count }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MariPrimaryBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "+$totalRegistrationsInPeriod User",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MariPrimaryBlue
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (growthData.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Text(
                        text = "Tidak ada data registrasi pada rentang waktu ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxVal = (growthData.maxOfOrNull { it.count } ?: 1L).coerceAtLeast(1L)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 24.dp.toPx()
                        val pointCount = growthData.size
                        val barSpacing = 4.dp.toPx()
                        val totalSpacing = barSpacing * (pointCount + 1)
                        val barWidth = ((canvasWidth - totalSpacing) / pointCount).coerceIn(4f, 32.dp.toPx())

                        growthData.forEachIndexed { index, point ->
                            val x = barSpacing + index * (barWidth + barSpacing)
                            val barHeightFraction = (point.count.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)
                            val barHeight = canvasHeight * barHeightFraction
                            val y = canvasHeight - barHeight

                            val barBrush = Brush.verticalGradient(
                                colors = if (point.count > 0) {
                                    listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                } else {
                                    listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                },
                                startY = y,
                                endY = canvasHeight
                            )

                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }

                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, canvasHeight),
                            end = Offset(canvasWidth, canvasHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = growthData.firstOrNull()?.dateLabel.orEmpty(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Maks: $maxVal pendaftar/hari",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = MariPrimaryBlue
                    )
                    Text(
                        text = growthData.lastOrNull()?.dateLabel.orEmpty(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanConversionCard(
    conversionStats: PlanConversionStats,
    totalUsers: Long
) {
    FuelCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Distribusi Paket Langganan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rasio pengguna Free vs Pro berbayar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "${conversionStats.conversionRatePercent}% Pro",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val safeTotal = totalUsers.coerceAtLeast(1L).toFloat()
            val freeFraction = (conversionStats.freeUsers.toFloat() / safeTotal).coerceIn(0f, 1f)
            val monthlyFraction = (conversionStats.proMonthlyUsers.toFloat() / safeTotal).coerceIn(0f, 1f)
            val yearlyFraction = (conversionStats.proYearlyUsers.toFloat() / safeTotal).coerceIn(0f, 1f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                if (freeFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(freeFraction)
                            .fillMaxSize()
                            .background(Color(0xFF94A3B8))
                    )
                }
                if (monthlyFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(monthlyFraction)
                            .fillMaxSize()
                            .background(MariPrimaryBlue)
                    )
                }
                if (yearlyFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(yearlyFraction)
                            .fillMaxSize()
                            .background(Color(0xFFEAB308))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                LegendItem(label = "Free Tier", count = conversionStats.freeUsers, color = Color(0xFF94A3B8))
                LegendItem(label = "Pro Bulanan", count = conversionStats.proMonthlyUsers, color = MariPrimaryBlue)
                LegendItem(label = "Pro Tahunan", count = conversionStats.proYearlyUsers, color = Color(0xFFEAB308))
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Long, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label ($count)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
