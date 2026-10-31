package id.maripartner.fuel.feature.subscription

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariAiTag
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import id.maripartner.fuel.core.designsystem.theme.MariTextLight
import id.maripartner.fuel.core.designsystem.theme.MariWarning
import id.maripartner.fuel.core.designsystem.theme.MariWarningBg
import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.model.UserEntitlement

/**
 * Layar Paket & Langganan — Sesuai Panduan Desain Resmi Mari Partner Fuel
 */
@Composable
fun ProPlansScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: (planCode: String) -> Unit = {},
    viewModel: SubscriptionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Paket & Langganan",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.userEntitlement == null) {
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
                        text = "Memuat data paket & kuota…",
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
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header: Eyebrow PILIH PENGALAMANMU & Judul
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                    ) {
                        Text(
                            text = "PILIH PENGALAMANMU",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MariAccentCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Upgrade cara kamu mengelola BBM",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MariBrandNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mulai gratis, tingkatkan kapan pun saat membutuhkan lebih banyak insight.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MariMutedLight
                        )
                    }
                }

                // 2. Kartu Status Akun & Sisa Kuota AI
                item {
                    uiState.userEntitlement?.let { ent ->
                        Box(modifier = Modifier.widthIn(max = 760.dp)) {
                            UserStatusAndQuotaCard(entitlement = ent)
                        }
                    }
                }

                // 3. Tab Pemilih Periode Penagihan (Bulanan vs Tahunan)
                item {
                    Box(modifier = Modifier.widthIn(max = 760.dp)) {
                        BillingPeriodSelector(
                            selectedPeriod = uiState.selectedBillingPeriod,
                            onSelectPeriod = { viewModel.onEvent(SubscriptionUiEvent.SelectBillingPeriod(it)) }
                        )
                    }
                }

                // 4. Daftar Kartu Paket (Free vs Pro)
                val filteredPlans = uiState.plans.filter {
                    it.isFree || it.billingPeriod.equals(uiState.selectedBillingPeriod, ignoreCase = true)
                }

                val isUserPro = uiState.userEntitlement?.isPro == true
                val currentPlanCode = uiState.userEntitlement?.planCode.orEmpty()

                items(filteredPlans.size) { index ->
                    val plan = filteredPlans[index]
                    val isCurrentPlan = if (plan.isFree) {
                        !isUserPro
                    } else {
                        isUserPro && currentPlanCode.equals(plan.code, ignoreCase = true)
                    }

                    Box(modifier = Modifier.widthIn(max = 760.dp)) {
                        PlanCard(
                            plan = plan,
                            isCurrentPlan = isCurrentPlan,
                            isUserPro = isUserPro,
                            currentPlanCode = currentPlanCode,
                            onSelect = { onNavigateToCheckout(plan.code) },
                            onDowngrade = { viewModel.onEvent(SubscriptionUiEvent.RequestDowngrade(plan)) },
                            onRenew = { viewModel.onEvent(SubscriptionUiEvent.RequestRenewal(plan)) },
                            onSwitchCycle = { viewModel.onEvent(SubscriptionUiEvent.RequestSwitchCycle(plan)) }
                        )
                    }
                }

                // 5. Catatan Kepatuhan Kebijakan Server & Google Play
                item {
                    Box(modifier = Modifier.widthIn(max = 760.dp)) {
                        ComplianceNoticeCard()
                    }
                }
            }
        }

        // Dialog Konfirmasi Downgrade ke Free (Graceful Expiry & No Immediate Cut)
        if (uiState.showDowngradeDialog) {
            val entitlement = uiState.userEntitlement
            val planName = entitlement?.planName ?: "Mari Pro"
            val expiry = entitlement?.expiresAt
            val isPermanent = entitlement?.isPro == true && expiry == null
            val expiresText = when {
                isPermanent -> "Permanen (Seumur Hidup)"
                expiry != null -> DateUtils.formatFullDate(expiry)
                else -> "akhir periode"
            }
            FuelSweetAlertDialog(
                show = true,
                type = SweetAlertType.WARNING,
                title = "Aturan Downgrade ke Free",
                message = "Anda saat ini memiliki langganan $planName aktif hingga $expiresText.\n\n" +
                    "Jaminan Aturan Sistem:\n" +
                    "• Hak Pro Anda TETAP AKTIF sepenuhnya hingga $expiresText tanpa diputus seketika.\n" +
                    "• Setelah tanggal tersebut, akun bertransisi ke Paket Free (5 scan AI/bln & maks 2 kendaraan aktif).\n" +
                    "• Seluruh data riwayat transaksi BBM Anda dijamin 100% aman dan tidak akan dihapus.\n" +
                    "• Kendaraan ke-3 dan seterusnya akan diarsipkan secara aman tanpa kehilangan data.\n\n" +
                    "Apakah Anda yakin ingin memproses downgrade?",
                confirmText = "Ya, Downgrade",
                dismissText = "Batal (Tetap Pro)",
                onConfirm = { viewModel.onEvent(SubscriptionUiEvent.ConfirmDowngrade) },
                onDismiss = { viewModel.onEvent(SubscriptionUiEvent.DismissDowngradeDialog) }
            )
        }

        // Dialog Konfirmasi Perpanjangan Paket (Renewal Kumulatif)
        if (uiState.showRenewalDialog) {
            val target = uiState.targetPlan
            val entitlement = uiState.userEntitlement
            val expiry = entitlement?.expiresAt
            val isPermanent = entitlement?.isPro == true && expiry == null
            val expiresText = when {
                isPermanent -> "Permanen (Seumur Hidup)"
                expiry != null -> DateUtils.formatFullDate(expiry)
                else -> "tanggal jatuh tempo"
            }
            FuelSweetAlertDialog(
                show = true,
                type = SweetAlertType.INFO,
                title = "Perpanjang ${target?.name ?: "Paket Pro"}",
                message = "Masa aktif Anda saat ini berlaku hingga $expiresText.\n\n" +
                    "Sesuai aturan sistem, perpanjangan paket akan menambahkan masa aktif secara kumulatif dari tanggal tersebut. Sisa hari aktif Anda yang sedang berjalan tidak akan hilang.\n\n" +
                    "Lanjutkan ke formulir pembayaran?",
                confirmText = "Lanjut Pembayaran",
                dismissText = "Batal",
                onConfirm = {
                    val code = uiState.targetPlan?.code ?: ""
                    viewModel.onEvent(SubscriptionUiEvent.DismissRenewalDialog)
                    onNavigateToCheckout(code)
                },
                onDismiss = { viewModel.onEvent(SubscriptionUiEvent.DismissRenewalDialog) }
            )
        }

        // Dialog Konfirmasi Peralihan Siklus Penagihan
        if (uiState.showSwitchCycleDialog) {
            val target = uiState.targetPlan
            val isUpgradeToYearly = target?.isYearly == true
            FuelSweetAlertDialog(
                show = true,
                type = SweetAlertType.INFO,
                title = if (isUpgradeToYearly) "Upgrade ke Paket Tahunan" else "Beralih ke Paket Bulanan",
                message = if (isUpgradeToYearly) {
                    "Anda akan beralih ke ${target?.name} (Hemat 17%).\n\nDurasi 1 tahun penuh akan ditambahkan ke sisa masa aktif Pro Anda saat ini.\n\nLanjutkan ke proses pembayaran?"
                } else {
                    "Anda akan beralih ke ${target?.name}.\n\nSiklus bulanan baru akan berlaku setelah masa aktif tahunan Anda yang sedang berjalan berakhir secara alami.\n\nLanjutkan ke proses pembayaran?"
                },
                confirmText = "Lanjut Pembayaran",
                dismissText = "Batal",
                onConfirm = {
                    val code = uiState.targetPlan?.code ?: ""
                    viewModel.onEvent(SubscriptionUiEvent.DismissSwitchCycleDialog)
                    onNavigateToCheckout(code)
                },
                onDismiss = { viewModel.onEvent(SubscriptionUiEvent.DismissSwitchCycleDialog) }
            )
        }

        // Alert Feedback Sukses Tindakan
        uiState.actionSuccessMessage?.let { msg ->
            FuelSweetAlertDialog(
                show = true,
                type = SweetAlertType.SUCCESS,
                title = "Permintaan Berhasil",
                message = msg,
                confirmText = "Mengerti",
                onConfirm = { viewModel.onEvent(SubscriptionUiEvent.DismissSuccessMessage) },
                onDismiss = { viewModel.onEvent(SubscriptionUiEvent.DismissSuccessMessage) }
            )
        }

        // Modal Segera Hadir via FuelSweetAlertDialog
        if (uiState.showComingSoonDialog) {
            FuelSweetAlertDialog(
                show = true,
                type = SweetAlertType.INFO,
                title = "Integrasi Pembayaran Segera Hadir",
                message = "Fitur checkout pembayaran resmi (QRISpy & Google Play Billing) sedang disiapkan.\n\nSesuai standar keamanan sistem, tidak ada simulasi transaksi palsu atau pemberian status Pro tanpa validasi server nyata.",
                confirmText = "Mengerti",
                onConfirm = { viewModel.onEvent(SubscriptionUiEvent.DismissComingSoon) },
                onDismiss = { viewModel.onEvent(SubscriptionUiEvent.DismissComingSoon) }
            )
        }
    }
}

