package id.maripartner.fuel.feature.subscription.checkout

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.domain.model.PaymentProviderType
import id.maripartner.fuel.domain.model.SubscriptionPlan

/**
 * Layar Checkout Pembayaran Resmi — Desain Resmi Mari Partner Fuel
 */
@Composable
fun CheckoutScreen(
    planCode: String,
    onNavigateBack: () -> Unit,
    onNavigateToQris: (paymentReference: String) -> Unit,
    onNavigateToSuccess: (paymentReference: String) -> Unit,
    viewModel: CheckoutViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(planCode) {
        viewModel.loadPlan(planCode)
    }

    LaunchedEffect(uiState.createdOrder) {
        val order = uiState.createdOrder
        if (order != null) {
            if (uiState.selectedProvider == PaymentProviderType.QRISPY) {
                onNavigateToQris(order.paymentReference)
            } else {
                viewModel.onEvent(
                    CheckoutUiEvent.HandlePlayPurchaseSuccess(
                        purchaseToken = "play_token_${order.paymentReference}",
                        orderId = "GPA.${order.paymentReference}",
                        productId = order.productId ?: "mari_pro",
                        basePlanId = order.basePlanId ?: "pro-monthly"
                    )
                )
            }
        }
    }

    LaunchedEffect(uiState.isPaymentSuccess) {
        if (uiState.isPaymentSuccess) {
            val ref = uiState.createdOrder?.paymentReference ?: "SUCCESS"
            onNavigateToSuccess(ref)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CheckoutUiEvent.DismissError)
        }
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Checkout Pembayaran",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            uiState.plan?.let { plan ->
                CheckoutBottomBar(
                    totalAmountIdr = plan.priceIdr,
                    isLoading = uiState.isCreatingOrder,
                    onCheckoutClick = { viewModel.onEvent(CheckoutUiEvent.StartCheckout) }
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
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
                        text = "Memuat rincian paket…",
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
                // 1. Header Ringkasan
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                    ) {
                        Text(
                            text = "PEMBAYARAN RESMI",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MariAccentCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Selesaikan Langganan",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MariBrandNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pilih kanal pembayaran resmi untuk mengaktifkan akses Mari Pro secara instan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MariMutedLight
                        )
                    }
                }

                // 2. Ringkasan Paket yang Dipilih
                item {
                    uiState.plan?.let { plan ->
                        Box(modifier = Modifier.widthIn(max = 760.dp)) {
                            PlanSummaryCard(plan = plan)
                        }
                    }
                }

                // 3. Pemilihan Metode Pembayaran
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PILIH METODE PEMBAYARAN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MariMutedLight
                        )

                        PaymentMethodCard(
                            title = "Google Play Billing",
                            badgeText = "Play Store",
                            badgeType = FuelChipType.Info,
                            description = "Pembayaran aman menggunakan akun Google Play, Saldo Play, Kartu Kredit/Debit, atau GoPay/Dana terdaftar.",
                            icon = Icons.Default.Payment,
                            isSelected = uiState.selectedProvider == PaymentProviderType.GOOGLE_PLAY,
                            onSelect = { viewModel.onEvent(CheckoutUiEvent.SelectProvider(PaymentProviderType.GOOGLE_PLAY)) }
                        )

                        PaymentMethodCard(
                            title = "QRIS Dinamis (QRISpy)",
                            badgeText = "Direct / Instant",
                            badgeType = FuelChipType.AiTag,
                            description = "Scan instan via BCA Mobile, Mandiri Livin, BRImo, GoPay, OVO, Dana, ShopeePay, & seluruh bank QRIS.",
                            icon = Icons.Default.QrCode,
                            isSelected = uiState.selectedProvider == PaymentProviderType.QRISPY,
                            onSelect = { viewModel.onEvent(CheckoutUiEvent.SelectProvider(PaymentProviderType.QRISPY)) }
                        )
                    }
                }

                // 4. Rincian Biaya
                item {
                    uiState.plan?.let { plan ->
                        Box(modifier = Modifier.widthIn(max = 760.dp)) {
                            PricingBreakdownCard(priceIdr = plan.priceIdr)
                        }
                    }
                }

                // 5. Catatan Kebijakan
                item {
                    Box(modifier = Modifier.widthIn(max = 760.dp)) {
                        PolicyNoticeCard()
                    }
                }
            }
        }
    }
}

