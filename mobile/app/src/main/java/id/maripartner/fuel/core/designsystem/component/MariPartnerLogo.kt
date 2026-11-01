package id.maripartner.fuel.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.designsystem.theme.MariCobaltBlue
import id.maripartner.fuel.core.designsystem.theme.MariCyanBlue
import id.maripartner.fuel.core.designsystem.theme.MariElectricBlue

/**
 * Visual canvas representasi logo ribbon 'M' khas Mari Partner sesuai identitas visual resmi.
 */
@Composable
fun MariPartnerRibbonM(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val gradientMain = Brush.linearGradient(
            colors = listOf(MariCobaltBlue, MariElectricBlue, MariCyanBlue),
            start = Offset(0f, h),
            end = Offset(w, 0f)
        )

        val gradientFold = Brush.linearGradient(
            colors = listOf(MariCobaltBlue.copy(alpha = 0.85f), MariElectricBlue),
            start = Offset(w * 0.2f, 0f),
            end = Offset(w * 0.7f, h)
        )

        // Sayap kiri M (Left stalk)
        val leftPath = Path().apply {
            moveTo(w * 0.12f, h * 0.90f)
            cubicTo(
                w * 0.10f, h * 0.50f,
                w * 0.22f, h * 0.15f,
                w * 0.35f, h * 0.10f
            )
            lineTo(w * 0.50f, h * 0.75f)
            lineTo(w * 0.38f, h * 0.85f)
            lineTo(w * 0.26f, h * 0.35f)
            close()
        }
        drawPath(path = leftPath, brush = gradientMain)

        // Lipatan pita tengah dan kanan (Folded ribbon)
        val ribbonPath = Path().apply {
            moveTo(w * 0.35f, h * 0.10f)
            cubicTo(
                w * 0.48f, h * 0.08f,
                w * 0.60f, h * 0.25f,
                w * 0.62f, h * 0.40f
            )
            lineTo(w * 0.42f, h * 0.88f)
            cubicTo(
                w * 0.48f, h * 0.95f,
                w * 0.60f, h * 0.95f,
                w * 0.68f, h * 0.80f
            )
            lineTo(w * 0.88f, h * 0.20f)
            cubicTo(
                w * 0.90f, h * 0.12f,
                w * 0.78f, h * 0.10f,
                w * 0.72f, h * 0.22f
            )
            lineTo(w * 0.58f, h * 0.70f)
            close()
        }
        drawPath(path = ribbonPath, brush = gradientFold)

        // Stroke highlight aksen berkilau di puncak lipatan
        val highlightPath = Path().apply {
            moveTo(w * 0.62f, h * 0.40f)
            lineTo(w * 0.88f, h * 0.20f)
        }
        drawPath(
            path = highlightPath,
            brush = Brush.linearGradient(listOf(MariElectricBlue, MariCyanBlue)),
            style = Stroke(width = w * 0.04f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Header Identitas Brand Mari Partner yang modern & elegan untuk halaman autentikasi.
 */
@Composable
fun MariPartnerBrandHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo Container dengan Efek Elevated Soft Glow
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp,
            modifier = Modifier.size(80.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFEBF4FF), Color.White)
                        )
                    )
                    .padding(10.dp)
            ) {
                MariPartnerRibbonM(size = 56.dp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Brand Name Text: "Mari Partner" + Badge "Fuel"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Mari ",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp
                ),
                color = MariCobaltBlue
            )
            Text(
                text = "Partner",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MariElectricBlue.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = MariElectricBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "FUEL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MariElectricBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle slogan brand
        Text(
            text = "Your Digital Fuel Partner",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Layar Title & Keterangan
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
