package id.maripartner.fuel.feature.auth

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.component.MariPartnerBrandHeader
import id.maripartner.fuel.core.designsystem.component.MariPartnerRibbonM
import id.maripartner.fuel.core.designsystem.theme.MariCobaltBlue
import id.maripartner.fuel.core.designsystem.theme.MariCyanBlue
import id.maripartner.fuel.core.designsystem.theme.MariElectricBlue

/**
 * Layout autentikasi adaptif kelas dunia:
 * - Pada layar lebar / tablet landscape (Two-Pane Split): Membagi layar menjadi Hero Branding di kiri dan formulir di kanan.
 * - Pada ponsel / tablet portrait (Single-Pane Full Screen): Memanfaatkan layar penuh secara natural tanpa card melayang sempit.
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
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    // Kondisi breakpoint: aktifkan Two-Pane jika lebar >= 640dp dan tinggi memadai (>= 460dp)
    val isTwoPane = screenWidthDp >= 640 && screenHeightDp >= 460

    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            if (!isTwoPane && onNavigateBack != null) {
                FuelTopBar(
                    title = title,
                    onNavigateBack = onNavigateBack
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (isTwoPane) {
            // === TAMPILAN LAYAR LEBAR / TABLET (TWO-PANE SPLIT) ===
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Panel Kiri: Hero & Identitas Brand Mari Partner
                AuthHeroPane(
                    modifier = Modifier
                        .weight(0.44f)
                        .fillMaxHeight()
                )

                // Panel Kanan: Area Formulir Autentikasi
                Box(
                    modifier = Modifier
                        .weight(0.56f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // Tombol kembali jika ada alur navigasi balik (misal Register / Forgot Password)
                        if (onNavigateBack != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Kembali",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Text(
                                    text = "Kembali ke Masuk",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Area Formulir yang dapat di-scroll saat keyboard aktif
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 500.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 36.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                content()
                            }
                        }
                    }
                }
            }
        } else {
            // === TAMPILAN PONSEL / TABLET PORTRAIT (FULL SCREEN SINGLE-PANE) ===
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF1F6FF),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header Brand Mari Partner
                    MariPartnerBrandHeader(
                        title = title,
                        subtitle = subtitle
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Formulir Memanfaatkan Lebar Layar Penuh Secara Alami
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 480.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * Panel Hero di sisi kiri layar tablet untuk memperkuat citra dan nilai produk Mari Partner Fuel.
 */
@Composable
private fun AuthHeroPane(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MariCobaltBlue,
                        MariElectricBlue,
                        Color(0xFF071A3B)
                    )
                )
            )
            .padding(horizontal = 36.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header Logo Brand Mari Partner
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            MariPartnerRibbonM(size = 42.dp)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mari Partner",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MariCyanBlue.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "FUEL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MariCyanBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Your Digital Fuel Partner",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Tagline Utama
                Text(
                    text = "Kelola Biaya Bahan Bakar Lebih Pintar & Terukur",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                        fontSize = 24.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Solusi pencatatan digital otomatis berbasis OCR lokal dan AI cloud untuk mengoptimalkan efisiensi kendaraan Anda.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp
                    ),
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Fitur-Fitur Utama Mari Partner Fuel
                HeroFeatureRow(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "Scan Struk Otomatis",
                    desc = "Ekstraksi SPBU, volume liter & total rupiah instan"
                )
                Spacer(modifier = Modifier.height(16.dp))
                HeroFeatureRow(
                    icon = Icons.Default.Insights,
                    title = "Analitik & Efisiensi",
                    desc = "Pantau km/liter dan rata-rata pengeluaran BBM"
                )
                Spacer(modifier = Modifier.height(16.dp))
                HeroFeatureRow(
                    icon = Icons.Default.DirectionsCar,
                    title = "Multi-Kendaraan",
                    desc = "Kelola motor & mobil operasional dalam 1 akun"
                )
            }

            // Footer Keamanan & Kepercayaan
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MariCyanBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aman & Terintegrasi Cloud Database PostgreSQL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun HeroFeatureRow(
    icon: ImageVector,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MariCyanBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp
                ),
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
