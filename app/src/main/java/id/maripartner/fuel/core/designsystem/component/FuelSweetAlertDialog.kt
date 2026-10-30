package id.maripartner.fuel.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import id.maripartner.fuel.core.designsystem.theme.MariCobaltBlue
import id.maripartner.fuel.core.designsystem.theme.MariElectricBlue

enum class SweetAlertType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
}

/**
 * Komponen Modal Dialog konfirmasi & alert bergaya SweetAlert modern.
 * Menghadirkan ikon visual berukuran besar, judul tegas, pesan informatif, dan tombol aksi terstandarisasi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelSweetAlertDialog(
    show: Boolean,
    type: SweetAlertType = SweetAlertType.SUCCESS,
    title: String,
    message: String,
    confirmText: String = "Lanjutkan",
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = {},
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true
) {
    if (!show) return

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon Bulat Besar khas SweetAlert
                val iconConfig = when (type) {
                    SweetAlertType.SUCCESS -> Triple(
                        Icons.Default.CheckCircle,
                        Color(0xFF2E7D32),
                        Color(0xFFE8F5E9)
                    )
                    SweetAlertType.WARNING -> Triple(
                        Icons.Default.WarningAmber,
                        Color(0xFFE65100),
                        Color(0xFFFFF3E0)
                    )
                    SweetAlertType.ERROR -> Triple(
                        Icons.Default.ErrorOutline,
                        Color(0xFFC62828),
                        Color(0xFFFFEBEE)
                    )
                    SweetAlertType.INFO -> Triple(
                        Icons.Default.Info,
                        MariCobaltBlue,
                        Color(0xFFE3F2FD)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(iconConfig.third, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconConfig.first,
                        contentDescription = null,
                        tint = iconConfig.second,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Judul Alert
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pesan Deskripsi
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Baris Tombol Aksi
                if (dismissText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text(
                                text = dismissText,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        FuelPrimaryButton(
                            text = confirmText,
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                    }
                } else {
                    FuelPrimaryButton(
                        text = confirmText,
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }
            }
        }
    }
}