/**
 * Kartu Status Akun & Kuota AI Pengguna
 */
@Composable
private fun UserStatusAndQuotaCard(entitlement: UserEntitlement) {
    FuelCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = 20.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STATUS AKUN ANDA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MariMutedLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = entitlement.planName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MariTextLight
                    )
                }

                if (entitlement.isPro) {
                    val daysLeft = entitlement.effectiveDaysUntilExpiry ?: Int.MAX_VALUE
                    if (entitlement.isExpiringSoon || daysLeft <= 7) {
                        FuelStatusChip(
                            text = if (daysLeft != Int.MAX_VALUE) "HABIS $daysLeft HARI LAGI" else "SEGERA BERAKHIR",
                            type = FuelChipType.Warning
                        )
                    } else {
                        FuelStatusChip(text = "PRO AKTIF", type = FuelChipType.Success)
                    }
                } else {
                    FuelStatusChip(text = "GRATIS", type = FuelChipType.Neutral)
                }
            }

            if (entitlement.expiresAt != null) {
                val daysLeft = entitlement.effectiveDaysUntilExpiry ?: Int.MAX_VALUE
                val isExpiring = entitlement.isPro && (entitlement.isExpiringSoon || daysLeft <= 7)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isExpiring) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = MariWarning,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Masa aktif hingga: ${DateUtils.formatFullDate(entitlement.expiresAt)}" +
                            if (isExpiring) " (Tersisa $daysLeft hari)" else "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isExpiring) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isExpiring) MariWarning else MariMutedLight
                    )
                }
            } else if (entitlement.isPro) {
                Text(
                    text = "Masa aktif: Permanen (Seumur Hidup)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF16A34A)
                )
            } else {
                Text(
                    text = "Batas Free: Maksimal 2 kendaraan aktif & 5 scan AI per bulan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MariMutedLight
                )
            }

            HorizontalDivider(color = MariBorderLight)

            // AI Quota Progress
            val quota = entitlement.aiQuota
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MariAiTag,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sisa Kuota AI Cloud",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MariTextLight
                        )
                    }

                    Text(
                        text = "${quota.remaining} / ${quota.limit} scan",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (quota.isExhausted) MariDanger else MariPrimaryBlue
                    )
                }

                LinearProgressIndicator(
                    progress = { quota.usagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (quota.isExhausted) MariDanger else MariPrimaryBlue,
                    trackColor = MariBorderLight
                )

                Text(
                    text = "OCR teks on-device di HP selalu gratis tanpa memotong kuota AI.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MariMutedLight
                )
            }
        }
    }
}

