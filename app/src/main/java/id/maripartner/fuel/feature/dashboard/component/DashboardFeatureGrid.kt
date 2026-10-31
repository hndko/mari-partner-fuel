package id.maripartner.fuel.feature.dashboard.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import id.maripartner.fuel.core.designsystem.theme.MariWarning

/**
 * Grid Kartu Wawasan & Optimasi Armada (2 Kolom) mengadaptasi section 'Untuk usaha Anda' pada GoPay.
 */
@Composable
fun DashboardFeatureGrid(
    onNavigateToSpbuBenchmark: () -> Unit,
    onNavigateToFuelShockSimulator: () -> Unit,
    onNavigateToEcoDriverCertificate: () -> Unit,
    onNavigateToAnomalyDetection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Section Title
        Text(
            text = "Wawasan Cerdas & Optimasi Armada",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Baris 1: Indeks SPBU Terbaik & Simulator Kenaikan BBM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCardItem(
                title = "Indeks SPBU",
                tag = "HEMAT",
                description = "Komparasi efisiensi km/L per SPBU & brand",
                icon = Icons.Default.LocalGasStation,
                iconBg = Color(0xFFE8F5E9),
                iconColor = MariSuccess,
                onClick = onNavigateToSpbuBenchmark,
                modifier = Modifier.weight(1f)
            )

            FeatureCardItem(
                title = "Simulator BBM",
                tag = "SIMULASI",
                description = "Uji dampak lonjakan tarif kas armada",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconBg = Color(0xFFFEE2E2),
                iconColor = MariDanger,
                onClick = onNavigateToFuelShockSimulator,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Baris 2: Sertifikat Eco-Driver & Deteksi Anomali BBM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCardItem(
                title = "Sertifikat Eco",
                tag = "PRO",
                description = "Skor efisiensi & reduksi jejak karbon CO₂",
                icon = Icons.Default.Star,
                iconBg = Color(0xFFFEF3C7),
                iconColor = MariWarning,
                onClick = onNavigateToEcoDriverCertificate,
                modifier = Modifier.weight(1f)
            )

            FeatureCardItem(
                title = "Anomali BBM",
                tag = "MONITOR",
                description = "Deteksi dini boros & indikasi kebocoran",
                icon = Icons.Default.WarningAmber,
                iconBg = Color(0xFFE0F2FE),
                iconColor = MariAccentCyan,
                onClick = onNavigateToAnomalyDetection,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FeatureCardItem(
    title: String,
    tag: String,
    description: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MariBorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Kartu: Ikon & Tag/Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconBg.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag,
                        color = iconColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Judul & Deskripsi
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                ),
                color = MariMutedLight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chevron Arrow Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Buka Fitur",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MariPrimaryBlue
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MariPrimaryBlue,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
