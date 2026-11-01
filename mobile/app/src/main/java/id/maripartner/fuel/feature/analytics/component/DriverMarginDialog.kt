package id.maripartner.fuel.feature.analytics.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.maripartner.fuel.core.designsystem.component.ThousandSeparatorVisualTransformation
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue

@Composable
fun DriverMarginDialog(
    currentRateIdr: Long,
    onDismiss: () -> Unit,
    onSaveRate: (Long) -> Unit
) {
    var rawText by remember {
        mutableStateOf(if (currentRateIdr > 0) currentRateIdr.toString() else "3500")
    }

    val presets = listOf(
        2500L to "Rp 2.500",
        3500L to "Rp 3.500",
        4500L to "Rp 4.500",
        6000L to "Rp 6.000"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Simulasi Margin Pengemudi Online",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Masukkan tarif pendapatan kotor per kilometer perjalanan untuk menghitung margin keuntungan bersih setelah dipotong True Cost operasional riil (BBM + Servis).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        rawText = digits
                    },
                    label = { Text("Pendapatan / Tarif per Km (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Preset Tarif Umum:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (amount, label) ->
                        FilterChip(
                            selected = rawText == amount.toString(),
                            onClick = { rawText = amount.toString() },
                            label = { Text(text = label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rawText.toLongOrNull() ?: 3500L
                    onSaveRate(rate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MariPrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Terapkan Simulasi")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal")
            }
        }
    )
}
