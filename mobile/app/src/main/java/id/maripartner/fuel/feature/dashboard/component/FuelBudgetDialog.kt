package id.maripartner.fuel.feature.dashboard.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.designsystem.component.ThousandSeparatorVisualTransformation
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue

@Composable
fun FuelBudgetDialog(
    initialBudgetIdr: Long,
    onDismiss: () -> Unit,
    onSaveBudget: (Long) -> Unit
) {
    var rawText by remember {
        mutableStateOf(if (initialBudgetIdr > 0) initialBudgetIdr.toString() else "")
    }

    val presets = listOf(
        500_000L to "500 Rb",
        1_000_000L to "1 Juta",
        1_500_000L to "1.5 Juta",
        2_500_000L to "2.5 Juta"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Atur Pagu Anggaran BBM",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tentukan batas maksimal uang bensin bulanan Anda. Aplikasi akan memperingatkan jika pengeluaran mendekati atau melebihi pagu.",
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
                    label = { Text("Pagu Bulanan (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pilihan Cepat:",
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
                    val amount = rawText.toLongOrNull() ?: 0L
                    onSaveBudget(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MariPrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Simpan Pagu")
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
