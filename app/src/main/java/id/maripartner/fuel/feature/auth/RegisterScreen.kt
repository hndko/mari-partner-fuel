package id.maripartner.fuel.feature.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.designsystem.component.FuelPasswordField
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.component.FuelSweetAlertDialog
import id.maripartner.fuel.core.designsystem.component.SweetAlertType
import id.maripartner.fuel.core.designsystem.theme.MariElectricBlue

@Composable
fun RegisterScreen(
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Toast untuk pesan error reguler
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            if (!uiState.isDuplicateEmailError) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.onEvent(AuthUiEvent.DismissError)
            }
        }
    }

    // 1. SweetAlert Modal Dialog untuk Sukses Pendaftaran
    FuelSweetAlertDialog(
        show = uiState.isRegistrationSuccess,
        type = SweetAlertType.SUCCESS,
        title = "Pendaftaran Berhasil!",
        message = "Akun Anda (${uiState.registeredEmail ?: email}) telah berhasil dibuat dan aktif. Silakan masuk untuk mulai mengelola pengeluaran BBM Anda.",
        confirmText = "Masuk Sekarang",
        onConfirm = {
            viewModel.onEvent(AuthUiEvent.ResetRegistrationState)
            Toast.makeText(context, "Silakan masukkan kata sandi Anda", Toast.LENGTH_SHORT).show()
            onNavigateBackToLogin()
        }
    )

    // 2. SweetAlert Modal Dialog untuk Deteksi Email Sudah Terdaftar (Duplikat)
    FuelSweetAlertDialog(
        show = uiState.isDuplicateEmailError,
        type = SweetAlertType.WARNING,
        title = "Email Sudah Terdaftar",
        message = "Alamat email '${email.trim()}' sudah terdaftar dalam sistem. Silakan langsung masuk menggunakan akun Anda.",
        confirmText = "Buka Halaman Masuk",
        dismissText = "Ganti Email",
        onConfirm = {
            viewModel.onEvent(AuthUiEvent.ResetRegistrationState)
            onNavigateBackToLogin()
        },
        onDismiss = {
            viewModel.onEvent(AuthUiEvent.DismissError)
        }
    )

    AuthResponsiveLayout(
        title = "Daftar Akun Baru",
        subtitle = "Daftar gratis untuk mencatat transaksi dan scan struk SPBU",
        onNavigateBack = {
            viewModel.onEvent(AuthUiEvent.ResetRegistrationState)
            onNavigateBackToLogin()
        },
        modifier = modifier
    ) {
        // Input Nama Lengkap
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Lengkap") },
            placeholder = { Text("Contoh: Handoko") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (name.isNotEmpty()) MariElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MariElectricBlue,
                focusedLabelColor = MariElectricBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("contoh@email.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = if (email.isNotEmpty()) MariElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MariElectricBlue,
                focusedLabelColor = MariElectricBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Kata Sandi
        FuelPasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Kata Sandi (Min. 6 Karakter)",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Ulangi Kata Sandi
        FuelPasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Ulangi Kata Sandi",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Submit Pendaftaran
        FuelPrimaryButton(
            text = "Daftar Akun",
            onClick = {
                viewModel.onEvent(
                    AuthUiEvent.SignUp(
                        name = name,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                )
            },
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Navigasi Kembali ke Login
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sudah memiliki akun? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = {
                    viewModel.onEvent(AuthUiEvent.ResetRegistrationState)
                    onNavigateBackToLogin()
                }
            ) {
                Text(
                    text = "Masuk di Sini",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
