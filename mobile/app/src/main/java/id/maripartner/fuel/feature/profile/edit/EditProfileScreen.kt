package id.maripartner.fuel.feature.profile.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.designsystem.component.FuelCard
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSecondaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.FuelTextField
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.theme.MariBorderLight
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue

private data class TimezoneOption(
    val code: String,
    val title: String,
    val description: String
)

private val AVAILABLE_TIMEZONES = listOf(
    TimezoneOption(
        code = "Asia/Jakarta",
        title = "WIB (UTC+7)",
        description = "Waktu Indonesia Barat (Sumatra, Jawa, Kalbar, Kalteng)"
    ),
    TimezoneOption(
        code = "Asia/Makassar",
        title = "WITA (UTC+8)",
        description = "Waktu Indonesia Tengah (Bali, Nusa Tenggara, Kalsel, Kaltim, Sulawesi)"
    ),
    TimezoneOption(
        code = "Asia/Jayapura",
        title = "WIT (UTC+9)",
        description = "Waktu Indonesia Timur (Maluku, Papua)"
    )
)

/**
 * Halaman Khusus Ubah Profil & Kata Sandi Akun Mari Partner Fuel.
 * Menghadirkan:
 * - Email berstatus read-only / locked permanen (tidak dapat diubah)
 * - Ubah Nama Lengkap
 * - Pemilihan Zona Waktu resmi Indonesia (WIB, WITA, WIT)
 * - Ubah Kata Sandi terintegrasi Supabase Auth dengan konfirmasi dan eye toggle
 * - Pencegahan double-submit & alert SweetAlert
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FuelTopBar(
                title = "Edit Profil Akun",
                onNavigateBack = onNavigateBack
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MariPrimaryBlue,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Eyebrow & Judul
                    Column {
                        Text(
                            text = "PENGATURAN AKUN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.6.sp
                            ),
                            color = MariPrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ubah Informasi Akun",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Perbarui nama lengkap, zona waktu, dan kata sandi Anda. Alamat email terdaftar bersifat permanen untuk keamanan data.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 1. KARTU IDENTITAS PROFIL
                    FuelCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MariPrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Identitas Pengguna",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Data dasar yang tampil di aplikasi dan laporan pengeluaran BBM",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Email (READ-ONLY / DISABLED)
                        Column {
                            FuelTextField(
                                value = uiState.email.ifBlank { "Sedang memuat..." },
                                onValueChange = {},
                                label = "Alamat Email Akun (Permanen)",
                                leadingIcon = Icons.Default.Email,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Email terkunci",
                                        tint = MariMutedLight.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                enabled = false,
                                readOnly = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Badge info keamanan email
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MariMutedLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isGoogleLinked) {
                                        "Email ini terhubung dengan akun Google Anda dan tidak dapat diubah demi integritas data."
                                    } else {
                                        "Alamat email tidak dapat diubah untuk menjamin integritas transaksi dan keamanan akun."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nama Lengkap (EDITABLE)
                        FuelTextField(
                            value = uiState.displayName,
                            onValueChange = { viewModel.onEvent(EditProfileUiEvent.DisplayNameChanged(it)) },
                            label = "Nama Lengkap *",
                            placeholder = "Contoh: Handoko Prasetyo",
                            leadingIcon = Icons.Default.Person,
                            errorMessage = uiState.displayNameError,
                            enabled = !uiState.isSaving
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Zona Waktu (TIMEZONE SELECTION)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MariPrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Zona Waktu Operasional *",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            AVAILABLE_TIMEZONES.forEach { tz ->
                                val isSelected = tz.code == uiState.timezone
                                val cardBorder = if (isSelected) MariPrimaryBlue else MaterialTheme.colorScheme.outlineVariant
                                val cardBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = cardBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(cardBg)
                                        .clickable(enabled = !uiState.isSaving) {
                                            viewModel.onEvent(EditProfileUiEvent.TimezoneChanged(tz.code))
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                if (!uiState.isSaving) {
                                                    viewModel.onEvent(EditProfileUiEvent.TimezoneChanged(tz.code))
                                                }
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MariPrimaryBlue
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tz.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) MariPrimaryBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = tz.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. KARTU UBAH KATA SANDI
                    FuelCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MariPrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Keamanan & Kata Sandi",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Ubah kata sandi login manual Anda",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Box petunjuk
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MariPrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kosongkan kolom kata sandi di bawah jika Anda tidak ingin mengubah kata sandi akun saat ini.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Baru
                        FuelTextField(
                            value = uiState.newPassword,
                            onValueChange = { viewModel.onEvent(EditProfileUiEvent.NewPasswordChanged(it)) },
                            label = "Kata Sandi Baru",
                            placeholder = "Minimal 6 karakter",
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.onEvent(EditProfileUiEvent.TogglePasswordVisibility) }) {
                                    Icon(
                                        imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (uiState.isPasswordVisible) "Sembunyikan" else "Tampilkan",
                                        tint = MariMutedLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            errorMessage = uiState.passwordError,
                            enabled = !uiState.isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Konfirmasi Password Baru
                        FuelTextField(
                            value = uiState.confirmPassword,
                            onValueChange = { viewModel.onEvent(EditProfileUiEvent.ConfirmPasswordChanged(it)) },
                            label = "Ulangi Kata Sandi Baru",
                            placeholder = "Masukkan ulang kata sandi baru",
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.onEvent(EditProfileUiEvent.ToggleConfirmPasswordVisibility) }) {
                                    Icon(
                                        imageVector = if (uiState.isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (uiState.isConfirmPasswordVisible) "Sembunyikan" else "Tampilkan",
                                        tint = MariMutedLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            errorMessage = uiState.confirmPasswordError,
                            enabled = !uiState.isSaving
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. TOMBOL AKSI
                    FuelPrimaryButton(
                        text = "Simpan Perubahan",
                        onClick = { viewModel.onEvent(EditProfileUiEvent.SaveProfile) },
                        leadingIcon = Icons.Default.Check,
                        isLoading = uiState.isSaving,
                        enabled = !uiState.isSaving && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    FuelSecondaryButton(
                        text = "Batal",
                        onClick = onNavigateBack,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Modal SweetAlert: Berhasil Simpan
    if (uiState.showSuccessDialog) {
        FuelSweetAlertDialog(
            show = true,
            type = SweetAlertType.SUCCESS,
            title = "Profil Berhasil Diperbarui",
            message = "Informasi akun dan kata sandi Anda telah berhasil disimpan dengan aman.",
            confirmText = "Selesai",
            onConfirm = {
                viewModel.onEvent(EditProfileUiEvent.DismissDialog)
                onNavigateBack()
            }
        )
    }

    // Modal SweetAlert: Gagal Simpan
    if (uiState.showErrorDialog) {
        FuelSweetAlertDialog(
            show = true,
            type = SweetAlertType.ERROR,
            title = "Gagal Memperbarui",
            message = uiState.dialogErrorMessage.ifBlank { "Terjadi kendala saat menyimpan profil. Silakan coba lagi." },
            confirmText = "Tutup",
            onConfirm = {
                viewModel.onEvent(EditProfileUiEvent.DismissDialog)
            }
        )
    }
}
