package id.maripartner.fuel.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Mari Partner Official Brand Colors (Berdasarkan Logo Resmi Mari Partner)
val MariCobaltBlue = Color(0xFF0047BA)      // Dark Royal Blue
val MariElectricBlue = Color(0xFF0066FF)    // Vibrant Blue
val MariCyanBlue = Color(0xFF00C2FF)        // Bright Cyan Accent
val MariDarkSlate = Color(0xFF0F172A)       // Text Brand Charcoal
val MariSubtleSlate = Color(0xFF64748B)     // Text Subtitle Muted

// Brand Gradient
val MariBrandGradient = Brush.horizontalGradient(
    colors = listOf(MariCobaltBlue, MariElectricBlue, MariCyanBlue)
)

val MariHeroGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFEBF3FF), Color(0xFFF8FAFC))
)

// Light Color Palette
val MariPrimaryLight = Color(0xFF0052CC)
val OnMariPrimaryLight = Color(0xFFFFFFFF)
val MariPrimaryContainerLight = Color(0xFFEBF3FF)
val OnMariPrimaryContainerLight = Color(0xFF002B75)

val MariSecondaryLight = Color(0xFF00A3FF)
val OnMariSecondaryLight = Color(0xFFFFFFFF)
val MariSecondaryContainerLight = Color(0xFFE0F4FF)
val OnMariSecondaryContainerLight = Color(0xFF003E66)

val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF475569)
val OutlineLight = Color(0xFFCBD5E1)
val OutlineVariantLight = Color(0xFFE2E8F0)

// Dark Color Palette
val MariPrimaryDark = Color(0xFF60A5FA)
val OnMariPrimaryDark = Color(0xFF00296B)
val MariPrimaryContainerDark = Color(0xFF003D99)
val OnMariPrimaryContainerDark = Color(0xFFD8E6FF)

val MariSecondaryDark = Color(0xFF38BDF8)
val OnMariSecondaryDark = Color(0xFF003657)
val MariSecondaryContainerDark = Color(0xFF004D7A)
val OnMariSecondaryContainerDark = Color(0xFFC7EBFF)

val BackgroundDark = Color(0xFF0B1120)
val OnBackgroundDark = Color(0xFFF1F5F9)
val SurfaceDark = Color(0xFF111827)
val OnSurfaceDark = Color(0xFFF1F5F9)
val SurfaceVariantDark = Color(0xFF1E293B)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF475569)
val OutlineVariantDark = Color(0xFF334155)

// Semantic Colors
val SuccessGreen = Color(0xFF16A34A)
val WarningOrange = Color(0xFFEA580C)
val ErrorRedLight = Color(0xFFDC2626)
val ErrorRedDark = Color(0xFFF87171)
