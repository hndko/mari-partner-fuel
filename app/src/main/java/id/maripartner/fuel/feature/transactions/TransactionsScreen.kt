package id.maripartner.fuel.feature.transactions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale
import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.SortOrder
import kotlinx.coroutines.launch

@Composable
fun TransactionsScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToProPlans: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pendingCsvContent by remember { mutableStateOf<String?>(null) }
    var showExportChoiceDialog by remember { mutableStateOf(false) }
    var showProUpgradeDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null && pendingCsvContent != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pendingCsvContent!!.toByteArray(Charsets.UTF_8))
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Riwayat transaksi berhasil diekspor ke file CSV.")
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Gagal menyimpan file CSV: ${e.message}")
                }
            } finally {
                pendingCsvContent = null
            }
        }
    }

    // Pagination trigger saat scroll mendekati ujung daftar
    val shouldLoadMore by remember {
        derivedStateOf {
            scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 400
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.canLoadMore && !uiState.isLoadingMore && !uiState.isLoading) {
            viewModel.onEvent(TransactionUiEvent.LoadNextPage)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(TransactionUiEvent.DismissMessage)
        }
    }

    // Kalkulasi ringkasan metrik dari transaksi yang tampil
    val totalExpense = remember(uiState.transactions) {
        uiState.transactions.sumOf { it.totalAmountIdr }
    }
    val totalVolume = remember(uiState.transactions) {
        uiState.transactions.mapNotNull { it.volumeLiters }.sum()
    }
    val totalCount = remember(uiState.transactions) {
        uiState.transactions.size
    }

    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                MariBrandNavy,
                MariPrimaryBlue.copy(alpha = 0.95f),
                MariPrimaryBlue
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val screenHeight = maxHeight
            val density = LocalDensity.current
            var headerHeightDp by remember { mutableStateOf(280.dp) }
            val remainingSheetMinHeight = (screenHeight - headerHeightDp + 36.dp).coerceAtLeast(400.dp)

            // Latar gradien atas jika di-pull down / overscroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(brush = gradientBrush)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = innerPadding.calculateBottomPadding() + 80.dp)
            ) {
                // 1. Hero Gradient Header (Status Bar, Top Bar, Total Pengeluaran, Volume/Count Badges)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            val measuredDp = with(density) { size.height.toDp() }
                            if (measuredDp > 0.dp) {
                                headerHeightDp = measuredDp
                            }
                        }
                        .background(brush = gradientBrush)
                        .statusBarsPadding()
                        .padding(top = 10.dp, bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            // Top Bar: Judul Halaman & Action Buttons (Glassmorphism)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Riwayat Transaksi",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 22.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Aktivitas pengeluaran & pengisian BBM",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.82f),
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                // Action Buttons (Glassmorphism Pill/Circle)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Ekspor Laporan
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                            .clickable(enabled = !uiState.isExporting) { showExportChoiceDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (uiState.isExporting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.FileDownload,
                                                contentDescription = "Ekspor Laporan",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Urutkan Transaksi
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                            .clickable {
                                                val nextSort = if (uiState.filter.sortOrder == SortOrder.NEWEST_FIRST) {
                                                    SortOrder.OLDEST_FIRST
                                                } else {
                                                    SortOrder.NEWEST_FIRST
                                                }
                                                viewModel.onEvent(TransactionUiEvent.SortOrderChanged(nextSort))
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Sort,
                                            contentDescription = "Urutkan (${uiState.filter.sortOrder.displayName})",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Segarkan
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                            .clickable(enabled = !uiState.isLoading) {
                                                viewModel.onEvent(TransactionUiEvent.Refresh)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Segarkan",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Hero KPI: Total Pengeluaran Bulan Ini
                            Text(
                                text = "TOTAL PENGELUARAN BULAN INI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = CurrencyFormatter.formatIdr(totalExpense),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 28.sp
                                    )
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.clickable(onClick = onNavigateToCreate)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MariPrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Catat BBM",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MariPrimaryBlue
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Badges: Total Volume & Total Transaksi
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "%.1f Liter".format(totalVolume),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "$totalCount Transaksi",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                // 2. Elevated White Rounded Sheet Container (wraps EVERYTHING below header)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-18).dp)
                        .defaultMinSize(minHeight = remainingSheetMinHeight),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = remainingSheetMinHeight),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Bento 2-Kolom Analitik Finansial
                            TransactionBentoSection(
                                totalExpense = totalExpense,
                                totalVolume = totalVolume,
                                transactions = uiState.transactions
                            )

                            // Kolom Pencarian Cepat (Soft Pill GoPay Style)
                            TextField(
                                value = uiState.filter.query,
                                onValueChange = { viewModel.onEvent(TransactionUiEvent.QueryChanged(it)) },
                                placeholder = {
                                    Text(
                                        text = "Cari SPBU, jenis BBM, nominal...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF94A3B8)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Cari",
                                        tint = Color(0xFF64748B)
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.filter.query.isNotBlank()) {
                                        IconButton(onClick = { viewModel.onEvent(TransactionUiEvent.QueryChanged("")) }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Hapus Pencarian",
                                                tint = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF1F5F9),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Baris Filter Kendaraan (Soft Capsule Pills)
                            if (uiState.availableVehicles.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        val isAllSelected = uiState.filter.vehicleId == null
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isAllSelected) MariPrimaryBlue.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                            border = if (isAllSelected) BorderStroke(1.dp, MariPrimaryBlue.copy(alpha = 0.4f)) else null,
                                            modifier = Modifier.clickable { viewModel.onEvent(TransactionUiEvent.VehicleFilterChanged(null)) }
                                        ) {
                                            Text(
                                                text = "Semua Kendaraan",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isAllSelected) MariPrimaryBlue else Color(0xFF475569)
                                                ),
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                    items(uiState.availableVehicles) { vehicle ->
                                        val isSelected = uiState.filter.vehicleId == vehicle.id
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isSelected) MariPrimaryBlue.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                            border = if (isSelected) BorderStroke(1.dp, MariPrimaryBlue.copy(alpha = 0.4f)) else null,
                                            modifier = Modifier.clickable {
                                                val newId = if (isSelected) null else vehicle.id
                                                viewModel.onEvent(TransactionUiEvent.VehicleFilterChanged(newId))
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp),
                                                    tint = if (isSelected) MariPrimaryBlue else Color(0xFF64748B)
                                                )
                                                Text(
                                                    text = vehicle.name,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MariPrimaryBlue else Color(0xFF475569)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Info Header Jumlah Transaksi
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Aktivitas Transaksi (${uiState.transactions.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.filter.sortOrder.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MariPrimaryBlue
                                )
                            }

                            // Status Konten: Loading, Empty, atau Pengelompokan Bulan
                            when {
                                uiState.isLoading && uiState.transactions.isEmpty() -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 48.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = MariPrimaryBlue,
                                            strokeWidth = 3.dp
                                        )
                                        Text(
                                            text = "Memuat riwayat transaksi...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MariMutedLight
                                        )
                                    }
                                }

                                uiState.transactions.isEmpty() -> {
                                    if (uiState.filter.isFilterActive) {
                                        FuelEmptyState(
                                            title = "Tidak Ada Hasil Pencarian",
                                            subtitle = "Tidak ditemukan transaksi yang cocok dengan kata kunci atau filter yang dipilih.",
                                            icon = Icons.Default.FilterList,
                                            actionButtonText = "Reset Filter",
                                            onActionClick = {
                                                viewModel.onEvent(TransactionUiEvent.QueryChanged(""))
                                                viewModel.onEvent(TransactionUiEvent.VehicleFilterChanged(null))
                                                viewModel.onEvent(TransactionUiEvent.FuelTypeFilterChanged(null))
                                            }
                                        )
                                    } else {
                                        FuelEmptyState(
                                            title = "Belum Ada Catatan Transaksi",
                                            subtitle = "Mulai pantau pengeluaran bahan bakar Anda dengan memindai struk SPBU atau mencatat secara manual.",
                                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                            actionButtonText = "＋ Catat BBM Sekarang",
                                            onActionClick = onNavigateToCreate
                                        )
                                    }
                                }

                                else -> {
                                    // Pengelompokan Transaksi per Bulan dengan Sub-Total Finansial
                                    val groupedByMonth = uiState.transactions.groupBy { tx ->
                                        val zdt = tx.transactionAt.atZone(DateUtils.ZONE_JAKARTA)
                                        val monthName = zdt.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("id", "ID"))
                                        "$monthName ${zdt.year}"
                                    }

                                    groupedByMonth.forEach { (monthYear, txList) ->
                                        val monthTotal = txList.sumOf { it.totalAmountIdr }
                                        val monthCount = txList.size

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Header Bulan & Sub-Total
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = monthYear,
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = MariTextLight,
                                                            fontSize = 15.sp
                                                        )
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = MariPrimaryBlue.copy(alpha = 0.10f)
                                                    ) {
                                                        Text(
                                                            text = "$monthCount Pengisian",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 11.sp,
                                                                color = MariPrimaryBlue
                                                            ),
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = "Total: ${CurrencyFormatter.formatIdr(monthTotal)}",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MariTextLight,
                                                        fontSize = 13.sp
                                                    )
                                                )
                                            }

                                            // Card Daftar Transaksi Bulan Ini
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = Color.White,
                                                shadowElevation = 2.dp,
                                                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    txList.forEachIndexed { index, transaction ->
                                                        val vehicle = uiState.availableVehicles.firstOrNull { it.id == transaction.vehicleId }
                                                        TransactionRowItem(
                                                            transaction = transaction,
                                                            vehicleName = vehicle?.name ?: "Kendaraan",
                                                            licensePlate = vehicle?.licensePlate,
                                                            onClick = { onNavigateToDetail(transaction.id) }
                                                        )
                                                        if (index < txList.lastIndex) {
                                                            HorizontalDivider(
                                                                color = Color.Black.copy(alpha = 0.05f),
                                                                thickness = 0.8.dp,
                                                                modifier = Modifier.padding(start = 76.dp, end = 16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (uiState.isLoadingMore) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MariPrimaryBlue,
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    if (showExportChoiceDialog) {
        ExportChoiceDialog(
            isProUser = uiState.isProUser,
            onDismiss = { showExportChoiceDialog = false },
            onExportPdf = {
                showExportChoiceDialog = false
                if (uiState.isProUser) {
                    viewModel.exportPdf(context = context) { pdfFile ->
                        sharePdfDocument(context, pdfFile)
                    }
                } else {
                    showProUpgradeDialog = true
                }
            },
            onExportCsv = {
                showExportChoiceDialog = false
                viewModel.exportCsv { csvContent ->
                    val fileName = "laporan_pengeluaran_bbm_${System.currentTimeMillis()}.csv"
                    pendingCsvContent = csvContent
                    createDocumentLauncher.launch(fileName)
                }
            }
        )
    }

    if (showProUpgradeDialog) {
        ProUpgradeExportPromoDialog(
            onDismiss = { showProUpgradeDialog = false },
            onUpgradeClick = {
                showProUpgradeDialog = false
                onNavigateToProPlans()
            }
        )
    }
}
}

/**
 * Bento 2-Kolom Analitik Finansial & Konsumsi BBM
 */
@Composable
private fun TransactionBentoSection(
    totalExpense: Long,
    totalVolume: Double,
    transactions: List<FuelTransaction>,
    modifier: Modifier = Modifier
) {
    val dominantStation = remember(transactions) {
        transactions.mapNotNull { it.stationName?.takeIf { s -> s.isNotBlank() } }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "SPBU Umum"
    }

    val dominantFuel = remember(transactions) {
        transactions.mapNotNull { it.fuelType?.takeIf { f -> f.isNotBlank() } }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "BBM Campuran"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bento 1: Anggaran / Pagu BBM Bulan Ini
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "Terkontrol",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(0xFF16A34A)
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Total Pengeluaran",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MariMutedLight,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = CurrencyFormatter.formatIdr(totalExpense),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MariTextLight,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar Visual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(MariPrimaryBlue, Color(0xFF38BDF8))
                                )
                            )
                    )
                }
            }
        }

        // Bento 2: SPBU & BBM Dominan
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = "%.1f L".format(totalVolume),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MariPrimaryBlue
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "BBM & SPBU Utama",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MariMutedLight,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = dominantFuel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MariTextLight,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = dominantStation,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MariMutedLight,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Badge Plat Nomor Mini Khas Indonesia
 */
