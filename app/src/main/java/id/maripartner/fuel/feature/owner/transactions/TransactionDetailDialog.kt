package id.maripartner.fuel.feature.owner.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.domain.model.owner.OwnerPaymentTx

/**
 * Modal dialog eksekutif untuk melihat rincian lengkap jejak audit mutasi transaksi pembayaran.
 */
@Composable
fun TransactionDetailDialog(
    tx: OwnerPaymentTx,
    onDismiss: () -> Unit
) {
    val isPaid = tx.status.equals("paid", ignoreCase = true)
    val isPending = tx.status.equals("pending", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                            else -> Icons.AutoMirrored.Filled.ReceiptLong
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
                        text = "Audit Mutasi Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = "Jejak Finansial & Settlement",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status & Nominal Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when {
                        isPaid -> Color(0xFF16A34A).copy(alpha = 0.08f)
                        isPending -> Color(0xFFD97706).copy(alpha = 0.08f)
                        else -> Color(0xFFDC2626).copy(alpha = 0.08f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL SETTLEMENT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatIdr(tx.amountIdr),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        isPaid -> Color(0xFF16A34A)
                                        isPending -> Color(0xFFD97706)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            )
                        }

                        when {
                            isPaid -> FuelStatusChip(text = "LUNAS", type = FuelChipType.Success)
                            isPending -> FuelStatusChip(text = "MENUNGGU", type = FuelChipType.Warning)
                            else -> FuelStatusChip(text = tx.status.uppercase(), type = FuelChipType.Danger)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Detail Data Audit
                AuditDetailRow(label = "ID Transaksi (UUID)", value = tx.id, isMonospace = true)
                AuditDetailRow(
                    label = "Referensi Pembayaran",
                    value = tx.paymentReference.ifBlank { "-" },
                    isMonospace = true
                )
                AuditDetailRow(label = "Nama Paket SaaS", value = tx.planName ?: "Mari Pro")
                AuditDetailRow(label = "Nama Pengguna", value = tx.userDisplayName)
                AuditDetailRow(
                    label = "User ID Pemesan",
                    value = tx.userId?.ifBlank { "-" } ?: "-",
                    isMonospace = true
                )
                AuditDetailRow(label = "Metode / Gateway", value = "${tx.provider.uppercase()} Gateway")

                val displayCreated = DateUtils.formatIsoDateTime(tx.createdAt)
                AuditDetailRow(label = "Waktu Pembuatan", value = displayCreated)

                val displayPaid = DateUtils.formatIsoDateTime(tx.paidAt)
                AuditDetailRow(label = "Waktu Settlement", value = if (isPaid) displayPaid else "Belum Selesai")

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Security & Audit Compliance Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Idempotensi Webhook & PostgreSQL RLS Terverifikasi Server",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Tutup",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MariPrimaryBlue
                    )
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AuditDetailRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
