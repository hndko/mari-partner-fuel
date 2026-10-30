package id.maripartner.fuel.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.designsystem.theme.MariAiTag
import id.maripartner.fuel.core.designsystem.theme.MariAiTagBg
import id.maripartner.fuel.core.designsystem.theme.MariDanger
import id.maripartner.fuel.core.designsystem.theme.MariDangerBg
import id.maripartner.fuel.core.designsystem.theme.MariInfo
import id.maripartner.fuel.core.designsystem.theme.MariInfoBg
import id.maripartner.fuel.core.designsystem.theme.MariSuccess
import id.maripartner.fuel.core.designsystem.theme.MariSuccessBg
import id.maripartner.fuel.core.designsystem.theme.MariWarning
import id.maripartner.fuel.core.designsystem.theme.MariWarningBg

enum class FuelChipType {
    Success,
    Warning,
    Info,
    Danger,
    AiTag,
    Neutral
}

/**
 * Chip status terstandar resmi Mari Partner Fuel.
 * Menampilkan teks status dengan latar belakang lembut (*soft background*) dan kontras yang jelas.
 */
@Composable
fun FuelStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    type: FuelChipType = FuelChipType.Info,
    icon: ImageVector? = null
) {
    val (backgroundColor, textColor) = when (type) {
        FuelChipType.Success -> MariSuccessBg to MariSuccess
        FuelChipType.Warning -> MariWarningBg to MariWarning
        FuelChipType.Info -> MariInfoBg to MariInfo
        FuelChipType.Danger -> MariDangerBg to MariDanger
        FuelChipType.AiTag -> MariAiTagBg to MariAiTag
        FuelChipType.Neutral -> Color(0xFFF0F3F7) to Color(0xFF64748B)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = textColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = textColor
            )
        }
    }
}
