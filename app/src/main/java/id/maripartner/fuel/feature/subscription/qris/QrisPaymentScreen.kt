package id.maripartner.fuel.feature.subscription.qris

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.domain.model.PaymentStatus
import id.maripartner.fuel.domain.usecase.payment.GetPaymentStatusUseCase
import id.maripartner.fuel.domain.usecase.subscription.GetMyEntitlementsUseCase
import kotlinx.coroutines.launch

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
                        snackbarHostState.showSnackbar("Pembayaran masih berstatus PENDING. Silakan selesaikan pembayaran.")
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
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Timer & Ref Banner
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.padding(start = 6.dp))
                            Text(
                                text = "Batas Waktu: 15 Menit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = paymentReference,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // QR Code Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Pindai Kode QRIS untuk Membayar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            // Tampilan Visual QRIS Box
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "QRIS Code",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(140.dp)
                                    )
                                    Text(
                                        text = "QRIS Dinamis Resmi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            paymentStatus?.let { status ->
                                Text(
                                    text = CurrencyFormatter.formatIdr(status.amountIdr),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "Mendukung BCA, Mandiri, BRI, BNI, GoPay, OVO, Dana, ShopeePay, LinkAja & Seluruh Aplikasi Berstandar QRIS.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Petunjuk Pembayaran
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Petunjuk Pembayaran:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "1. Buka aplikasi m-Banking atau e-Wallet favorit Anda.", style = MaterialTheme.typography.bodySmall)
                            Text(text = "2. Pilih menu Bayar / Scan QRIS lalu arahkan kamera ke kode di atas.", style = MaterialTheme.typography.bodySmall)
                            Text(text = "3. Pastikan nama merchant 'Mari Partner' dan nominal sudah sesuai.", style = MaterialTheme.typography.bodySmall)
                            Text(text = "4. Masukkan PIN dan selesaikan transaksi.", style = MaterialTheme.typography.bodySmall)
                            Text(text = "5. Ketuk tombol 'Periksa Status Pembayaran' di bawah.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Tombol Aksi
                item {
                    FuelPrimaryButton(
                        text = "Periksa Status Pembayaran",
                        onClick = { checkStatus() },
                        isLoading = isCheckingStatus
                    )
                }

                item {
                    FuelSecondaryButton(
                        text = "Kembali ke Beranda",
                        onClick = onNavigateBack
                    )
                }
            }
        }
    }
}
