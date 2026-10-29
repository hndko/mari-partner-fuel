package id.maripartner.fuel.feature.transactions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.SortOrder

@Composable
fun TransactionsScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var pendingCsvContent by remember { mutableStateOf<String?>(null) }

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

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Riwayat Pengeluaran BBM",
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.exportCsv { csvString ->
                                pendingCsvContent = csvString
                                val defaultFileName = "mari-partner-fuel-transaksi-${java.time.LocalDate.now()}.csv"
                                createDocumentLauncher.launch(defaultFileName)
                            }
                        },
                        enabled = !uiState.isExporting
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Ekspor CSV"
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
                            contentDescription = "Urutkan (${uiState.filter.sortOrder.displayName})"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Kolom Pencarian SPBU (Debounced)
            OutlinedTextField(
                value = uiState.filter.query,
                onValueChange = { viewModel.onEvent(TransactionUiEvent.QueryChanged(it)) },
                placeholder = { Text("Cari nama SPBU atau lokasi...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.filter.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.onEvent(TransactionUiEvent.QueryChanged("")) }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus Pencarian")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Baris Filter Kendaraan Cepat
            if (uiState.availableVehicles.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.filter.vehicleId == null,
                            onClick = { viewModel.onEvent(TransactionUiEvent.VehicleFilterChanged(null)) },
                            label = { Text("Semua Kendaraan") }
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
                                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
                            }
                        )
                    }
                }
            }

            // Konten Berdasarkan Status UI
            when {
                uiState.isLoading && uiState.transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.transactions.isEmpty() -> {
                    if (uiState.filter.isFilterActive) {
                        FuelEmptyState(
                            title = "Tidak Ada Hasil",
                            subtitle = "Tidak ditemukan transaksi BBM yang sesuai dengan kata kunci atau filter yang Anda pilih.",
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
                            title = "Riwayat Transaksi Masih Kosong",
                            subtitle = "Semua catatan pengisian bahan bakar Anda akan tersimpan rapi dan dapat dicari di sini.",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            actionButtonText = "Tambah Pengeluaran BBM",
                            onActionClick = onNavigateToCreate
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.transactions,
                            key = { it.id }
                        ) { transaction ->
                            TransactionItemCard(
                                transaction = transaction,
                                onClick = { onNavigateToDetail(transaction.id) }
                            )
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItemCard(
    transaction: FuelTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Baris Atas: Tanggal & Indikator Struk
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDateTime(transaction.transactionAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (transaction.hasReceipt) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Ada Struk",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Struk",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Baris Tengah: Nominal Rupiah & Kendaraan
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyFormatter.formatIdr(transaction.totalAmountIdr),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = transaction.vehicleName ?: "Kendaraan",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Baris Bawah: Detail Volume, Jenis BBM & Lokasi SPBU
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (transaction.volumeLiters != null && transaction.volumeLiters > 0) {
                    Text(
                        text = CurrencyFormatter.formatLiters(transaction.volumeLiters),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (!transaction.fuelType.isNullOrBlank()) {
                    Text(
                        text = transaction.fuelType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!transaction.stationName.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = transaction.stationName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
