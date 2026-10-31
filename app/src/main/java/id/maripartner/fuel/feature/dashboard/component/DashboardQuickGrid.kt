package id.maripartner.fuel.feature.dashboard.component

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.designsystem.theme.MariDanger

/**
 * Hub Menu Pintasan 8-Ikon (Grid 2 Baris x 4 Kolom) mengadaptasi tata letak ikonik GoPay.
 * Menghadirkan akses langsung ke seluruh fitur operasional Mari Partner Fuel.
 */
@Composable
fun DashboardQuickGrid(
    pendingRemindersCount: Int,
    onNavigateToReceiptScan: () -> Unit,
    onNavigateToTransactionForm: () -> Unit,
    onNavigateToTripCalculator: () -> Unit,
    onOpenBudgetDialog: () -> Unit,
    onOpenRadarDialog: () -> Unit,
    onCheckNearbySpbu: () -> Unit,
    onFilterService: () -> Unit,
    onNavigateToNightHud: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Baris 1 (4 Ikon)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickGridItem(
                title = "Scan Struk",
                icon = Icons.Default.DocumentScanner,
                bgColor = Color(0xFFE0F7FA),
                iconColor = Color(0xFF00838F),
                onClick = onNavigateToReceiptScan,
                modifier = Modifier.weight(1f)
            )

            QuickGridItem(
                title = "Catat Manual",
                icon = Icons.Default.Add,
                bgColor = Color(0xFFE3F2FD),
                iconColor = Color(0xFF1565C0),
                onClick = onNavigateToTransactionForm,
                modifier = Modifier.weight(1f)
            )

            QuickGridItem(
                title = "Kalkulator Trip",
                icon = Icons.Default.Route,
                bgColor = Color(0xFFFFF3E0),
                iconColor = Color(0xFFE65100),
                onClick = onNavigateToTripCalculator,
                modifier = Modifier.weight(1f)
            )

            QuickGridItem(
                title = "Pagu BBM",
                icon = Icons.Default.TrackChanges,
                bgColor = Color(0xFFE8F5E9),
                iconColor = Color(0xFF2E7D32),
                onClick = onOpenBudgetDialog,
                modifier = Modifier.weight(1f)
            )
        }

        // Baris 2 (4 Ikon)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickGridItem(
                title = "Radar Harga",
                icon = Icons.Default.LocalGasStation,
                bgColor = Color(0xFFF3E5F5),
                iconColor = Color(0xFF6A1B9A),
                onClick = onOpenRadarDialog,
                modifier = Modifier.weight(1f)
            )

            QuickGridItem(
                title = "SPBU Dekat",
                icon = Icons.Default.NearMe,
                bgColor = Color(0xFFFFF8E1),
                iconColor = Color(0xFFF57F17),
                onClick = onCheckNearbySpbu,
                modifier = Modifier.weight(1f)
            )

            QuickGridItem(
                title = "Servis & Oli",
                icon = Icons.Default.Build,
                bgColor = Color(0xFFFFEBEE),
                iconColor = Color(0xFFC62828),
                badgeCount = pendingRemindersCount,
                onClick = onFilterService,
                modifier = Modifier.weight(1f)
            )

            QuickGridItem(
                title = "Night HUD",
                icon = Icons.Default.Nightlight,
                bgColor = Color(0xFFEDE7F6),
                iconColor = Color(0xFF4527A0),
                onClick = onNavigateToNightHud,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickGridItem(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 28.dp)
            ) {
                onClick()
            }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = MariDanger,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
