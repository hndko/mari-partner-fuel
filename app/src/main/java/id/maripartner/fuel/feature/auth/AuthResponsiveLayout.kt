package id.maripartner.fuel.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.config.AppConfig
import id.maripartner.fuel.core.designsystem.component.MariPartnerRibbonM
import id.maripartner.fuel.core.designsystem.component.PrivacyPolicyDialog
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariBgLight
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue

/**
 * Layout autentikasi modern mengadaptasi konsep UI GoPay:
 * Hero Gradient Header (Deep Navy & Electric Blue) dengan logo Ribbon M dan pill keamanan di atas,
 * serta Elevated White Rounded Sheet (28dp) berbayang halus yang membungkus formulir aksi.
 */
@Composable
fun AuthResponsiveLayout(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable () -> Unit
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

    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = snackbarHost,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MariBgLight)
                .verticalScroll(rememberScrollState())
                .padding(bottom = innerPadding.calculateBottomPadding() + 24.dp)
        ) {
            // 1. Hero Gradient Header (Status Bar, Back Button, Brand Ribbon M Logo, & Security Pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = gradientBrush)
                    .statusBarsPadding()
                    .padding(top = 10.dp, bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar: Tombol Kembali Glassmorphism & Pill Keamanan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (onNavigateBack != null) {
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
                                        onNavigateBack()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(38.dp))
                        }

                        // Pill Badge Keamanan (Klik untuk Buka Kebijakan Privasi)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                                .clickable { showPrivacyDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Keamanan & Privasi Terjamin ›",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.size(38.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Logo Ribbon M Container Elevated
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            MariPartnerRibbonM(size = 48.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Brand Name: "Mari Partner" + Badge "FUEL"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Mari ",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "Partner",
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.22f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "FUEL",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Your Digital Fuel Partner",
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // 2. Elevated White Rounded Sheet Container (Overlapping the gradient header)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-18).dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Judul & Subtitle Halaman
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp
                            ),
                            color = MariMutedLight,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Formulir Aksi Spesifik (Login / Register / Forgot Password)
                        content()

                        Spacer(modifier = Modifier.height(14.dp))

                        // Link Kebijakan Privasi
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Keamanan data dilindungi oleh ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MariMutedLight
                            )
                            Text(
                                text = "Kebijakan Privasi",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { showPrivacyDialog = true }
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Footer Versi Aplikasi
                        Text(
                            text = "Mari Partner Fuel · v${AppConfig.versionName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MariMutedLight.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // Modal Dialog Kebijakan Privasi
        PrivacyPolicyDialog(
            show = showPrivacyDialog,
            onDismiss = { showPrivacyDialog = false }
        )
    }
}