@Composable
private fun IndonesianMiniPlateBadge(
    licensePlate: String?,
    modifier: Modifier = Modifier
) {
    if (licensePlate.isNullOrBlank()) return
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(0.8.dp, Color(0xFF475569)),
        modifier = modifier
    ) {
        Text(
            text = licensePlate.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                fontSize = 9.sp,
                color = Color.White
            ),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

/**
 * Badge Bahan Bakar dengan Warna Realistis
 */
@Composable
private fun RealisticFuelBadge(
    fuelType: String?,
    modifier: Modifier = Modifier
) {
    if (fuelType.isNullOrBlank()) return
    val (bgColor, textColor) = when {
        fuelType.contains("Pertamax", ignoreCase = true) || fuelType.contains("Turbo", ignoreCase = true) || fuelType.contains("V-Power", ignoreCase = true) ->
            Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
        fuelType.contains("Pertalite", ignoreCase = true) ->
            Color(0xFFF0FDF4) to Color(0xFF15803D)
        fuelType.contains("Solar", ignoreCase = true) || fuelType.contains("Dex", ignoreCase = true) || fuelType.contains("Diesel", ignoreCase = true) ->
            Color(0xFFFEFCE8) to Color(0xFFA16207)
        else ->
            Color(0xFFF1F5F9) to Color(0xFF475569)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = fuelType,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = textColor
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Baris Item Transaksi Ramping Ala GoPay Activity List
 */
@Composable
private fun TransactionRowItem(
    transaction: FuelTransaction,
    vehicleName: String,
    licensePlate: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOcr = transaction.scanStatus == "ocr" || transaction.scanStatus == "ai" || transaction.scanStatus == "ocr_review"
    val iconBgColor = if (isOcr) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)
    val iconTint = if (isOcr) MariPrimaryBlue else Color(0xFF475569)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Squircle Icon Container
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalGasStation,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 2. Info Detail (SPBU, Plat & Kendaraan, BBM Badge, Tanggal)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.stationName?.takeIf { it.isNotBlank() } ?: "SPBU Umum",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MariTextLight,
                    fontSize = 15.sp
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!licensePlate.isNullOrBlank()) {
                    IndonesianMiniPlateBadge(licensePlate = licensePlate)
                } else {
                    Text(
                        text = vehicleName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MariMutedLight,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                RealisticFuelBadge(fuelType = transaction.fuelType)

                transaction.volumeLiters?.let { liters ->
                    Text(
                        text = "%.1f L".format(liters),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MariMutedLight,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = DateUtils.formatDateTime(transaction.transactionAt),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 3. Nominal Pengeluaran & Status Tag Mini
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = CurrencyFormatter.formatIdr(transaction.totalAmountIdr),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MariTextLight,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isOcr) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (isOcr) "AI Scan" else "Manual",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = if (isOcr) MariPrimaryBlue else Color(0xFF64748B)
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ExportChoiceDialog(
    isProUser: Boolean,
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = MariPrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Ekspor Riwayat Transaksi",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Pilih format dokumen pengeluaran BBM yang ingin Anda hasilkan:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: PDF Resmi (Pro)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isProUser) MariPrimaryBlue else Color(0xFFF59E0B).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExportPdf() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF004D40).copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF004D40),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Dokumen PDF Resmi",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isProUser) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = if (isProUser) "PRO AKTIF" else "FITUR PRO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isProUser) Color(0xFF15803D) else Color(0xFFB45309)
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Laporan ber-kop resmi, ringkasan eksekutif, tabel transaksi rinci, dan tanda tangan pengesahan klaim/pajak.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: CSV Mentah (Free)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MariBorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExportCsv() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MariPrimaryBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = MariPrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Tabel Data CSV",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = "GRATIS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Format spreadsheet mentah standar (.csv) yang kompatibel dengan Microsoft Excel dan Google Sheets.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun ProUpgradeExportPromoDialog(
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Fitur Eksklusif Mari Pro",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ekspor Laporan PDF Formal Ber-Kop adalah fitur premium yang dirancang khusus untuk mempermudah klaim pengeluaran bahan bakar kantor (reimbursement) dan audit pajak.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Keunggulan Laporan PDF Resmi:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Kop resmi korporat & nomor dokumen sah\n• Ringkasan eksekutif finansial bulanan\n• Tabel pengeluaran rinci per kendaraan\n• Kolom tanda tangan pengesahan atasan",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = Color(0xFF78350F)
                        )
                    }
                }
                Text(
                    text = "Tingkatkan akun Anda ke Mari Pro untuk menikmati fitur ini dan kelola armada tanpa batas!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onUpgradeClick,
                colors = ButtonDefaults.buttonColors(containerColor = MariPrimaryBlue)
            ) {
                Text("Lihat Paket Pro")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Nanti Saja")
            }
        }
    )
}

private fun sharePdfDocument(context: Context, pdfFile: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Bagikan Laporan PDF Resmi")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
