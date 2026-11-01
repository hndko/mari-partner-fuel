package id.maripartner.fuel.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue

/**
 * Dialog resmi Kebijakan Privasi & Ketentuan Layanan (Privacy Policy & Data Safety).
 * Memenuhi standar transparansi Google Play Store & UU Pelindungan Data Pribadi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MariPrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MariPrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Kebijakan Privasi & Keamanan",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Mari Partner Fuel · Komitmen Perlindungan Data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PolicySectionItem(
                        icon = Icons.Default.Lock,
                        title = "1. Data yang Dikumpulkan",
                        description = "Kami mengumpulkan data akun (nama, email) dan data operasional kendaraan (riwayat nominal BBM, volume liter, odometer, dan model kendaraan) untuk kalkulasi efisiensi konsumsi BBM Anda."
                    )

                    PolicySectionItem(
                        icon = Icons.Default.CameraAlt,
                        title = "2. Pemrosesan Foto Struk (Local-First)",
                        description = "Pemindaian teks struk diproses di perangkat lokal menggunakan Google ML Kit. Kami tidak menyimpan atau mengunggah foto fisik struk secara permanen ke server, melainkan hanya menyimpan data angka relasional yang telah Anda setujui."
                    )

                    PolicySectionItem(
                        icon = Icons.Default.MyLocation,
                        title = "3. Penggunaan Izin Lokasi",
                        description = "Izin lokasi hanya digunakan saat aplikasi dibuka (foreground) untuk mendeteksi kedekatan SPBU (~250m). Mari Partner Fuel tidak melacak pergerakan perangkat Anda di latar belakang."
                    )

                    PolicySectionItem(
                        icon = Icons.Default.Shield,
                        title = "4. Enkripsi & Isolasi Row Level Security",
                        description = "Seluruh koneksi jaringan menggunakan enkripsi TLS/HTTPS yang ketat. Basis data dilindungi oleh PostgreSQL Row Level Security (RLS) sehingga hanya akun pemilik yang memiliki akses ke data kendaraan dan transaksi."
                    )

                    PolicySectionItem(
                        icon = Icons.Default.DeleteForever,
                        title = "5. Hak Pengguna & Hapus Akun Mandiri",
                        description = "Sesuai regulasi Google Play dan UU PDP, Anda berhak mengekspor data dalam bentuk PDF/CSV dan dapat menghapus akun beserta seluruh data riwayatnya secara permanen langsung melalui menu Profil."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Close CTA Button
                FuelPrimaryButton(
                    text = "Saya Mengerti & Setuju",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PolicySectionItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MariPrimaryBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MariPrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
