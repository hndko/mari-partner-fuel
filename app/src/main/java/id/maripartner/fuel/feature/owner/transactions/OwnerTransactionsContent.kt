package id.maripartner.fuel.feature.owner.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.domain.model.owner.OwnerPaymentTx
import java.util.Locale

/**
 * Konsol Audit Mutasi Transaksi Pembayaran SaaS.
 * Menampilkan ringkasan omset settlement, pencarian real-time, filter status pembayaran,
 * dan modal rincian audit finansial berstandar enterprise.
 */
@Composable
fun OwnerTransactionsContent(
    transactions: List<OwnerPaymentTx>,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedStatusFilter by rememberSaveable { mutableStateOf("all") } // "all", "paid", "pending", "failed"
    var selectedTxForDetail by remember { mutableStateOf<OwnerPaymentTx?>(null) }

    // Hitung metrik agregat
    val totalPaidAmount = remember(transactions) {
        transactions
            .filter { it.status.equals("paid", ignoreCase = true) }
            .sumOf { it.amountIdr }
    }

    val totalPendingAmount = remember(transactions) {
        transactions
            .filter { it.status.equals("pending", ignoreCase = true) }
            .sumOf { it.amountIdr }
    }

    val paidCount = remember(transactions) {
        transactions.count { it.status.equals("paid", ignoreCase = true) }
    }

    val pendingCount = remember(transactions) {
        transactions.count { it.status.equals("pending", ignoreCase = true) }
    }

    val failedCount = remember(transactions) {
        transactions.count {
            !it.status.equals("paid", ignoreCase = true) && !it.status.equals("pending", ignoreCase = true)
        }
    }

    // Filter list transaksi secara reaktif
    val filteredTransactions = remember(transactions, searchQuery, selectedStatusFilter) {
        transactions.filter { tx ->
            // Filter Status
            val matchesStatus = when (selectedStatusFilter) {
                "paid" -> tx.status.equals("paid", ignoreCase = true)
                "pending" -> tx.status.equals("pending", ignoreCase = true)
                "failed" -> !tx.status.equals("paid", ignoreCase = true) && !tx.status.equals("pending", ignoreCase = true)
                else -> true
            }

            // Filter Search Query
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase(Locale.ROOT)
                tx.userDisplayName.lowercase(Locale.ROOT).contains(q) ||
                    (tx.userId ?: "").lowercase(Locale.ROOT).contains(q) ||
                    (tx.planName ?: "").lowercase(Locale.ROOT).contains(q) ||
                    tx.paymentReference.lowercase(Locale.ROOT).contains(q) ||
                    tx.provider.lowercase(Locale.ROOT).contains(q) ||
                    tx.status.lowercase(Locale.ROOT).contains(q)
            }

            matchesStatus && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner Eksekutif: Audit Mutasi Finansial
        item {
            TransactionsHeaderBanner()
        }

        // 2. Strip Ringkasan 3-KPI: Total Lunas, Pending, dan Settlement Rate
        item {
            TransactionsKpiSummaryStrip(
                totalCount = transactions.size,
                paidCount = paidCount,
                pendingCount = pendingCount,
                totalPaidAmount = totalPaidAmount,
                totalPendingAmount = totalPendingAmount
            )
        }

        // 3. Kolom Pencarian Real-Time & Filter Status Chip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari ref ID, pengguna, paket, atau provider...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari Transaksi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus Pencarian"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Filter Status Chips (Semua, Lunas, Pending, Gagal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == "all",
                        onClick = { selectedStatusFilter = "all" },
                        label = {
                            Text(
                                text = "Semua (${transactions.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedStatusFilter == "all") FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MariPrimaryBlue,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    FilterChip(
                        selected = selectedStatusFilter == "paid",
                        onClick = { selectedStatusFilter = "paid" },
                        label = {
                            Text(
                                text = "Lunas ($paidCount)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedStatusFilter == "paid") FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF16A34A),
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    FilterChip(
                        selected = selectedStatusFilter == "pending",
                        onClick = { selectedStatusFilter = "pending" },
                        label = {
                            Text(
                                text = "Pending ($pendingCount)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedStatusFilter == "pending") FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD97706),
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    if (failedCount > 0) {
                        FilterChip(
                            selected = selectedStatusFilter == "failed",
                            onClick = { selectedStatusFilter = "failed" },
                            label = {
                                Text(
                                    text = "Gagal ($failedCount)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selectedStatusFilter == "failed") FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDC2626),
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }

        // 4. Section Header & Counter Transaksi
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Mutasi Transaksi (${filteredTransactions.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (searchQuery.isNotBlank() || selectedStatusFilter != "all") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MariPrimaryBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Filter Aktif",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MariPrimaryBlue
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 5. Daftar Transaksi / Empty State
        if (filteredTransactions.isEmpty()) {
            item {
                EmptyTransactionsCard(
                    isFiltered = searchQuery.isNotBlank() || selectedStatusFilter != "all",
                    onResetFilter = {
                        searchQuery = ""
                        selectedStatusFilter = "all"
                    }
                )
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionItemCard(
                    tx = tx,
                    onDetailClicked = { selectedTxForDetail = tx }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Modal Dialog: Detail Audit Transaksi
    selectedTxForDetail?.let { tx ->
        TransactionDetailDialog(
            tx = tx,
            onDismiss = { selectedTxForDetail = null }
        )
    }
}

/**
 * Banner Eksekutif VIP untuk Halaman Audit Transaksi Masuk.
 */
@Composable
private fun TransactionsHeaderBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A), // Dark Slate 900
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MariPrimaryBlue.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.18f))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF10B981).copy(alpha = 0.35f),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "AUDIT MUTASI FINANSIAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 10.sp,
                            color = Color(0xFF34D399)
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFF10B981).copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = "SETTLEMENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF34D399)
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Arus Kas & Pembayaran SaaS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Rekam jejak mutasi pembayaran langganan, verifikasi webhook QRIS, dan riwayat settlement.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                )
            }
        }
    }
}

/**
 * Strip Ringkasan 3-KPI Finansial:
 * [TOTAL LUNAS] | [MENUNGGU SETTLEMENT] | [SETTLEMENT RATE]
 */
@Composable
private fun TransactionsKpiSummaryStrip(
    totalCount: Int,
    paidCount: Int,
    pendingCount: Int,
    totalPaidAmount: Long,
    totalPendingAmount: Long
) {
    val successRate = if (totalCount > 0) {
        String.format(Locale.ROOT, "%.1f", (paidCount.toDouble() / totalCount.toDouble()) * 100)
    } else {
        "0.0"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tile 1: Total Omset Lunas
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = Color(0xFF10B981).copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "TOTAL LUNAS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF10B981)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.formatIdr(totalPaidAmount),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFF16A34A)
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$paidCount Transaksi Selesai",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Tile 2: Menunggu Settlement / In-Flight
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = Color(0xFFF59E0B).copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "MENUNGGU BAYAR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFFD97706)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.formatIdr(totalPendingAmount),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFFD97706)
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$pendingCount Menunggu QRIS",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Tile 3: Rasio Konversi Sukses
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = MariPrimaryBlue.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "SETTLEMENT RATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MariPrimaryBlue
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$successRate% Sukses",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MariPrimaryBlue
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Dari $totalCount Order",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

/**
 * Kartu Item Transaksi Pembayaran dengan Desain Eksekutif.
 */
@Composable
private fun TransactionItemCard(
    tx: OwnerPaymentTx,
    onDetailClicked: () -> Unit
) {
    val isPaid = tx.status.equals("paid", ignoreCase = true)
    val isPending = tx.status.equals("pending", ignoreCase = true)

    FuelCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetailClicked)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Status Icon, Plan Name, Provider & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isPaid -> Color(0xFFDCFCE7)
                                    isPending -> Color(0xFFFEF3C7)
                                    else -> Color(0xFFFEE2E2)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    isPaid -> Color(0xFF16A34A).copy(alpha = 0.3f)
                                    isPending -> Color(0xFFD97706).copy(alpha = 0.3f)
                                    else -> Color(0xFFDC2626).copy(alpha = 0.3f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = when {
                                isPaid -> Icons.Default.CheckCircle
                                isPending -> Icons.Default.HourglassTop
                                else -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            tint = when {
                                isPaid -> Color(0xFF16A34A)
                                isPending -> Color(0xFFD97706)
                                else -> Color(0xFFDC2626)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = tx.planName ?: "Paket Langganan SaaS",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = tx.provider.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }

                            val ref = tx.paymentReference
                            if (ref.isNotBlank()) {
                                val refDisplay = if (ref.length > 10) ref.take(10) + "..." else ref
                                Text(
                                    text = "Ref: #$refDisplay",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                when {
                    isPaid -> FuelStatusChip(text = "LUNAS", type = FuelChipType.Success)
                    isPending -> FuelStatusChip(text = "MENUNGGU", type = FuelChipType.Warning)
                    else -> FuelStatusChip(text = tx.status.uppercase(), type = FuelChipType.Danger)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-metrics Grid (2 Columns: Pelanggan & Nominal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Box Pelanggan
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PELANGGAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tx.userDisplayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                        val uid = tx.userId
                        val uidDisplay = if (!uid.isNullOrBlank()) {
                            if (uid.length > 8) "${uid.take(8)}..." else uid
                        } else {
                            "-"
                        }
                        Text(
                            text = "UID: $uidDisplay",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Box Nominal
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "NOMINAL BAYAR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatIdr(tx.amountIdr),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (isPaid) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = if (isPaid) "Settlement Berhasil" else "Menunggu Pembayaran",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = if (isPaid) Color(0xFF16A34A) else Color(0xFFD97706)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Timestamp & Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rawDate = tx.paidAt ?: tx.createdAt ?: "-"
                val displayDate = if (rawDate.length >= 19) rawDate.take(19).replace("T", " ") else rawDate

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                OutlinedButton(
                    onClick = onDetailClicked,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MariPrimaryBlue
                    ),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Detail Audit",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Kartu Tampilan Kosong (Empty State) yang Ramah Pengguna.
 */
@Composable
private fun EmptyTransactionsCard(
    isFiltered: Boolean,
    onResetFilter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Icon(
                    imageVector = if (isFiltered) Icons.Default.Payments else Icons.Default.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isFiltered) "Mutasi Tidak Ditemukan" else "Belum Ada Riwayat Transaksi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isFiltered) {
                    "Tidak ada mutasi pembayaran yang cocok dengan kata kunci pencarian atau filter status yang dipilih."
                } else {
                    "Seluruh transaksi pembayaran paket langganan pengguna dari QRIS atau Payment Gateway akan otomatis tercatat dan tersinkronisasi di sini."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (isFiltered) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onResetFilter,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reset Pencarian & Filter")
                }
            }
        }
    }
}