/**
 * Kartu Ringkasan Paket
 */
@Composable
private fun PlanSummaryCard(plan: SubscriptionPlan) {
    FuelCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = 18.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Paket Dipilih",
                        style = MaterialTheme.typography.labelSmall,
                        color = MariMutedLight
                    )
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MariTextLight
                    )
                }

                Text(
                    text = CurrencyFormatter.formatIdr(plan.priceIdr),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MariPrimaryBlue
                    )
                )
            }

            Text(
                text = if (plan.isYearly) "Penagihan tahunan (Hemat 17% dibanding bulanan)" else "Penagihan bulanan fleksibel, dapat dibatalkan kapan saja",
                style = MaterialTheme.typography.bodySmall,
                color = MariMutedLight
            )

            HorizontalDivider(color = MariBorderLight)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "Kendaraan armada tanpa batas & 100 scan AI/bln",
                    "Matriks Biaya Riil per Km (TCO) & Kalkulator Margin Driver",
                    "Sertifikat Digital Eco-Driver & Rekap Emisi CO₂ (PDF)",
                    "Ekspor Laporan PDF Formal Ber-Kop (Reimbursement & Pajak)",
                    "Pagu Anggaran BBM Pro & Radar Komparasi Harga SPBU"
                ).forEach { featureText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MariSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = featureText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MariTextLight
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kartu Pilihan Metode Pembayaran
 */
@Composable
private fun PaymentMethodCard(
    title: String,
    badgeText: String,
    badgeType: FuelChipType,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    FuelCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        cornerRadius = 14.dp,
        contentPadding = 16.dp,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MariPrimaryBlue else MariBorderLight
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MariPrimaryBlue,
                    unselectedColor = MariBorderLight
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MariPrimaryBlue.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MariPrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MariTextLight
                        )
                    }

                    FuelStatusChip(text = badgeText, type = badgeType)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MariMutedLight,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

/**
 * Kartu Rincian Biaya
 */
@Composable
private fun PricingBreakdownCard(priceIdr: Long) {
    FuelCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        contentPadding = 18.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "RINCIAN PEMBAYARAN",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MariMutedLight
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Harga Langganan", style = MaterialTheme.typography.bodyMedium, color = MariMutedLight)
                Text(text = CurrencyFormatter.formatIdr(priceIdr), style = MaterialTheme.typography.bodyMedium, color = MariTextLight)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Biaya Layanan & Pajak", style = MaterialTheme.typography.bodyMedium, color = MariMutedLight)
                Text(text = "Rp 0 (Gratis)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MariPrimaryBlue)
            }

            HorizontalDivider(color = MariBorderLight)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Pembayaran",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MariTextLight
                )
                Text(
                    text = CurrencyFormatter.formatIdr(priceIdr),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MariPrimaryBlue
                    )
                )
            }
        }
    }
}

/**
 * Catatan Kebijakan
 */
@Composable
private fun PolicyNoticeCard() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MariBorderLight.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MariPrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Status Pro aktif secara instan dan otomatis diperpanjang sesuai masa aktif yang terverifikasi aman di server.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MariMutedLight,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Floating / Sticky Bottom Bar Checkout
 */
@Composable
private fun CheckoutBottomBar(
    totalAmountIdr: Long,
    isLoading: Boolean,
    onCheckoutClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, MariBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Bayar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MariMutedLight
                )
                Text(
                    text = CurrencyFormatter.formatIdr(totalAmountIdr),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MariPrimaryBlue
                    )
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            FuelPrimaryButton(
                text = "Bayar Sekarang",
                onClick = onCheckoutClick,
                isLoading = isLoading,
                modifier = Modifier.widthIn(max = 200.dp)
            )
        }
    }
}
