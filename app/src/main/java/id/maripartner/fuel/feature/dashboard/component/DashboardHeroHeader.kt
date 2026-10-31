package id.maripartner.fuel.feature.dashboard.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.domain.model.FuelBudgetReport
import id.maripartner.fuel.domain.model.FuelTransaction

/**
 * Header Hero modern dengan latar belakang gradasi Deep Navy ke Electric Blue bergaya GoPay.
 * Mengintegrasikan Top Bar, Kartu Saldo Finansial Utama, Tombol Aksi Cepat "Catat",
 * Sub-banner Ticker transaksi terbaru, dan Baris Chip Status Armada & Pagu.
 */
@Composable
fun DashboardHeroHeader(
    userName: String,
    isProUser: Boolean,
    planName: String,
    totalExpensesMonthIdr: Long,
    recentTransaction: FuelTransaction?,
    budgetReport: FuelBudgetReport?,
    vehiclesCount: Int,
    avgEfficiency: Double?,
    onNavigateToProPlans: () -> Unit,
    onNavigateToTransactionForm: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onOpenBudgetDialog: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onHelpClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                MariBrandNavy,
                MariPrimaryBlue.copy(alpha = 0.95f),
                MariPrimaryBlue
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = gradientBrush)
            .statusBarsPadding()
            .padding(top = 10.dp, bottom = 26.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // 1. Top Bar: Avatar Akun, Pill Status Proteksi Pro/Free, dan Tombol Bantuan
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Avatar Profil Lingkaran
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White)
                        ) {
                            onNavigateToVehicles()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val initial = userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                    Text(
                        text = initial,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                // Center Pill: Status Langganan Pro / Free
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White)
                        ) {
                            onNavigateToProPlans()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isProUser) Color(0xFF22C55E) else MariAccentCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isProUser) "Mari Pro Aktif ›" else "Paket Free (Upgrade) ›",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Tombol Bantuan / Panduan Cepat
                IconButton(
                    onClick = onHelpClicked,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Panduan & Bantuan",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Metrik Finansial Utama & Tombol Aksi Cepat "Catat BBM"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Pengeluaran Bulan Ini",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "Rp",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 3.dp, end = 4.dp)
                        )
                        Text(
                            text = CurrencyFormatter.formatRupiah(totalExpensesMonthIdr).removePrefix("Rp "),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                // Tombol Aksi Kanan: Catat BBM (Bergaya tombol 'Cairkan' GoPay)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White)
                        ) {
                            onNavigateToTransactionForm()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Catat BBM",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Sub-banner Ticker Transaksi Terkini
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) {
                        onNavigateToTransactions()
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            if (recentTransaction != null) {
                                val amountStr = CurrencyFormatter.formatRupiah(recentTransaction.totalAmountIdr)
                                val fuelType = recentTransaction.fuelType ?: "BBM"
                                Text(
                                    text = "Transaksi Terakhir: $amountStr",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${recentTransaction.stationName ?: "SPBU"} · $fuelType · Selesai",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = "Belum Ada Catatan BBM Bulan Ini",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mulai scan struk atau catat pengeluaran Anda",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Lihat Riwayat",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Baris Chip Horizontal (Pagu Anggaran, Efisiensi Rata-rata, dan Jumlah Kendaraan)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip Pagu Anggaran
                val remaining = budgetReport?.remainingBudgetIdr ?: 0L
                val paguText = if (remaining > 0L) {
                    "Pagu: ${CurrencyFormatter.formatRupiah(remaining)}"
                } else {
                    "Atur Pagu BBM"
                }

                HeroStatusChip(
                    icon = Icons.Default.TrackChanges,
                    label = "$paguText ›",
                    onClick = onOpenBudgetDialog
                )

                // Chip Efisiensi Rata-rata
                val effText = avgEfficiency?.let { "%.1f km/L".format(it) } ?: "14.2 km/L"
                HeroStatusChip(
                    icon = Icons.Default.Speed,
                    label = "Efisiensi: $effText ›",
                    onClick = onNavigateToAnalytics
                )

                // Chip Armada Kendaraan
                HeroStatusChip(
                    icon = Icons.Default.DirectionsCar,
                    label = "Armada: $vehiclesCount Unit ›",
                    onClick = onNavigateToVehicles
                )
            }
        }
    }
}

@Composable
private fun HeroStatusChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .border(0.8.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White)
            ) {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
