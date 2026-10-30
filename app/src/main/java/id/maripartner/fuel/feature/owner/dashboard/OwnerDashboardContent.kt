package id.maripartner.fuel.feature.owner.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.domain.model.owner.DailyGrowthPoint
import id.maripartner.fuel.domain.model.owner.PlanConversionStats
import id.maripartner.fuel.domain.model.owner.SaasAdminMetrics
import id.maripartner.fuel.domain.model.owner.SaasKpi
import id.maripartner.fuel.feature.owner.navigation.OwnerTab

@Composable
fun OwnerDashboardContent(
    metrics: SaasAdminMetrics,
    selectedTimeframe: String,
    onTimeframeChanged: (String) -> Unit,
    onNavigateToTab: (OwnerTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive Welcome & Header
        item {
            ExecutiveHeaderBanner()
        }

        // 2. Filter Rentang Waktu
        item {
            TimeframeFilterRow(
                selectedTimeframe = selectedTimeframe,
                onTimeframeChanged = onTimeframeChanged
            )
        }

        // 3. Hero Revenue Card (Omset Finansial Utama)
        item {
            HeroRevenueCard(totalRevenue = metrics.kpi.totalRevenueIdr)
        }

        // 4. Grid 3 KPI Penting (Pengguna, Pro Aktif, Armada)
        item {
            OperationalKpiRow(
                kpi = metrics.kpi,
                conversionStats = metrics.conversionStats
            )
        }

        // 5. Menu Pintasan Aksi Eksekutif (Quick Action Shortcuts)
        item {
            ExecutiveQuickActions(onNavigateToTab = onNavigateToTab)
        }

        // 6. Grafik Tren Pendaftaran Pengguna Harian (Canvas Chart)
        item {
            RegistrationGrowthChartCard(growthData = metrics.growthTrend)
        }

        // 7. Distribusi Paket Langganan & Konversi
        item {
            PlanConversionCard(
                conversionStats = metrics.conversionStats,
                totalUsers = metrics.kpi.totalUsers
            )
        }

        // Bottom space for comfortable scroll above navigation bar
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExecutiveHeaderBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A), // Dark Slate 900 khas VIP Executive
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAB308).copy(alpha = 0.2f))
            ) {
                Text(text = "👑", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ringkasan Eksekutif SaaS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEAB308)
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                                color = Color(0xFF0F172A)
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Pemantauan performa bisnis, registrasi pengguna, dan omset real-time.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun TimeframeFilterRow(
    selectedTimeframe: String,
    onTimeframeChanged: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        TimeframeChip(
            label = "30 Hari",
            isSelected = selectedTimeframe == "30d",
            onClick = { onTimeframeChanged("30d") }
        )
        TimeframeChip(
            label = "7 Hari",
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
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MariPrimaryBlue,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun HeroRevenueCard(totalRevenue: Long) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MariPrimaryBlue.copy(alpha = 0.08f),
                            Color(0xFF0D9488).copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MariPrimaryBlue.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MariPrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TOTAL OMSET PENDAPATAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Akumulasi Pembayaran Langganan",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    FuelStatusChip(
                        text = "TERVERIFIKASI",
                        type = FuelChipType.Success
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = CurrencyFormatter.formatIdr(totalRevenue),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Berdasarkan seluruh transaksi paket Pro bulanan dan tahunan yang lunas tercatat di basis data.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OperationalKpiRow(
    kpi: SaasKpi,
    conversionStats: PlanConversionStats
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Card 1: Total Pengguna
            KpiMetricTile(
                title = "Total Pengguna",
                value = "${kpi.totalUsers}",
                subtitle = "${kpi.freeUsers} Free • ${kpi.activeProUsers} Pro",
                icon = Icons.Default.People,
                accentColor = MariPrimaryBlue,
                modifier = Modifier.weight(1f)
            )

            // Card 2: Pelanggan Pro
            KpiMetricTile(
                title = "Pelanggan Pro",
                value = "${kpi.activeProUsers}",
                subtitle = "${conversionStats.conversionRatePercent}% Rasio Konversi",
                icon = Icons.Default.WorkspacePremium,
                accentColor = Color(0xFFD97706),
                modifier = Modifier.weight(1f)
            )
        }

        // Card 3: Operasional BBM & Armada (Full Width Tile)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB).copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aktivitas Pengisian & Armada",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${kpi.totalTransactionsRecorded} Catatan BBM",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        text = "${kpi.totalVehiclesRegistered} Armada Aktif",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiMetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ExecutiveQuickActions(onNavigateToTab: (OwnerTab) -> Unit) {
    Column {
        Text(
            text = "Pintasan Aksi Konsol",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionPill(
                icon = Icons.Default.PriceChange,
                label = "Atur Paket",
                onClick = { onNavigateToTab(OwnerTab.PLANS) },
                modifier = Modifier.weight(1f)
            )
            QuickActionPill(
                icon = Icons.Default.Group,
                label = "Akses Pro",
                onClick = { onNavigateToTab(OwnerTab.USERS) },
                modifier = Modifier.weight(1f)
            )
            QuickActionPill(
                icon = Icons.Default.Payments,
                label = "Audit Transaksi",
                onClick = { onNavigateToTab(OwnerTab.TRANSACTIONS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MariPrimaryBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
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
