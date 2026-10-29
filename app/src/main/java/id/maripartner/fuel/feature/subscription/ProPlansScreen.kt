package id.maripartner.fuel.feature.subscription

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.model.UserEntitlement

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
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Status Langganan & Kuota AI Pengguna
                item {
                    uiState.userEntitlement?.let { ent ->
                        UserStatusAndQuotaCard(entitlement = ent)
                    }
                }

                // 2. Tab Pemilih Periode Penagihan (Bulanan vs Tahunan)
                item {
                    BillingPeriodSelector(
                        selectedPeriod = uiState.selectedBillingPeriod,
                        onSelectPeriod = { viewModel.onEvent(SubscriptionUiEvent.SelectBillingPeriod(it)) }
                    )
                }

                // 3. Kartu Paket (Free vs Pro)
                val filteredPlans = uiState.plans.filter {
                    it.isFree || it.billingPeriod.equals(uiState.selectedBillingPeriod, ignoreCase = true)
                }

                items(filteredPlans.size) { index ->
                    val plan = filteredPlans[index]
                    val isCurrentPlan = if (plan.isFree) {
                        !(uiState.userEntitlement?.isPro ?: false)
                    } else {
                        (uiState.userEntitlement?.isPro ?: false) &&
                                uiState.userEntitlement?.planCode.equals(plan.code, ignoreCase = true)
                    }

                    PlanCard(
                        plan = plan,
                        isCurrentPlan = isCurrentPlan,
                        onSelect = { onNavigateToCheckout(plan.code) }
                    )
                }

                // 4. Catatan Kepatuhan Kebijakan
                item {
                    ComplianceNoticeCard()
                }
            }
        }

        // Dialog Informasi "Segera Hadir" (Zero-Mock Purchase)
        if (uiState.showComingSoonDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(SubscriptionUiEvent.DismissComingSoon) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = "Integrasi Pembayaran Segera Hadir",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Fitur checkout pembayaran resmi (QRISpy & Google Play Billing) sedang disiapkan untuk Tahap 7.\n\n" +
                                "Sesuai standar keamanan sistem, tidak ada simulasi transaksi palsu atau pemberian status Pro tanpa validasi server nyata.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(SubscriptionUiEvent.DismissComingSoon) }) {
                        Text("Mengerti")
                    }
                }
            )
        }
    }
}

@Composable
private fun UserStatusAndQuotaCard(entitlement: UserEntitlement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entitlement.isPro) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status Akun Anda",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = entitlement.planName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (entitlement.isPro) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (entitlement.isPro) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (entitlement.isPro) "PRO AKTIF" else "GRATIS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (entitlement.isPro) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (entitlement.expiresAt != null) {
                Text(
                    text = "Masa aktif hingga: ${DateUtils.formatDateTime(entitlement.expiresAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (!entitlement.isPro) {
                Text(
                    text = "Batas: Maksimal 2 kendaraan aktif & 5 scan AI per bulan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // AI Quota Progress
            val quota = entitlement.aiQuota
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sisa Kuota AI Cloud",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${quota.remaining} / ${quota.limit} scan",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (quota.isExhausted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { quota.usagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (quota.isExhausted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Text(
                    text = "OCR teks on-device di HP selalu gratis tanpa memotong kuota AI.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BillingPeriodSelector(
    selectedPeriod: String,
    onSelectPeriod: (String) -> Unit
) {
    TabRow(
        selectedTabIndex = if (selectedPeriod == "monthly") 0 else 1,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Tab(
            selected = selectedPeriod == "monthly",
            onClick = { onSelectPeriod("monthly") },
            text = { Text("Bulanan", fontWeight = FontWeight.SemiBold) }
        )
        Tab(
            selected = selectedPeriod == "yearly",
            onClick = { onSelectPeriod("yearly") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tahunan", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-17%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isCurrentPlan: Boolean,
    onSelect: () -> Unit
) {
    val isPro = plan.isPro
    val borderColor = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isPro) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = plan.description ?: if (plan.isFree) "Paket standar tanpa biaya" else "Paket performa tinggi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isPro) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rekomendasi",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Price Display
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (plan.priceIdr == 0L) "Gratis" else CurrencyFormatter.formatIdr(plan.priceIdr),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (plan.priceIdr > 0L) {
                    Text(
                        text = if (plan.isYearly) " / tahun" else " / bulan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }
            }

            // Features List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val vehicleText = if (plan.maxVehicles == null) "Kendaraan tanpa batas" else "Maksimal ${plan.maxVehicles} kendaraan aktif"
                val aiText = "${plan.aiScanQuota} scan struk AI per bulan"

                FeatureRow(text = vehicleText, isIncluded = true)
                FeatureRow(text = aiText, isIncluded = true)
                FeatureRow(text = "OCR teks on-device tanpa batas", isIncluded = true)
                FeatureRow(text = "Pencatatan & riwayat transaksi BBM", isIncluded = true)
                FeatureRow(text = "Ekspor data riwayat ke format CSV", isIncluded = true)
                FeatureRow(
                    text = "Analitik performa & grafik efisiensi BBM",
                    isIncluded = isPro
                )
                FeatureRow(
                    text = "Cloud AI Gemini Fallback berprioritas tinggi",
                    isIncluded = isPro
                )
            }

            // CTA Button
            if (isCurrentPlan) {
                FuelSecondaryButton(
                    text = "Paket Anda Saat Ini",
                    onClick = {},
                    enabled = false
                )
            } else if (isPro) {
                FuelPrimaryButton(
                    text = "Tingkatkan ke ${plan.name}",
                    onClick = onSelect
                )
            } else {
                FuelSecondaryButton(
                    text = "Pilih ${plan.name}",
                    onClick = onSelect,
                    enabled = false
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(text: String, isIncluded: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ComplianceNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Langganan digital Mari Partner Fuel mematuhi Google Play Payments Policy. Seluruh harga diatur secara otoritatif di server.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
