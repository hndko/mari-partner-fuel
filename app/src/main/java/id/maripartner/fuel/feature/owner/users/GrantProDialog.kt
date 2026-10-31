package id.maripartner.fuel.feature.owner.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.domain.model.owner.AdminUserSummary

@Composable
fun GrantProDialog(
    targetUser: AdminUserSummary,
    selectedDays: Int,
    notes: String,
    isSubmitting: Boolean,
    onDaysChanged: (Int) -> Unit,
    onNotesChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⭐", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aktivasi Pro Manual",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Berikan hak istimewa status Pro kepada akun pengguna:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = targetUser.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ID: ${targetUser.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Pilih Durasi Tambahan:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                DurationRadioOption(
                    title = "30 Hari (1 Bulan)",
                    days = 30,
                    isSelected = selectedDays == 30,
                    onSelect = { onDaysChanged(30) }
                )
                DurationRadioOption(
                    title = "90 Hari (3 Bulan)",
                    days = 90,
                    isSelected = selectedDays == 90,
                    onSelect = { onDaysChanged(90) }
                )
                DurationRadioOption(
                    title = "365 Hari (1 Tahun Penuh)",
                    days = 365,
                    isSelected = selectedDays == 365,
                    onSelect = { onDaysChanged(365) }
                )
                DurationRadioOption(
                    title = "Permanen (Seumur Hidup / Lifetime) ⭐",
                    days = 0,
                    isSelected = selectedDays == 0,
                    onSelect = { onDaysChanged(0) }
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChanged,
                    label = { Text("Catatan / Alasan Pemberian (Opsional)") },
                    placeholder = { Text("Misal: Hadiah loyalitas, uji coba beta") },
                    maxLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = MariPrimaryBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Konfirmasi & Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun DurationRadioOption(
    title: String,
    days: Int,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}
