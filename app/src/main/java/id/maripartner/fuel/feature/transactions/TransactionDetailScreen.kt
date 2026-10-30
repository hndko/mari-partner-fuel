package id.maripartner.fuel.feature.transactions

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelDangerButton
import id.maripartner.fuel.core.designsystem.component.FuelErrorState
import id.maripartner.fuel.core.designsystem.component.FuelLoadingState
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.domain.model.FuelTransaction

@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Modal Dialog Konfirmasi Hapus Transaksi (SweetAlert Warning/Danger)
    FuelSweetAlertDialog(
        show = showDeleteConfirmDialog,
        type = SweetAlertType.ERROR,
        title = "Hapus Transaksi BBM?",
        message = "Apakah Anda yakin ingin menghapus data transaksi pengisian BBM ini? Catatan yang terhapus tidak dapat dikembalikan.",
        confirmText = if (uiState.isDeleting) "Menghapus..." else "Ya, Hapus Data",
        dismissText = "Batal",
        onConfirm = {
            viewModel.deleteTransaction()
            showDeleteConfirmDialog = false
        },
        onDismiss = {
            showDeleteConfirmDialog = false
        }
    )

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Detail Pengeluaran BBM",
                onNavigateBack = onNavigateBack,
                actions = {
                    if (uiState.transaction != null) {
                        IconButton(onClick = { onNavigateToEdit(transactionId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Transaksi",
                                tint = MariPrimaryBlue
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Transaksi",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                uiState.isLoading -> {
                    FuelLoadingState(
                        message = "Memuat rincian transaksi pengeluaran BBM..."
                    )
                }

                uiState.transaction == null -> {
                    FuelErrorState(
                        message = uiState.errorMessage ?: "Data transaksi tidak ditemukan atau telah dihapus.",
                        onRetry = { viewModel.loadTransaction(transactionId) }
                    )
                }

                else -> {
                    val tx = uiState.transaction!!

                    Column(
                        modifier = Modifier
                            .widthIn(max = 760.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Header Visual Sesuai Official UI Reference
                        Column {
                            Text(
                                text = "RINCIAN PENGELUARAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MariPrimaryBlue
                            )
                            Text(
                                text = "Transaksi Bahan Bakar",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MariTextLight
                                )
                            )
                            Text(
                                text = DateUtils.formatDateTime(tx.transactionAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MariMutedLight
                            )
                        }

                        // 2. Hero Card: Nominal Pengeluaran Utama
                        FuelCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MariPrimaryBlue.copy(alpha = 0.08f),
                                                Color.White
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "TOTAL PEMBAYARAN",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = MariMutedLight
                                        )

                                        if (tx.scanStatus == "ocr" || tx.scanStatus == "ai" || tx.scanStatus == "ocr_review") {
                                            FuelStatusChip(text = "AI OCR", type = FuelChipType.AiTag)
                                        } else {
                                            FuelStatusChip(text = "Input Manual", type = FuelChipType.Neutral)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = CurrencyFormatter.formatIdr(tx.totalAmountIdr),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MariPrimaryBlue
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        tx.volumeLiters?.let { liters ->
                                            Text(
                                                text = "%.2f Liter".format(liters),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MariBrandNavy
                                                )
                                            )
                                        }

                                        tx.effectivePricePerLiter?.let { price ->
                                            Text(
                                                text = "• @ ${CurrencyFormatter.formatIdr(price.toLong())} / L",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MariMutedLight
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Kartu Informasi Kendaraan
                        FuelCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
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
                                    Text(
                                        text = "Kendaraan Terhubung",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MariTextLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MariBorderLight)
                                Spacer(modifier = Modifier.height(14.dp))

                                DetailRowItem(
                                    label = "Nama Kendaraan",
                                    value = tx.vehicleName ?: "Kendaraan",
                                    isBold = true
                                )
                                if (!tx.vehiclePlate.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    DetailRowItem(
                                        label = "Nomor Polisi (Plat)",
                                        value = tx.vehiclePlate
                                    )
                                }
                            }
                        }

                        // 4. Kartu Rincian Pengisian BBM & SPBU
                        FuelCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF169B70).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            tint = Color(0xFF169B70),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Data Pengisian BBM",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MariTextLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MariBorderLight)
                                Spacer(modifier = Modifier.height(14.dp))

                                DetailRowItem(
                                    label = "Stasiun / SPBU",
                                    value = tx.stationName?.takeIf { it.isNotBlank() } ?: "SPBU Umum"
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                DetailRowItem(
                                    label = "Jenis Bahan Bakar",
                                    value = tx.fuelType?.takeIf { it.isNotBlank() } ?: "BBM Standar",
                                    chipType = FuelChipType.Info
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                DetailRowItem(
                                    label = "Volume Pengisian",
                                    value = tx.volumeLiters?.let { "%.2f Liter".format(it) } ?: "-"
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                DetailRowItem(
                                    label = "Harga per Liter",
                                    value = tx.effectivePricePerLiter?.let { "${CurrencyFormatter.formatIdr(it.toLong())} / Liter" } ?: "-"
                                )
                            }
                        }

                        // 5. Kartu Odometer & Perjalanan
                        if (tx.odometerKm != null && tx.odometerKm > 0) {
                            FuelCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFC9871C).copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Speed,
                                                contentDescription = null,
                                                tint = Color(0xFFC9871C),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Odometer & Jarak Tempuh",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MariTextLight
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = MariBorderLight)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    DetailRowItem(
                                        label = "Odometer saat Isi",
                                        value = "${tx.odometerKm.toInt()} km",
                                        isBold = true
                                    )
                                }
                            }
                        }

                        // 6. Kartu Catatan / Memo (Jika ada)
                        if (!tx.notes.isNullOrBlank()) {
                            FuelCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MariMutedLight.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Notes,
                                                contentDescription = null,
                                                tint = MariMutedLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Catatan Tambahan",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MariTextLight
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = MariBorderLight)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = tx.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MariTextLight
                                    )
                                }
                            }
                        }

                        // 7. Info Privasi Penyimpanan Ephemeral (Sesuai Standar Proyek)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MariPrimaryBlue.copy(alpha = 0.06f))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MariPrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Privasi Terjaga: Foto struk diproses sementara untuk scan OCR teks. Data pengeluaran tersimpan aman sebagai angka & teks di PostgreSQL dengan RLS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MariMutedLight
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 8. Tombol Aksi Bawah: Edit & Hapus
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FuelSecondaryButton(
                                text = "Edit Transaksi",
                                onClick = { onNavigateToEdit(transactionId) },
                                leadingIcon = Icons.Default.Edit,
                                modifier = Modifier.weight(1f)
                            )

                            FuelDangerButton(
                                text = "Hapus Transaksi",
                                onClick = { showDeleteConfirmDialog = true },
                                leadingIcon = Icons.Default.Delete,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRowItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    chipType: FuelChipType? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MariMutedLight
        )

        if (chipType != null) {
            FuelStatusChip(text = value, type = chipType)
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    color = MariTextLight
                )
            )
        }
    }
}
