package id.maripartner.fuel.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import id.maripartner.fuel.core.common.config.AppConfig
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelDangerButton
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.component.FuelTextField
import id.maripartner.fuel.core.designsystem.component.FuelTextLink
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.component.PrivacyPolicyDialog
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToProPlans: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSaasAdmin: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileUiEvent.LoadProfile)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(ProfileUiEvent.DismissMessage)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(ProfileUiEvent.DismissMessage)
        }
    }

    LaunchedEffect(uiState.isAccountDeleted) {
        if (uiState.isAccountDeleted) {
            onLogout()
        }
    }

    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                MariBrandNavy,
                MariPrimaryBlue.copy(alpha = 0.95f),
                MariPrimaryBlue
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val screenHeight = maxHeight
            val density = LocalDensity.current
            var headerHeightDp by remember { mutableStateOf(280.dp) }

            // Latar gradien atas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(brush = gradientBrush)
            )

            val remainingCardMinHeight = (screenHeight - headerHeightDp + 36.dp).coerceAtLeast(320.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Hero Gradient Header (Status Bar, Top Bar, Identitas Pengguna & Pro Status)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            val measuredDp = with(density) { size.height.toDp() }
                            if (measuredDp > 0.dp) {
                                headerHeightDp = measuredDp
                            }
                        }
                        .background(brush = gradientBrush)
                        .statusBarsPadding()
                        .padding(top = 10.dp, bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 760.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Bar: Judul & Status Versi
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Profil Akun",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 22.sp
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "v${AppConfig.versionName} Resmi",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Avatar Lingkaran Elegan
                        val initialLetter = uiState.displayName.take(1).uppercase().ifBlank { "P" }
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 8.dp,
                            border = BorderStroke(3.dp, Color.White.copy(alpha = 0.85f)),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                                        )
                                    )
                            ) {
                                Text(
                                    text = initialLetter,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 34.sp
                                    ),
                                    color = MariPrimaryBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Nama Pengguna & Email
                        Text(
                            text = uiState.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.email.ifBlank { "Akun Pengguna" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Chip Langganan
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.isPro) {
                                val proLabel = if (uiState.planName.isNotBlank() && !uiState.planName.contains("Free", ignoreCase = true)) {
                                    "${uiState.planName} Aktif"
                                } else {
                                    "Paket Pro Aktif"
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFEAB308).copy(alpha = 0.25f))
                                        .border(1.dp, Color(0xFFFDE047).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Diamond,
                                        contentDescription = null,
                                        tint = Color(0xFFFEF08A),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = proLabel,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.18f))
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Paket Free",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tombol Ubah / Edit Profil Cepat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier.clickable(onClick = onNavigateToEditProfile)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MariPrimaryBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Ubah Profil Akun",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MariPrimaryBlue
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Elevated White Rounded Sheet Container (Pengaturan & Preferensi)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-18).dp)
                        .defaultMinSize(minHeight = remainingCardMinHeight),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = remainingCardMinHeight),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(
                                    top = 22.dp,
                                    bottom = innerPadding.calculateBottomPadding() + 24.dp
                                ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                // 1b. Kartu Eksklusif Pemilik SaaS (Hanya Tampil untuk Owner / Admin)
                if (uiState.isOwnerOrAdmin) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F172A) // Dark Slate 900
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEAB308).copy(alpha = 0.2f))
                                ) {
                                    Text(text = "👑", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Panel Pemilik SaaS",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFEAB308)
                                        ) {
                                            Text(
                                                text = "VIP OWNER",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 9.sp,
                                                    color = Color.Black
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Pantau pendaftaran, omset langganan & kelola Pro",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8)
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onNavigateToSaasAdmin,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buka Panel Pemilik SaaS",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // 2. Kartu Penautan Akun Google (Google Account Linking)
                FuelCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = MariPrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Akun Google",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (uiState.isGoogleLinked) {
                                    "Terhubung — Anda dapat masuk 1-klik via Google"
                                } else {
                                    "Belum ditautkan ke akun Google"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (uiState.isGoogleLinked) {
                            FuelStatusChip(
                                text = "Terhubung",
                                type = FuelChipType.Success,
                                icon = Icons.Default.CheckCircle
                            )
                        } else {
                            FuelPrimaryButton(
                                text = "Tautkan",
                                onClick = {
                                    if (!uiState.isGoogleSignInConfigured) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Google OAuth Web Client ID belum dikonfigurasi.")
                                        }
                                        return@FuelPrimaryButton
                                    }

                                    coroutineScope.launch {
                                        try {
                                            val credentialManager = CredentialManager.create(context)
                                            val googleIdOption = GetGoogleIdOption.Builder()
                                                .setFilterByAuthorizedAccounts(false)
                                                .setServerClientId(AppConfig.googleWebClientId)
                                                .setAutoSelectEnabled(false)
                                                .build()

                                            val request = GetCredentialRequest.Builder()
                                                .addCredentialOption(googleIdOption)
                                                .build()

                                            val result = credentialManager.getCredential(
                                                request = request,
                                                context = context
                                            )

                                            val credential = result.credential
                                            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                                viewModel.onEvent(ProfileUiEvent.LinkGoogle(googleIdToken))
                                            } else {
                                                snackbarHostState.showSnackbar("Kredensial Google tidak dikenali.")
                                            }
                                        } catch (e: GetCredentialException) {
                                            val msg = e.localizedMessage ?: "Dibatalkan"
                                            if (!msg.contains("cancel", ignoreCase = true)) {
                                                snackbarHostState.showSnackbar("Batal menautkan: $msg")
                                            }
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Kendala: ${e.localizedMessage}")
                                        }
                                    }
                                },
                                isLoading = uiState.isUpdating,
                                modifier = Modifier.widthIn(max = 120.dp)
                            )
                        }
                    }
                }

                // 3. Status Paket Langganan
                FuelCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = if (uiState.isPro) MariSuccess else MariPrimaryBlue,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.isPro) "Paket Berlangganan: ${uiState.planName}" else "Kelola BBM Lebih Maksimal",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val subSubtitle = if (uiState.isPro) {
                                uiState.planExpiresAt?.let {
                                    if (it.startsWith("Permanen")) "Masa aktif: $it" else "Masa aktif berlaku sampai: $it"
                                } ?: "Status Pro aktif dengan armada dan analitik tak terbatas."
                            } else {
                                "Tingkatkan ke Pro untuk kuota scan AI hingga 100x/bulan dan multi-kendaraan tanpa batas."
                            }
                            Text(
                                text = subSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    FuelPrimaryButton(
                        text = if (uiState.isPro) "Kelola Paket & Langganan" else "Lihat Pilihan Paket Pro",
                        onClick = onNavigateToProPlans
                    )
                }

                // 4. Panel Preferensi & Pengaturan Aplikasi
                FuelCard {
                    Text(
                        text = "Preferensi & Sistem",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row: Notifikasi Pengingat Servis
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Notifikasi Pengingat",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Ingatkan jadwal ganti oli & servis berkala",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isReminderNotificationEnabled,
                            onCheckedChange = { viewModel.onEvent(ProfileUiEvent.ToggleReminderNotification(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MariPrimaryBlue
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Row: Kunci Keamanan Biometrik Aplikasi (Fingerprint / Face Unlock)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = if (uiState.isBiometricEnabled) MariPrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Kunci Aplikasi Biometrik",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Gunakan Sidik Jari atau Wajah untuk membuka aplikasi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isBiometricEnabled,
                                onCheckedChange = { viewModel.onEvent(ProfileUiEvent.ToggleBiometricLock(it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MariPrimaryBlue
                                )
                            )
                        }

                        if (uiState.isBiometricEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Kunci Otomatis Setelah:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(0 to "Langsung", 1 to "1 Menit", 5 to "5 Menit").forEach { (minutes, label) ->
                                    val isSelected = uiState.lockTimeoutMinutes == minutes
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onEvent(ProfileUiEvent.SetLockTimeout(minutes)) },
                                        label = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MariPrimaryBlue.copy(alpha = 0.15f),
                                            selectedLabelColor = MariPrimaryBlue
                                        )
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Row: Detektor Cerdas SPBU Terdekat (Geofence Reminder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Deteksi Kehadiran SPBU",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Ingatkan otomatis catat BBM saat berada di area SPBU (~250m)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isSpbuReminderEnabled,
                            onCheckedChange = { viewModel.onEvent(ProfileUiEvent.ToggleSpbuReminder(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MariPrimaryBlue
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Row: Keamanan Data & Privasi (Klik untuk Buka Dialog Kebijakan Privasi)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.onEvent(ProfileUiEvent.SetPrivacyPolicyDialogVisible(true))
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MariPrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Keamanan & Privasi",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Kebijakan Privasi, RLS & Ephemeral Storage (Ketuk untuk detail)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Detail Privasi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Row: Versi Aplikasi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MariPrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Versi Aplikasi",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Mari Partner Fuel · v${AppConfig.versionName} (Build 14)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        FuelStatusChip(text = "v${AppConfig.versionName} Resmi", type = FuelChipType.Success)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Tombol Keluar Akun
                FuelSecondaryButton(
                    text = "Keluar dari Akun",
                    onClick = { viewModel.onEvent(ProfileUiEvent.SetLogoutDialogVisible(true)) },
                    leadingIcon = Icons.AutoMirrored.Filled.Logout,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Tombol Hapus Akun & Data (Kepatuhan Wajib Google Play Store)
                FuelDangerButton(
                    text = if (uiState.isDeletingAccount) "Menghapus Akun..." else "Hapus Akun & Seluruh Data",
                    onClick = { viewModel.onEvent(ProfileUiEvent.SetDeleteAccountDialogVisible(true)) },
                    leadingIcon = Icons.Default.DeleteForever,
                    enabled = !uiState.isDeletingAccount,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Footer Hak Cipta & Info Build
                Text(
                    text = "Mari Partner Fuel · v${AppConfig.versionName} (Build 13)\nAll Rights Reserved © 2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
}
}

    // Modal Dialog: Kebijakan Privasi & Ketentuan Layanan (Data Safety Google Play)
    PrivacyPolicyDialog(
        show = uiState.showPrivacyPolicyDialog,
        onDismiss = { viewModel.onEvent(ProfileUiEvent.SetPrivacyPolicyDialogVisible(false)) }
    )

    // Modal Dialog: Konfirmasi Keluar Akun (SweetAlert style)
    if (uiState.showLogoutConfirmDialog) {
        FuelSweetAlertDialog(
            show = true,
            type = SweetAlertType.WARNING,
            title = "Keluar Akun?",
            message = "Apakah Anda yakin ingin keluar dari akun Mari Partner Fuel pada perangkat ini?",
            confirmText = "Ya, Keluar",
            dismissText = "Batal",
            onConfirm = {
                viewModel.onEvent(ProfileUiEvent.SetLogoutDialogVisible(false))
                onLogout()
            },
            onDismiss = {
                viewModel.onEvent(ProfileUiEvent.SetLogoutDialogVisible(false))
            }
        )
    }

    // Modal Dialog: Konfirmasi Hapus Akun Permanen (Google Play Mandatory In-App Deletion)
    if (uiState.showDeleteAccountDialog) {
        FuelSweetAlertDialog(
            show = true,
            type = SweetAlertType.ERROR,
            title = "Hapus Akun & Data?",
            message = "Tindakan ini bersifat permanen dan tidak dapat dibatalkan. Seluruh riwayat transaksi BBM, armada kendaraan, pengingat servis, dan profil akun Anda akan dihapus secara permanen dari server.",
            confirmText = if (uiState.isDeletingAccount) "Menghapus..." else "Hapus Akun Permanen",
            dismissText = "Batal",
            dismissOnBackPress = !uiState.isDeletingAccount,
            dismissOnClickOutside = !uiState.isDeletingAccount,
            onConfirm = {
                if (!uiState.isDeletingAccount) {
                    viewModel.onEvent(ProfileUiEvent.DeleteAccount)
                }
            },
            onDismiss = {
                if (!uiState.isDeletingAccount) {
                    viewModel.onEvent(ProfileUiEvent.SetDeleteAccountDialogVisible(false))
                }
            }
        )
    }
}
