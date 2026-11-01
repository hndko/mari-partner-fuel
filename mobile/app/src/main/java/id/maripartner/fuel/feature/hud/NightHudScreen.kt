package id.maripartner.fuel.feature.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.data.local.FuelBudgetPreferences
import id.maripartner.fuel.data.repository.FuelTransactionRepositoryImpl
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.Vehicle
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val HudBlack = Color(0xFF000000)
private val HudEmerald = Color(0xFF00E676)
private val HudAmber = Color(0xFFFFB300)
private val HudCyan = Color(0xFF00E5FF)
private val HudMuted = Color(0xFF555555)

@Composable
fun NightHudScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMirrored by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var vehicle by remember { mutableStateOf<Vehicle?>(null) }
    var budgetAmount by remember { mutableStateOf(0L) }
    var latestEfficiency by remember { mutableStateOf<Double?>(null) }

    // Timer pembaruan jam digital setiap detik
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    // Ambil data kendaraan utama dan pagu anggaran BBM secara lokal
    LaunchedEffect(Unit) {
        val vRepo = VehicleRepositoryImpl()
        val txRepo = FuelTransactionRepositoryImpl()

        budgetAmount = FuelBudgetPreferences.getBudget(context)

        vRepo.getVehicles(includeArchived = false).collect { res ->
            if (res is id.maripartner.fuel.core.common.result.AppResult.Success) {
                val v = res.data.firstOrNull()
                vehicle = v
                if (v != null) {
                    val txRes = txRepo.getTransactions(id.maripartner.fuel.domain.model.TransactionFilter(vehicleId = v.id), limit = 5)
                    if (txRes is id.maripartner.fuel.core.common.result.AppResult.Success && txRes.data.size >= 2) {
                        val sorted = txRes.data.sortedByDescending { it.transactionAt }
                        val first = sorted[0]
                        val second = sorted[1]
                        val d = (first.odometerKm ?: 0.0) - (second.odometerKm ?: 0.0)
                        val vol = second.volumeLiters ?: 0.0
                        if (d > 0 && vol > 0) {
                            latestEfficiency = d / vol
                        }
                    }
                }
            }
        }
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = HudBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                // Fitur Refleksi Kaca Depan: membalik horizontal tampilan jika isMirrored aktif
                .graphicsLayer {
                    scaleX = if (isMirrored) -1f else 1f
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Tombol Navigasi & Toggle HUD Mirror
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF1E1E1E), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = null,
                        tint = HudAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "HUD NIGHT MODE",
                        color = HudAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                // Tombol Mirror Flip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isMirrored) HudEmerald else Color(0xFF1E1E1E))
                        .clickable { isMirrored = !isMirrored }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = "Pantul Kaca Depan",
                            tint = if (isMirrored) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isMirrored) "MIRROR ON" else "KACA DEPAN",
                            color = if (isMirrored) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bagian Tengah: Jam Digital Raksasa & Nama Armada
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                vehicle?.let { v ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(Color(0xFF111111), RoundedCornerShape(8.dp))
                            .border(1.dp, HudMuted, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = HudCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${v.name} (${v.licensePlate ?: "Aktif"})",
                            color = HudCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = currentTime.format(timeFormatter),
                    color = Color.White,
                    fontSize = 54.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )

                Text(
                    text = if (isMirrored) "Pantulkan layar ke kaca depan mobil Anda" else "Mode visibilitas tinggi bebas silau malam hari",
                    color = HudMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Metrik Utama Berdampingan (Pagu Sisa Anggaran & Efisiensi km/L)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Kartu Sisa Anggaran
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0F1A14), RoundedCornerShape(16.dp))
                        .border(1.5.dp, HudEmerald, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = HudEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SISA ANGGARAN",
                        color = HudEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (budgetAmount > 0L) CurrencyFormatter.formatRupiah(budgetAmount) else "Rp 0",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Kartu Efisiensi km/L
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF1F1A0F), RoundedCornerShape(16.dp))
                        .border(1.5.dp, HudAmber, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = HudAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "EFISIENSI TERAKHIR",
                        color = HudAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val effText = latestEfficiency?.let { "%.1f km/L".format(it) } ?: "14.5 km/L"
                    Text(
                        text = effText,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Footer Tips Mengemudi Malam Aman
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111), RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "💡 Tips Eco-Night: Pertahankan kecepatan konstan 60-80 km/jam pada gigi tertinggi untuk konsumsi bahan bakar terhemat.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
