package id.maripartner.fuel.feature.transactions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
    val listState = rememberLazyListState()
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
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 3
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

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Riwayat Pengeluaran BBM",
                actions = {
                    IconButton(
                        onClick = { showExportChoiceDialog = true },
                        enabled = !uiState.isExporting
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MariPrimaryBlue
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Ekspor Laporan",
                                tint = MariPrimaryBlue
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            val nextSort = if (uiState.filter.sortOrder == SortOrder.NEWEST_FIRST) {
                                SortOrder.OLDEST_FIRST
                            } else {
                                SortOrder.NEWEST_FIRST
                            }
                            viewModel.onEvent(TransactionUiEvent.SortOrderChanged(nextSort))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Urutkan (${uiState.filter.sortOrder.displayName})",
                            tint = MariTextLight
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(TransactionUiEvent.Refresh) },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Segarkan",
                            tint = MariTextLight
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MariPrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Catat BBM")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
            ) {
                // 1. Header Visual Sesuai Official Reference UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AKTIVITAS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MariPrimaryBlue
                        )
                        Text(
                            text = "Riwayat Transaksi",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MariTextLight
                            )
                        )
                        Text(
                            text = "Semua catatan pengisian BBM dalam satu tempat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MariMutedLight
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    FuelPrimaryButton(
                        text = "＋ Catat BBM",
                        onClick = onNavigateToCreate,
                        modifier = Modifier.width(136.dp)
                    )
                }

                // 2. Ringkasan Metrik Pengeluaran & Volume (Top Stat Cards)
                if (uiState.transactions.isNotEmpty()) {
                    FuelCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Total Pengeluaran
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = "Total Pengeluaran",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MariMutedLight
                                )
                                Text(
                                    text = CurrencyFormatter.formatIdr(totalExpense),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MariPrimaryBlue
                                    )
                                )
                                Text(
                                    text = "$totalCount transaksi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MariMutedLight
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(MariBorderLight)
                            )

                            // Total Volume
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = "Total Volume",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MariMutedLight
                                )
                                Text(
                                    text = "%.1f L".format(totalVolume),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MariBrandNavy
                                    )
                                )
                                Text(
                                    text = if (totalVolume > 0) "@ ${CurrencyFormatter.formatIdr((totalExpense / totalVolume).toLong())}/L" else "-",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MariMutedLight
                                )
                            }
                        }
                    }
                }

                // 3. Kolom Pencarian Cepat (Debounced)
                OutlinedTextField(
                    value = uiState.filter.query,
                    onValueChange = { viewModel.onEvent(TransactionUiEvent.QueryChanged(it)) },
                    placeholder = {
                        Text(
                            text = "Cari SPBU, jenis BBM, nominal...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MariMutedLight
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = MariMutedLight
                        )
                    },
                    trailingIcon = {
                        if (uiState.filter.query.isNotBlank()) {
                            IconButton(onClick = { viewModel.onEvent(TransactionUiEvent.QueryChanged("")) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus Pencarian",
                                    tint = MariMutedLight
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MariPrimaryBlue,
                        unfocusedBorderColor = MariBorderLight,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )

                // 4. Baris Filter Kendaraan Cepat
                if (uiState.availableVehicles.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.filter.vehicleId == null,
                                onClick = { viewModel.onEvent(TransactionUiEvent.VehicleFilterChanged(null)) },
                                label = { Text("Semua Kendaraan") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MariPrimaryBlue.copy(alpha = 0.12f),
                                    selectedLabelColor = MariPrimaryBlue
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = uiState.filter.vehicleId == null,
                                    borderColor = if (uiState.filter.vehicleId == null) MariPrimaryBlue else MariBorderLight
                                )
                            )
                        }
                        items(uiState.availableVehicles) { vehicle ->
                            val isSelected = uiState.filter.vehicleId == vehicle.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val newId = if (isSelected) null else vehicle.id
                                    viewModel.onEvent(TransactionUiEvent.VehicleFilterChanged(newId))
                                },
                                label = { Text(vehicle.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) MariPrimaryBlue else MariMutedLight
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MariPrimaryBlue.copy(alpha = 0.12f),
                                    selectedLabelColor = MariPrimaryBlue
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) MariPrimaryBlue else MariBorderLight
                                )
                            )
                        }
                    }
                }

                // 5. Konten Utama Berdasarkan Status UI
                when {
                    uiState.isLoading && uiState.transactions.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
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
                    }

                    uiState.transactions.isEmpty() -> {
                        if (uiState.filter.isFilterActive) {
                            FuelEmptyState(
                                title = "Tidak Ada Hasil Pencarian",
                                subtitle = "Tidak ditemukan transaksi yang cocok dengan kata kunci atau filter kendaraan yang dipilih.",
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
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Info Jumlah Transaksi
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Menampilkan ${uiState.transactions.size} transaksi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MariMutedLight
                                    )
                                    Text(
                                        text = uiState.filter.sortOrder.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MariPrimaryBlue
                                    )
                                }
                            }

                            items(
                                items = uiState.transactions,
                                key = { it.id }
                            ) { transaction ->
                                val vehicle = remember(transaction.vehicleId, uiState.availableVehicles) {
                                    uiState.availableVehicles.firstOrNull { it.id == transaction.vehicleId }
                                }
                                TransactionItemCard(
                                    transaction = transaction,
                                    vehicleName = vehicle?.name ?: "Kendaraan",
                                    licensePlate = vehicle?.licensePlate,
                                    onClick = { onNavigateToDetail(transaction.id) }
                                )
                            }

                            if (uiState.isLoadingMore) {
                                item {
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

/**
 * Kartu Item Transaksi Modern Sesuai Design System Mari Partner
 */
@Composable
private fun TransactionItemCard(
    transaction: FuelTransaction,
    vehicleName: String,
    licensePlate: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FuelCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Baris Atas: Tanggal Transaksi & Sumber (AI OCR / Manual)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDateTime(transaction.transactionAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MariMutedLight
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (transaction.scanStatus == "ocr" || transaction.scanStatus == "ai" || transaction.scanStatus == "ocr_review") {
                        FuelStatusChip(
                            text = "AI OCR",
                            type = FuelChipType.AiTag
                        )
                    } else {
                        FuelStatusChip(
                            text = "Manual",
                            type = FuelChipType.Neutral
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Baris Tengah: Kendaraan & Nominal Pengeluaran
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info Kendaraan
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MariPrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = vehicleName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MariTextLight
                            )
                        )
                        if (!licensePlate.isNullOrBlank()) {
                            Text(
                                text = licensePlate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MariMutedLight
                            )
                        }
                    }
                }

                // Nominal Total
                Text(
                    text = CurrencyFormatter.formatIdr(transaction.totalAmountIdr),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MariPrimaryBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Baris Bawah: SPBU, Volume & BBM Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lokasi / SPBU
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = MariMutedLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = transaction.stationName?.takeIf { it.isNotBlank() } ?: "SPBU Umum",
                        style = MaterialTheme.typography.bodySmall,
                        color = MariMutedLight,
                        maxLines = 1
                    )
                }

                // Volume & Jenis BBM
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    transaction.volumeLiters?.let { liters ->
                        Text(
                            text = "%.1f L".format(liters),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MariTextLight
                        )
                    }

                    if (!transaction.fuelType.isNullOrBlank()) {
                        FuelStatusChip(
                            text = transaction.fuelType,
                            type = FuelChipType.Info
                        )
                    }
                }
            }

            // Odometer jika tersedia
            if (transaction.odometerKm != null && transaction.odometerKm > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MariMutedLight
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Odometer: ${transaction.odometerKm} km",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                }
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
