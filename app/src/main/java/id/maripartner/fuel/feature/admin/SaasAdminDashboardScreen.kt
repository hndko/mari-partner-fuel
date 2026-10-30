package id.maripartner.fuel.feature.admin

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.domain.model.AdminUserSummary
import id.maripartner.fuel.domain.model.DailyGrowthPoint
import id.maripartner.fuel.domain.model.PlanConversionStats
import id.maripartner.fuel.domain.model.SaasKpi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaasAdminDashboardScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SaasAdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Feedback pesan notifikasi
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val error = uiState.errorMessage
        val success = uiState.successMessage
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(SaasAdminUiEvent.DismissMessages)
        } else if (!success.isNullOrBlank()) {
            snackbarHostState.showSnackbar(success)
            viewModel.onEvent(SaasAdminUiEvent.DismissMessages)
        }
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Panel Pemilik SaaS",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(SaasAdminUiEvent.Refresh) },
                        enabled = !uiState.isLoading && !uiState.isRefreshing
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
                                tint = MariPrimaryBlue
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (uiState.isLoading && !uiState.isRefreshing) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MariPrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Memuat data analitik SaaS...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Header Banner & Filter Waktu
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                isSelected = uiState.selectedTimeframe == "30d",
                                onClick = { viewModel.onEvent(SaasAdminUiEvent.ChangeTimeframe("30d")) }
                            )
                            TimeframeChip(
                                label = "7 Hari Terakhir",
                                isSelected = uiState.selectedTimeframe == "7d",
                                onClick = { viewModel.onEvent(SaasAdminUiEvent.ChangeTimeframe("7d")) }
                            )
                            TimeframeChip(
                                label = "Semua Waktu",
                                isSelected = uiState.selectedTimeframe == "all",
                                onClick = { viewModel.onEvent(SaasAdminUiEvent.ChangeTimeframe("all")) }
                            )
                        }
                    }
                }

                // 2. Grid Hero KPI (4 Kartu Ringkasan)
                item {
                    KpiHeroGrid(kpi = uiState.metrics.kpi, conversionStats = uiState.metrics.conversionStats)
                }

                // 3. Grafik Tren Pendaftaran Pengguna (Native Compose Canvas)
                item {
                    RegistrationGrowthChartCard(growthData = uiState.metrics.growthTrend)
                }

                // 4. Distribusi Paket & Rasio Konversi
                item {
                    PlanConversionCard(
                        conversionStats = uiState.metrics.conversionStats,
                        totalUsers = uiState.metrics.kpi.totalUsers
                    )
                }

                // 5. Section Header: Manajemen Pengguna & Status Pro
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Pengguna & Hak Akses Pro",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Kelola pengguna terbaru atau berikan status Pro secara langsung.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onEvent(SaasAdminUiEvent.UpdateSearchQuery(it)) },
                            placeholder = { Text("Cari nama pengguna atau ID...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onEvent(SaasAdminUiEvent.UpdateSearchQuery("")) }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Hapus pencarian",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 6. Daftar Pengguna Terbaru
                val userList = uiState.filteredRecentUsers
                if (userList.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = if (uiState.searchQuery.isNotEmpty()) "Tidak ada pengguna yang cocok dengan '${uiState.searchQuery}'" else "Belum ada riwayat pendaftaran pengguna.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = userList,
                        key = { it.id }
                    ) { user ->
                        AdminUserCard(
                            user = user,
                            onGrantProClicked = {
                                viewModel.onEvent(SaasAdminUiEvent.OpenGrantProDialog(user))
                            }
                        )
                    }
                }
            }
        }

        // Dialog Berikan Pro Manual
        if (uiState.selectedUserForGrantPro != null) {
            val targetUser = uiState.selectedUserForGrantPro!!
            GrantProModalDialog(
                targetUser = targetUser,
                selectedDays = uiState.grantDurationDays,
                notes = uiState.grantNotes,
                isSubmitting = uiState.isGrantingPro,
                onDaysChanged = { viewModel.onEvent(SaasAdminUiEvent.ChangeGrantDuration(it)) },
                onNotesChanged = { viewModel.onEvent(SaasAdminUiEvent.ChangeGrantNotes(it)) },
                onDismiss = { viewModel.onEvent(SaasAdminUiEvent.DismissGrantProDialog) },
                onConfirm = { viewModel.onEvent(SaasAdminUiEvent.SubmitGrantPro) }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Sub-Komponen UI
// -----------------------------------------------------------------------------

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
            // Card 1: Total Omset Pendapatan
            KpiMetricCard(
                title = "Total Omset IDR",
                value = CurrencyFormatter.formatIdr(kpi.totalRevenueIdr),
                subtitle = "Dari transaksi paket berbayar",
                icon = Icons.Default.AttachMoney,
                iconTint = Color(0xFF0D9488), // Teal
                containerColor = Color(0xFFF0FDFA),
                modifier = Modifier.weight(1f)
            )

            // Card 2: Total Pengguna
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
            // Card 3: Pengguna Pro Aktif & Konversi
            KpiMetricCard(
                title = "Pelanggan Pro Aktif",
                value = "${kpi.activeProUsers} Akun",
                subtitle = "${conversionStats.conversionRatePercent}% Rasio Konversi",
                icon = Icons.Default.WorkspacePremium,
                iconTint = Color(0xFFD97706), // Amber
                containerColor = Color(0xFFFFFBEB),
                modifier = Modifier.weight(1f)
            )

            // Card 4: Transaksi BBM & Armada
            KpiMetricCard(
                title = "Catatan BBM & Armada",
                value = "${kpi.totalTransactionsRecorded} Log",
                subtitle = "${kpi.totalVehiclesRegistered} Armada aktif",
                icon = Icons.Default.LocalGasStation,
                iconTint = Color(0xFF2563EB), // Indigo
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
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                // Gambar Canvas Bar Chart yang halus dan responsif
                val maxVal = (growthData.maxOfOrNull { it.count } ?: 1L).coerceAtLeast(1L)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 24.dp.toPx() // Sisakan ruang bawah untuk label
                        val pointCount = growthData.size
                        val barSpacing = 4.dp.toPx()
                        val totalSpacing = barSpacing * (pointCount + 1)
                        val barWidth = ((canvasWidth - totalSpacing) / pointCount).coerceIn(4f, 32.dp.toPx())

                        growthData.forEachIndexed { index, point ->
                            val x = barSpacing + index * (barWidth + barSpacing)
                            val barHeightFraction = (point.count.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)
                            val barHeight = canvasHeight * barHeightFraction
                            val y = canvasHeight - barHeight

                            // Gambar Bar dengan gradien
                            val barBrush = Brush.verticalGradient(
                                colors = if (point.count > 0) {
                                    listOf(Color(0xFF38BDF8), Color(0xFF0284C7)) // Sky to Teal
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

                        // Garis baseline abu-abu
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, canvasHeight),
                            end = Offset(canvasWidth, canvasHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Label Tanggal Pertama dan Terakhir
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

            // Bar multi-segmen visual
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
                            .background(Color(0xFF94A3B8)) // Slate
                    )
                }
                if (monthlyFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(monthlyFraction)
                            .fillMaxSize()
                            .background(MariPrimaryBlue) // Primary Teal
                    )
                }
                if (yearlyFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(yearlyFraction)
                            .fillMaxSize()
                            .background(Color(0xFFEAB308)) // Amber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend counts
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                LegendItem(
                    label = "Free Tier",
                    count = conversionStats.freeUsers,
                    color = Color(0xFF94A3B8)
                )
                LegendItem(
                    label = "Pro Bulanan",
                    count = conversionStats.proMonthlyUsers,
                    color = MariPrimaryBlue
                )
                LegendItem(
                    label = "Pro Tahunan",
                    count = conversionStats.proYearlyUsers,
                    color = Color(0xFFEAB308)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    count: Long,
    color: Color
) {
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

@Composable
private fun AdminUserCard(
    user: AdminUserSummary,
    onGrantProClicked: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inisial Avatar
                val initial = user.displayName.take(1).uppercase().ifBlank { "U" }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (user.isPro) Color(0xFFFEF3C7) else Color(0xFFE2E8F0)
                        )
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (user.isPro) Color(0xFFB45309) else Color(0xFF475569)
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (user.role.equals("owner", ignoreCase = true)) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "👑", fontSize = 12.sp)
                        } else if (user.role.equals("admin", ignoreCase = true)) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "🛡️", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID: ${user.id.take(8)}... • ${user.vehiclesCount} Kendaraan • ${user.transactionsCount} Transaksi",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge status Pro
                if (user.isPro) {
                    FuelStatusChip(
                        text = "PRO",
                        type = FuelChipType.Success,
                        icon = Icons.Default.Stars
                    )
                } else {
                    FuelStatusChip(
                        text = "FREE",
                        type = FuelChipType.Info
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Upgrade / Perpanjang Pro Button
            OutlinedButton(
                onClick = onGrantProClicked,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFD97706)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (user.isPro) "Perpanjang Masa Aktif Pro" else "Berikan Akses Pro Gratis",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GrantProModalDialog(
    targetUser: AdminUserSummary,
    selectedDays: Int,
    notes: String,
    isSubmitting: Boolean,
    onDaysChanged: (Int) -> Unit,
    onNotesChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⭐", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aktivasi Pro Manual",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Berikan hak istimewa status Pro kepada akun:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = targetUser.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ID: ${targetUser.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Pilih Durasi Tambahan:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Radio options
                DurationRadioOption(
                    title = "30 Hari (1 Bulan)",
                    days = 30,
                    isSelected = selectedDays == 30,
                    onSelect = { onDaysChanged(30) }
                )
                DurationRadioOption(
                    title = "90 Hari (3 Bulan)",
                    days = 90,
                    isSelected = selectedDays == 90,
                    onSelect = { onDaysChanged(90) }
                )
                DurationRadioOption(
                    title = "365 Hari (1 Tahun Penuh)",
                    days = 365,
                    isSelected = selectedDays == 365,
                    onSelect = { onDaysChanged(365) }
                )

                // Input Catatan
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChanged,
                    label = { Text("Catatan / Alasan Pemberian (Opsional)") },
                    placeholder = { Text("Misal: Hadiah loyalitas, uji coba beta") },
                    maxLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = MariPrimaryBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Konfirmasi & Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun DurationRadioOption(
    title: String,
    days: Int,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}