/**
 * Pemilih Periode Penagihan (Bulanan vs Tahunan)
 */
@Composable
private fun BillingPeriodSelector(
    selectedPeriod: String,
    onSelectPeriod: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MariBorderLight.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MariBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Bulanan
            val isMonthly = selectedPeriod == "monthly"
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isMonthly) MariPrimaryBlue else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectPeriod("monthly") }
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bulanan",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isMonthly) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isMonthly) Color.White else MariTextLight
                    )
                }
            }

            // Tahunan
            val isYearly = selectedPeriod == "yearly"
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isYearly) MariPrimaryBlue else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectPeriod("yearly") }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Tahunan",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isYearly) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isYearly) Color.White else MariTextLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isYearly) Color.White.copy(alpha = 0.25f) else MariSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Hemat 17%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isYearly) Color.White else MariSuccess
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kartu Penawaran Paket (Free vs Pro)
 */
@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isCurrentPlan: Boolean,
    isUserPro: Boolean,
    currentPlanCode: String,
    onSelect: () -> Unit,
    onDowngrade: () -> Unit,
    onRenew: () -> Unit,
    onSwitchCycle: () -> Unit
) {
    val isPro = plan.isPro

    FuelCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        contentPadding = 22.dp,
        border = BorderStroke(
            width = if (isPro) 2.dp else 1.dp,
            color = if (isPro) MariPrimaryBlue else MariBorderLight
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = if (isPro) "UNTUK KEBUTUHAN LEBIH" else "MULAI DI SINI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isPro) MariPrimaryBlue else MariMutedLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MariTextLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plan.description ?: if (plan.isFree) "Untuk pencatatan kebutuhan dasar." else "Insight lebih dalam dan fitur pintar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MariMutedLight
                    )
                }

                if (isCurrentPlan && isUserPro) {
                    FuelStatusChip(text = "PAKET ANDA SAAT INI", type = FuelChipType.Success)
                } else if (isPro) {
                    FuelStatusChip(text = "PALING FLEKSIBEL", type = FuelChipType.AiTag)
                }
            }

            // Display Harga
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (plan.priceIdr == 0L) "Rp0" else CurrencyFormatter.formatIdr(plan.priceIdr),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = if (isPro) MariPrimaryBlue else MariTextLight
                )
                Text(
                    text = if (plan.priceIdr == 0L) " / selamanya" else if (plan.isYearly) " / tahun" else " / bulan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MariMutedLight,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            // CTA Button berdasarkan status kepemilikan dan aturan siklus
            if (isCurrentPlan) {
                if (isUserPro) {
                    FuelPrimaryButton(
                        text = "Perpanjang Masa Aktif",
                        onClick = onRenew,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    FuelSecondaryButton(
                        text = "Paket Anda Saat Ini",
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (plan.isFree && isUserPro) {
                FuelSecondaryButton(
                    text = "Downgrade ke Paket Free",
                    onClick = onDowngrade,
                    enabled = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (isUserPro && !plan.isFree) {
                if (plan.isYearly) {
                    FuelPrimaryButton(
                        text = "Upgrade ke Tahunan (Hemat 17%)",
                        onClick = onSwitchCycle,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    FuelSecondaryButton(
                        text = "Beralih ke Bulanan",
                        onClick = onSwitchCycle,
                        enabled = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (isPro) {
                FuelPrimaryButton(
                    text = "Pilih Paket Pro",
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                FuelSecondaryButton(
                    text = "Paket Dasar",
                    onClick = onSelect,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(color = MariBorderLight)

            // Features Checklist
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val vehicleText = if (plan.maxVehicles == null) "Kendaraan tanpa batas" else "Maksimal ${plan.maxVehicles} kendaraan aktif"
                val aiText = "${plan.aiScanQuota} scan struk AI per bulan"

                PlanFeatureItem(text = "Catat transaksi BBM", isIncluded = true)
                PlanFeatureItem(text = vehicleText, isIncluded = true)
                PlanFeatureItem(text = "Riwayat transaksi & filter", isIncluded = true)
                PlanFeatureItem(text = "OCR teks on-device lokal tanpa batas", isIncluded = true)
                PlanFeatureItem(text = aiText, isIncluded = isPro)
                PlanFeatureItem(text = "Analitik performa & grafik efisiensi BBM", isIncluded = isPro)
                PlanFeatureItem(text = "Cloud AI Gemini Fallback prioritas tinggi", isIncluded = isPro)
                PlanFeatureItem(text = "Ekspor data riwayat ke format CSV", isIncluded = isPro)
                PlanFeatureItem(text = "Prioritas dukungan teknis", isIncluded = isPro)
            }
        }
    }
}

/**
 * Baris Item Fitur Paket
 */
@Composable
private fun PlanFeatureItem(text: String, isIncluded: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (isIncluded) MariPrimaryBlue.copy(alpha = 0.12f) else MariBorderLight.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isIncluded) MariPrimaryBlue else MariMutedLight.copy(alpha = 0.5f),
                modifier = Modifier.size(13.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isIncluded) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (isIncluded) MariTextLight else MariMutedLight.copy(alpha = 0.6f)
        )
    }
}

/**
 * Catatan Kepatuhan Kebijakan
 */
@Composable
private fun ComplianceNoticeCard() {
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
                tint = MariMutedLight,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Harga dan benefit di atas merupakan konfigurasi resmi server. Transaksi aman, terverifikasi otomatis, dan dapat dibatalkan kapan saja.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MariMutedLight,
                lineHeight = 16.sp
            )
        }
    }
}
