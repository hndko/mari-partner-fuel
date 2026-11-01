package id.maripartner.fuel.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue

/**
 * Bilah Navigasi Bawah resmi Mari Partner Fuel.
 * Mengadopsi arsitektur 5 segmen simetris dengan Tombol Melayang Menonjol di tengah (Elevated Center Scan Button).
 */
@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Bilah Latar Belakang (Surface)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Beranda
                BottomNavItemCell(
                    title = "Beranda",
                    icon = Icons.Default.Dashboard,
                    selected = currentRoute == NavRoute.Dashboard.route,
                    onClick = { onNavigateToRoute(NavRoute.Dashboard.route) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: Riwayat
                BottomNavItemCell(
                    title = "Riwayat",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    selected = currentRoute == NavRoute.Transactions.route,
                    onClick = { onNavigateToRoute(NavRoute.Transactions.route) },
                    modifier = Modifier.weight(1f)
                )

                // Spacer Penyeimbang untuk Tombol Melayang di Tengah
                Spacer(modifier = Modifier.weight(1.15f))

                // Tab 4: Kendaraan
                BottomNavItemCell(
                    title = "Kendaraan",
                    icon = Icons.Default.DirectionsCar,
                    selected = currentRoute == NavRoute.Vehicles.route,
                    onClick = { onNavigateToRoute(NavRoute.Vehicles.route) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 5: Profil
                BottomNavItemCell(
                    title = "Profil",
                    icon = Icons.Default.Person,
                    selected = currentRoute == NavRoute.Profile.route,
                    onClick = { onNavigateToRoute(NavRoute.Profile.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. Tombol Melayang Tengah (Elevated Center Floating Action Button)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(elevation = 10.dp, shape = CircleShape, spotColor = MariPrimaryBlue)
                    .border(
                        width = 3.5.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(MariPrimaryBlue, MariAccentCyan)
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) {
                        onNavigateToRoute(NavRoute.ReceiptScan.route)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = "Scan Struk SPBU",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "SCAN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MariPrimaryBlue
            )
        }
    }
}

@Composable
private fun BottomNavItemCell(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) {
        MariPrimaryBlue
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp)
            ) {
                if (!selected) onClick()
            }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selected) MariPrimaryBlue.copy(alpha = 0.12f) else Color.Transparent
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = contentColor
        )
    }
}
