package id.maripartner.fuel.feature.subscription.qris

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.core.designsystem.theme.MariWarning
import id.maripartner.fuel.domain.model.PaymentStatus
import id.maripartner.fuel.domain.usecase.payment.GetPaymentStatusUseCase
import id.maripartner.fuel.domain.usecase.subscription.GetMyEntitlementsUseCase
import kotlinx.coroutines.launch

/**
 * Layar Pembayaran QRIS Dinamis — Desain Resmi Mari Partner Fuel
 */
@Composable
fun QrisPaymentScreen(
    paymentReference: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: (paymentReference: String) -> Unit,
    getPaymentStatusUseCase: GetPaymentStatusUseCase = remember { GetPaymentStatusUseCase() },
    getMyEntitlementsUseCase: GetMyEntitlementsUseCase = remember { GetMyEntitlementsUseCase() }
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var paymentStatus by remember { mutableStateOf<PaymentStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isCheckingStatus by remember { mutableStateOf(false) }

    fun checkStatus() {
        isCheckingStatus = true
        coroutineScope.launch {
            when (val res = getPaymentStatusUseCase(paymentReference)) {
                is AppResult.Success -> {
                    paymentStatus = res.data
                    isCheckingStatus = false
                    if (res.data.isPaid) {
                        getMyEntitlementsUseCase(forceRefresh = true)
                        onPaymentSuccess(paymentReference)
                    } else {
                        snackbarHostState.showSnackbar("Pembayaran masih berstatus PENDING. Silakan selesaikan pembayaran di aplikasi e-Wallet atau perbankan Anda.")
                    }
                }
                is AppResult.Error -> {
                    isCheckingStatus = false
                    snackbarHostState.showSnackbar(res.exception.message)
                }
            }
        }
    }

    LaunchedEffect(paymentReference) {
        isLoading = true
        when (val res = getPaymentStatusUseCase(paymentReference)) {
            is AppResult.Success -> {
                paymentStatus = res.data
                isLoading = false
                if (res.data.isPaid) {
                    onPaymentSuccess(paymentReference)
                }
            }
            is AppResult.Error -> {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Pembayaran QRIS",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = MariPrimaryBlue,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Menghasilkan kode QRIS resmi…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MariMutedLight
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Area
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    ) {
                        Text(
                            text = "PEMBAYARAN QRIS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MariAccentCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pindai Kode QRIS",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MariBrandNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Buka aplikasi perbankan atau e-Wallet favorit Anda, lalu arahkan kamera ke kode QRIS di bawah.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MariMutedLight
                        )
                    }
                }

                // Timer & Ref Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MariBorderLight.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MariBorderLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MariWarning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Batas Waktu: 15 Menit",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MariTextLight
                                )
                            }
                            Text(
                                text = paymentReference,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MariMutedLight
                            )
                        }
                    }
                }

                // QR Code Card
                item {
                    FuelCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp),
                        cornerRadius = 18.dp,
                        contentPadding = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FuelStatusChip(text = "STANDAR QRIS NASIONAL", type = FuelChipType.AiTag)

                            // Tampilan Visual QRIS Box
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(2.dp, MariPrimaryBlue, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "QRIS Code",
                                        tint = MariBrandNavy,
                                        modifier = Modifier.size(150.dp)
                                    )
                                    Text(
                                        text = "MARI PARTNER FUEL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            letterSpacing = 1.sp
                                        ),
                                        color = MariPrimaryBlue
                                    )
                                }
                            }

                            paymentStatus?.let { status ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Total yang harus dibayar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MariMutedLight
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = CurrencyFormatter.formatIdr(status.amountIdr),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp
                                        ),
                                        color = MariPrimaryBlue
                                    )
                                }
                            }

                            Text(
                                text = "Mendukung BCA, Mandiri, BRI, BNI, GoPay, OVO, Dana, ShopeePay, LinkAja & Seluruh Aplikasi Berstandar QRIS.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                textAlign = TextAlign.Center,
                                color = MariMutedLight,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Petunjuk Pembayaran
                item {
                    FuelCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp),
                        cornerRadius = 14.dp,
                        contentPadding = 18.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PETUNJUK PEMBAYARAN",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MariMutedLight
                            )
                            PaymentInstructionStep(number = "1", text = "Buka aplikasi perbankan atau e-Wallet favorit Anda.")
                            PaymentInstructionStep(number = "2", text = "Pilih menu Bayar / Scan QRIS lalu arahkan kamera ke kode di atas.")
                            PaymentInstructionStep(number = "3", text = "Pastikan nama merchant 'Mari Partner Fuel' dan nominal sesuai.")
                            PaymentInstructionStep(number = "4", text = "Masukkan PIN keamanan untuk menyelesaikan pembayaran.")
                            PaymentInstructionStep(number = "5", text = "Ketuk tombol 'Periksa Status Pembayaran' di bawah.")
                        }
                    }
                }

                // Tombol Aksi
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FuelPrimaryButton(
                            text = "Periksa Status Pembayaran",
                            onClick = { checkStatus() },
                            isLoading = isCheckingStatus,
                            modifier = Modifier.fillMaxWidth()
                        )

                        FuelSecondaryButton(
                            text = "Kembali ke Beranda",
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentInstructionStep(number: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MariPrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = MariPrimaryBlue
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MariTextLight,
            modifier = Modifier.weight(1f)
        )
    }
}
