package id.maripartner.fuel.core.designsystem.component

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue

enum class SweetAlertType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
}

/**
 * Komponen Modal Dialog konfirmasi & alert modern berstandar enterprise.
 * Menghadirkan ikon visual berlapis (concentric pulse ring), kartu konten terstruktur,
 * tipografi berkontras tinggi, dan tombol aksi terstandarisasi.
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
            usePlatformDefaultWidth = false,
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        val alertConfig = when (type) {
            SweetAlertType.SUCCESS -> AlertConfig(
                icon = Icons.Default.CheckCircle,
                primaryColor = Color(0xFF16A34A),
                glowColor = Color(0xFF16A34A).copy(alpha = 0.12f),
                ringColor = Color(0xFF16A34A).copy(alpha = 0.22f),
                badgeText = "BERHASIL"
            )
            SweetAlertType.WARNING -> AlertConfig(
                icon = Icons.Default.WarningAmber,
                primaryColor = Color(0xFFD97706),
                glowColor = Color(0xFFD97706).copy(alpha = 0.12f),
                ringColor = Color(0xFFD97706).copy(alpha = 0.22f),
                badgeText = "PERHATIAN"
            )
            SweetAlertType.ERROR -> AlertConfig(
                icon = Icons.Default.ErrorOutline,
                primaryColor = Color(0xFFDC2626),
                glowColor = Color(0xFFDC2626).copy(alpha = 0.12f),
                ringColor = Color(0xFFDC2626).copy(alpha = 0.22f),
                badgeText = "KENDALA"
            )
            SweetAlertType.INFO -> AlertConfig(
                icon = Icons.Default.Info,
                primaryColor = MariPrimaryBlue,
                glowColor = MariPrimaryBlue.copy(alpha = 0.12f),
                ringColor = MariPrimaryBlue.copy(alpha = 0.22f),
                badgeText = "INFORMASI"
            )
        }

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .widthIn(max = 440.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Layered Concentric Glow Badge
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(alertConfig.glowColor),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(alertConfig.ringColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = alertConfig.icon,
                            contentDescription = null,
                            tint = alertConfig.primaryColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Alert Category Pill Chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = alertConfig.glowColor
                ) {
                    Text(
                        text = alertConfig.badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        ),
                        color = alertConfig.primaryColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Judul Alert
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Body Pesan Terstruktur
                val hasBullets = message.contains("•") || message.lines().any { it.trim().startsWith("•") }
                if (hasBullets) {
                    val parts = message.split("\n\n")
                    val intro = parts.firstOrNull()?.takeIf { !it.contains("•") }
                    val bulletSection = if (intro != null) parts.drop(1).joinToString("\n\n") else message

                    if (!intro.isNullOrBlank()) {
                        Text(
                            text = intro,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bulletSection.lines().filter { it.isNotBlank() }.forEach { line ->
                                if (line.trim().startsWith("•")) {
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = alertConfig.primaryColor
                                            )
                                        )
                                        Text(
                                            text = line.trim().removePrefix("•").trim(),
                                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Baris Tombol Aksi
                if (dismissText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = dismissText,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == SweetAlertType.ERROR) Color(0xFFDC2626) else MariPrimaryBlue
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = confirmText,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == SweetAlertType.ERROR) Color(0xFFDC2626) else MariPrimaryBlue
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private data class AlertConfig(
    val icon: ImageVector,
    val primaryColor: Color,
    val glowColor: Color,
    val ringColor: Color,
    val badgeText: String
)
