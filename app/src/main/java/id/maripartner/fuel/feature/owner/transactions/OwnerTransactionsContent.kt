package id.maripartner.fuel.feature.owner.transactions

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.domain.model.owner.OwnerPaymentTx

@Composable
fun OwnerTransactionsContent(
    transactions: List<OwnerPaymentTx>,
    modifier: Modifier = Modifier
) {
    val totalPaidAmount = transactions
        .filter { it.status.equals("paid", ignoreCase = true) }
        .sumOf { it.amountIdr }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Ringkasan Transaksi Masuk
        item {
            TransactionSummaryBanner(
                totalCount = transactions.size,
                totalPaidAmount = totalPaidAmount
            )
        }

        if (transactions.isEmpty()) {
            item {
                EmptyTransactionsCard()
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionItemCard(tx = tx)
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TransactionSummaryBanner(
    totalCount: Int,
    totalPaidAmount: Long
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MariPrimaryBlue.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MariPrimaryBlue.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = MariPrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Total Pemasukan Berhasil",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatIdr(totalPaidAmount),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MariPrimaryBlue
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Berdasarkan $totalCount transaksi tercatat di sistem",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TransactionItemCard(tx: OwnerPaymentTx) {
    val isPaid = tx.status.equals("paid", ignoreCase = true)
    val isPending = tx.status.equals("pending", ignoreCase = true)

    FuelCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Plan name & Status Chip
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
                            .background(
                                when {
                                    isPaid -> Color(0xFFDCFCE7)
                                    isPending -> Color(0xFFFEF3C7)
                                    else -> Color(0xFFFEE2E2)
                                }
                            )
                    ) {
                        Icon(
                            imageVector = when {
                                isPaid -> Icons.Default.CheckCircle
                                isPending -> Icons.Default.HourglassTop
                                else -> Icons.Default.ReceiptLong
                            },
                            contentDescription = null,
                            tint = when {
                                isPaid -> Color(0xFF16A34A)
                                isPending -> Color(0xFFD97706)
                                else -> Color(0xFFDC2626)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tx.planName ?: "Langganan SaaS",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Metode: ${tx.provider.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                when {
                    isPaid -> FuelStatusChip(
                        text = "LUNAS",
                        type = FuelChipType.Success
                    )
                    isPending -> FuelStatusChip(
                        text = "PENDING",
                        type = FuelChipType.Warning
                    )
                    else -> FuelStatusChip(
                        text = tx.status.uppercase(),
                        type = FuelChipType.Danger
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount and User info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Pengguna:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tx.userDisplayName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    val uid = tx.userId
                    val uidDisplay = if (!uid.isNullOrBlank()) {
                        if (uid.length > 8) "${uid.take(8)}..." else uid
                    } else {
                        "-"
                    }
                    Text(
                        text = "UID: $uidDisplay",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = CurrencyFormatter.formatIdr(tx.amountIdr),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Timestamp & Reference
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rawDate = tx.paidAt ?: tx.createdAt ?: "-"
                val displayDate = if (rawDate.length >= 19) rawDate.take(19).replace("T", " ") else rawDate
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val ref = tx.paymentReference
                if (!ref.isNullOrBlank()) {
                    val refDisplay = if (ref.length > 12) ref.take(12) else ref
                    Text(
                        text = "Ref: $refDisplay",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum Ada Riwayat Transaksi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Transaksi pembayaran langganan pengguna akan tampil secara otomatis di sini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
