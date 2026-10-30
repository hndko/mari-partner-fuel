package id.maripartner.fuel.feature.dashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.theme.MariAiTag
import id.maripartner.fuel.core.designsystem.theme.MariAiTagBg
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariDangerBg
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import id.maripartner.fuel.core.designsystem.theme.MariSuccessBg
import id.maripartner.fuel.core.designsystem.theme.MariWarning
import id.maripartner.fuel.core.designsystem.theme.MariWarningBg
import id.maripartner.fuel.domain.model.DashboardSummary
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import java.time.LocalDate
import java.util.Locale

/**
 * Layar Beranda (Dashboard) Mari Partner Fuel yang telah diredesain secara premium,
 * kaya informasi visual, terstruktur, responsif untuk seluruh ukuran layar, dan nyaman digunakan.
 */
@Composable
fun DashboardScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToManualInput: () -> Unit,
    onNavigateToProPlans: () -> Unit,
    onNavigateToReminders: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToTransactionDetail: (String) -> Unit = {},
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Mari Partner Fuel",
                actions = {
                    // Chip Status Langganan Pro / Free Tier
                    AssistChip(
                        onClick = onNavigateToProPlans,
                        label = {
                            Text(
                                text = if (uiState.isProUser) "Pro Member" else "Tingkatkan ke Pro",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (uiState.isProUser) Color(0xFFE5A100) else MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (uiState.isProUser) Color(0xFFFFF3CD) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            labelColor = if (uiState.isProUser) Color(0xFF7A5800) else MaterialTheme.colorScheme.primary
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = if (uiState.isProUser) Color(0xFFE5A100) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // Tombol Segarkan Dashboard
                    IconButton(
                        onClick = { viewModel.onEvent(DashboardUiEvent.Refresh) },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Segarkan Dashboard",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF1F6FF),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Header Sambutan Pengguna & Periode
                GreetingHeaderSection(
                    userName = uiState.userName,
                    totalVehicles = uiState.totalVehiclesCount
                )

                // 2. Hero Card: Ringkasan Pengeluaran & Metrik BBM Bulan Berjalan
                HeroMonthlySpendingCard(summary = uiState.summary)

                // 3. Action Hub: 4 Kartu Aksi Cepat Interaktif
                QuickActionHubSection(
                    pendingRemindersCount = uiState.pendingRemindersCount,
                    onNavigateToScan = onNavigateToScan,
                    onNavigateToManualInput = onNavigateToManualInput,
                    onNavigateToReminders = onNavigateToReminders,
                    onNavigateToAnalytics = onNavigateToAnalytics
                )

                // 4. Kendaraan Utama / Ajakan Mendaftarkan Kendaraan
                PrimaryVehicleOverviewSection(
                    primaryVehicle = uiState.primaryVehicle,
                    onManageVehicles = onNavigateToVehicles
                )

                // 5. Transaksi Terbaru & Pencatatan Pengisian Terkini
                RecentTransactionsOrStarterGuide(
                    transactions = uiState.summary.recentTransactions,
                    primaryVehicle = uiState.primaryVehicle,
                    onViewAll = onNavigateToTransactions,
                    onStartScan = onNavigateToScan,
                    onManualInput = onNavigateToManualInput,
                    onTransactionClick = onNavigateToTransactionDetail
                )

                // 6. Tips Cerdas Efisiensi BBM (Eco-Driving Insight)
                FuelEfficiencyTipCard()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * 1. Bagian Header Sambutan & Status Pengguna
 */
@Composable
private fun GreetingHeaderSection(
    userName: String,
    totalVehicles: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0B61DB), Color(0xFF053E94))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.firstOrNull()?.uppercase() ?: "M",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Halo, $userName 👋",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (totalVehicles > 0) "$totalVehicles Kendaraan Aktif Terhubung" else "Pantau konsumsi BBM kendaraan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text(
                text = DateUtils.formatMonthYear(LocalDate.now()),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * 2. Kartu Utama (Hero Card) Pengeluaran & Metrik BBM Bulan Berjalan
 */
@Composable
private fun HeroMonthlySpendingCard(summary: DashboardSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0A54B0),
                            Color(0xFF053570)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOTAL PENGELUARAN BBM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (summary.transactionCount > 0) "${summary.transactionCount} Transaksi" else "Bulan Ini",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = CurrencyFormatter.formatIdr(summary.totalExpenseIdr),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (summary.hasComparisonData && summary.expenseChangePercentage != null) {
                    val isIncrease = summary.expenseDifferenceIdr > 0
                    val pct = summary.expenseChangePercentage
                    val sign = if (isIncrease) "+" else ""
                    val icon = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                    val badgeColor = if (isIncrease) Color(0xFFFFB4AB) else Color(0xFFB4F0B7)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$sign${String.format(Locale.US, "%.1f", pct)}% vs bulan lalu",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = badgeColor
                        )
                    }
                } else {
                    Text(
                        text = if (summary.totalExpenseIdr == 0L) "Belum ada transaksi pengisian BBM di bulan ini" else "Data perbandingan bulan lalu belum tersedia",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroMetricTile(
                        label = "Total Liter",
                        value = CurrencyFormatter.formatLiters(summary.totalVolumeLiters),
                        modifier = Modifier.weight(1f)
                    )
                    HeroMetricTile(
                        label = "Rata-rata/Liter",
                        value = summary.averagePricePerLiterIdr?.let { CurrencyFormatter.formatIdr(it.toLong()) } ?: "-",
                        modifier = Modifier.weight(1f)
                    )
                    HeroMetricTile(
                        label = "Rata-rata/Isi",
                        value = if (summary.averageExpensePerTransactionIdr > 0) CurrencyFormatter.formatIdr(summary.averageExpensePerTransactionIdr) else "-",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

/**
 * 3. Bagian Action Hub: 4 Kartu Aksi & Pintasan Modern
 */
@Composable
private fun QuickActionHubSection(
    pendingRemindersCount: Int,
    onNavigateToScan: () -> Unit,
    onNavigateToManualInput: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MENU CEPAT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp,
                        fontSize = 11.sp
                    ),
                    color = MariPrimaryBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Aksi & Pintasan Utama",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "4 Pintasan",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCardItem(
                title = "Scan Struk SPBU",
                subtitle = "OCR Kamera & AI Cerdas",
                icon = Icons.Default.DocumentScanner,
                iconBgColor = MariPrimaryBlue,
                iconTintColor = Color.White,
                badgeText = "Smart AI",
                badgeBgColor = MariAiTagBg,
                badgeTextColor = MariAiTag,
                isPrimary = true,
                onClick = onNavigateToScan,
                modifier = Modifier.weight(1f)
            )

            ActionCardItem(
                title = "Catat Manual",
                subtitle = "Entri pengisian tanpa foto",
                icon = Icons.Default.LocalGasStation,
                iconBgColor = MariSuccessBg,
                iconTintColor = MariSuccess,
                badgeText = "Entri Cepat",
                badgeBgColor = MariSuccessBg,
                badgeTextColor = MariSuccess,
                isPrimary = false,
                onClick = onNavigateToManualInput,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val hasPending = pendingRemindersCount > 0
            ActionCardItem(
                title = "Servis & Oli",
                subtitle = if (hasPending) "$pendingRemindersCount jadwal perlu dicek" else "Jadwal & riwayat servis",
                icon = Icons.Default.Build,
                iconBgColor = if (hasPending) MariDangerBg else MariWarningBg,
                iconTintColor = if (hasPending) MariDanger else MariWarning,
                badgeText = if (hasPending) "$pendingRemindersCount Perhatian" else "Perawatan",
                badgeBgColor = if (hasPending) MariDangerBg else MariWarningBg,
                badgeTextColor = if (hasPending) MariDanger else MariWarning,
                isPrimary = false,
                onClick = onNavigateToReminders,
                modifier = Modifier.weight(1f)
            )

            ActionCardItem(
                title = "Analitik BBM",
                subtitle = "Tren efisiensi km/l & biaya",
                icon = Icons.Default.BarChart,
                iconBgColor = Color(0xFFF3E8FF),
                iconTintColor = Color(0xFF7C3AED),
                badgeText = "Statistik",
                badgeBgColor = Color(0xFFF3E8FF),
                badgeTextColor = Color(0xFF7C3AED),
                isPrimary = false,
                onClick = onNavigateToAnalytics,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionCardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    badgeBgColor: Color = Color.Transparent,
    badgeTextColor: Color = Color.Unspecified
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) Color(0xFFF8FAFD) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isPrimary) 1.5.dp else 1.dp,
            color = if (isPrimary) MariPrimaryBlue.copy(alpha = 0.45f) else MariBorderLight
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPrimary) 2.dp else 1.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Container Icon Squircle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Kolom Teks Judul & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeBgColor
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                ),
                                color = badgeTextColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trailing Chevron Circle Button
            Surface(
                shape = CircleShape,
                color = if (isPrimary) {
                    MariPrimaryBlue.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isPrimary) MariPrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

/**
 * 4. Bagian Kendaraan Utama Aktif
 */
@Composable
private fun PrimaryVehicleOverviewSection(
    primaryVehicle: Vehicle?,
    onManageVehicles: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kendaraan Utama",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Kelola Kendaraan",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onManageVehicles() }
            )
        }

        if (primaryVehicle != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onManageVehicles() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (primaryVehicle.vehicleType) {
                                VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
                                else -> Icons.Default.DirectionsCar
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = primaryVehicle.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!primaryVehicle.licensePlate.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = primaryVehicle.licensePlate.orEmpty(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!primaryVehicle.defaultFuelType.isNullOrBlank()) {
                                Text(
                                    text = "⛽ ${primaryVehicle.defaultFuelType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (primaryVehicle.lastOdometerKm != null && primaryVehicle.lastOdometerKm > 0) {
                                Text(
                                    text = "📍 ${primaryVehicle.lastOdometerKm.toLong()} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Detail Kendaraan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onManageVehicles() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Belum Ada Kendaraan Terhubung",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tambahkan motor atau mobil Anda untuk menghitung konsumsi km/l secara otomatis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(onClick = onManageVehicles) {
                        Text("+ Tambah", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 5. Bagian Transaksi Terbaru & Pencatatan Pengisian Terkini di Beranda
 */
@Composable
private fun RecentTransactionsOrStarterGuide(
    transactions: List<FuelTransaction>,
    primaryVehicle: Vehicle?,
    onViewAll: () -> Unit,
    onStartScan: () -> Unit,
    onManualInput: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRANSAKSI TERBARU",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp,
                        fontSize = 11.sp
                    ),
                    color = MariPrimaryBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Aktivitas Pengisian BBM",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (transactions.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MariPrimaryBlue.copy(alpha = 0.08f),
                    border = BorderStroke(0.8.dp, MariPrimaryBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.clickable { onViewAll() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Lihat Semua",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = MariPrimaryBlue
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        if (transactions.isNotEmpty()) {
            // Daftar Transaksi Terkini
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MariBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    transactions.take(4).forEachIndexed { index, tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTransactionClick(tx.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Squircle
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MariPrimaryBlue.copy(alpha = 0.08f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (tx.scanStatus == "ocr_confirmed" || tx.scanStatus == "ai_confirmed") {
                                            Icons.Default.DocumentScanner
                                        } else {
                                            Icons.Default.LocalGasStation
                                        },
                                        contentDescription = null,
                                        tint = MariPrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Data Stasiun & Subtitle
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tx.stationName?.takeIf { it.isNotBlank() } ?: "SPBU",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    if (!tx.fuelType.isNullOrBlank()) {
                                        val fuelBadgeColor = when {
                                            tx.fuelType.contains("Pertalite", ignoreCase = true) -> Color(0xFF00897B)
                                            tx.fuelType.contains("Solar", ignoreCase = true) -> Color(0xFFC05621)
                                            else -> MariPrimaryBlue
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = fuelBadgeColor.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = tx.fuelType,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                ),
                                                color = fuelBadgeColor,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = tx.vehicleName ?: "Kendaraan",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = DateUtils.formatDateTime(tx.transactionAt),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Nominal & Volume
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.formatIdr(tx.totalAmountIdr),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MariPrimaryBlue
                                )
                                if (tx.volumeLiters != null && tx.volumeLiters > 0) {
                                    Text(
                                        text = CurrencyFormatter.formatLiters(tx.volumeLiters),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        if (index < transactions.take(4).size - 1) {
                            HorizontalDivider(
                                color = MariBorderLight.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Footer Link
                    HorizontalDivider(color = MariBorderLight)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewAll() }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Buka Riwayat Lengkap (${transactions.size} transaksi)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MariPrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        } else {
            // Tampilan Bersih & Interaktif saat Belum Ada Transaksi
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MariBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MariPrimaryBlue.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, MariPrimaryBlue.copy(alpha = 0.15f)),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MariPrimaryBlue,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Belum Ada Catatan Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Mulai pantau biaya dan konsumsi km/liter dengan mencatat pengisian BBM pertama Anda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (primaryVehicle != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.8.dp, MariBorderLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = when (primaryVehicle.vehicleType) {
                                        VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
                                        else -> Icons.Default.DirectionsCar
                                    },
                                    contentDescription = null,
                                    tint = MariPrimaryBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Kendaraan aktif: ${primaryVehicle.name}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onStartScan,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MariPrimaryBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scan Struk AI",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onManualInput,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, MariPrimaryBlue),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MariPrimaryBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = MariPrimaryBlue,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Catat Manual",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MariPrimaryBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data Model Panduan Cerdas Efisiensi BBM
 */
private data class EcoDrivingTip(
    val title: String,
    val category: String,
    val savingsPotential: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color
)

private val ECO_DRIVING_TIPS = listOf(
    EcoDrivingTip(
        title = "Mengemudi Halus (Smooth Throttle)",
        category = "Teknik Kemudi",
        savingsPotential = "Hemat hingga 15%",
        description = "Hindari akselerasi mendadak dan rem mendadak. Menjaga putaran mesin di rentang 2.000–2.500 RPM terbukti memangkas pemborosan BBM hingga 15%.",
        icon = Icons.Default.Speed,
        primaryColor = Color(0xFF00897B)
    ),
    EcoDrivingTip(
        title = "Tekanan Angin Ban Sesuai Standar",
        category = "Ban & Roda",
        savingsPotential = "Hemat 3–5%",
        description = "Ban kempes 20% meningkatkan hambatan gulir secara signifikan. Cek tekanan ban minimal 2 minggu sekali saat ban dalam kondisi dingin.",
        icon = Icons.Default.TireRepair,
        primaryColor = Color(0xFF0284C7)
    ),
    EcoDrivingTip(
        title = "Rutin Ganti Oli & Filter Udara",
        category = "Perawatan Mesin",
        savingsPotential = "Hemat hingga 10%",
        description = "Oli mesin yang sudah jenuh dan filter udara berdebu menghambat aliran oksigen dan pelumasan mesin, membuat konsumsi bensin lebih boros.",
        icon = Icons.Default.Build,
        primaryColor = Color(0xFF7C3AED)
    ),
    EcoDrivingTip(
        title = "Kurangi Beban Muatan Berlebih",
        category = "Beban Kendaraan",
        savingsPotential = "Hemat hingga 8%",
        description = "Setiap beban ekstra 45 kg menambah beban kerja mesin sebesar 1–2%. Bersihkan barang yang tidak terpakai dari bagasi mobil atau jok motor Anda.",
        icon = Icons.Default.FitnessCenter,
        primaryColor = Color(0xFFD97706)
    ),
    EcoDrivingTip(
        title = "Aerodinamika & Kecepatan Stabil",
        category = "Kecepatan Jalan",
        savingsPotential = "Hemat 5–10%",
        description = "Di jalan tol atau luar kota di atas 70 km/jam, tutup kaca jendela mobil untuk meminimalkan hambatan angin, dan jaga laju kendaraan tetap konstan.",
        icon = Icons.Default.Air,
        primaryColor = Color(0xFF059669)
    )
)

/**
 * 6. Kartu Tips Cerdas Efisiensi BBM (Eco-Driving Insight)
 * Interaktif, dinamis, visual premium dengan kategori & estimasi penghematan
 */
@Composable
private fun FuelEfficiencyTipCard() {
    val initialIndex = remember { (LocalDate.now().dayOfYear) % ECO_DRIVING_TIPS.size }
    var currentTipIndex by remember { mutableStateOf(initialIndex) }
    val tip = ECO_DRIVING_TIPS[currentTipIndex]

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MariBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Category Pill + Savings Pill + Shuffle Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tip.primaryColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = tip.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            color = tip.primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Savings Potential Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MariSuccessBg,
                        border = BorderStroke(0.6.dp, MariSuccess.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = tip.savingsPotential,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = MariSuccess,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                // Interactive "Ganti Tips" Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(0.8.dp, MariBorderLight),
                    modifier = Modifier.clickable {
                        currentTipIndex = (currentTipIndex + 1) % ECO_DRIVING_TIPS.size
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Tips Selanjutnya",
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Ganti Tips",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = MariPrimaryBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Content: Squircle Icon + Title & Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tip.primaryColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, tip.primaryColor.copy(alpha = 0.2f)),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = tip.icon,
                            contentDescription = null,
                            tint = tip.primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tip.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tip.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dot Indicators (Pagination dots)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ECO_DRIVING_TIPS.forEachIndexed { idx, _ ->
                    val isSelected = idx == currentTipIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(5.dp)
                            .width(if (isSelected) 18.dp else 5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isSelected) tip.primaryColor else MariBorderLight
                            )
                            .clickable { currentTipIndex = idx }
                    )
                }
            }
        }
    }
}
