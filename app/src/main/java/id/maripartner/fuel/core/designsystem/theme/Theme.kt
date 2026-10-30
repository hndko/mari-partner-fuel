package id.maripartner.fuel.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Palet warna resmi Terang (Light Mode) Mari Partner Fuel.
 * Sesuai referensi resmi DESIGN_SYSTEM.md dan styles.css.
 */
private val LightColorScheme = lightColorScheme(
    primary = MariPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = MariInfoBg,
    onPrimaryContainer = MariBrandNavy,
    secondary = MariAccentCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F9FC),
    onSecondaryContainer = Color(0xFF0C2456),
    tertiary = MariBrandNavy,
    onTertiary = Color.White,
    background = MariBgLight,
    onBackground = MariTextLight,
    surface = MariSurfaceLight,
    onSurface = MariTextLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = MariMutedLight,
    outline = MariBorderLight,
    outlineVariant = MariBorderSubtle,
    error = MariDanger,
    onError = Color.White,
    errorContainer = MariDangerBg,
    onErrorContainer = MariDanger
)

/**
 * Palet warna resmi Gelap (Dark Mode) Mari Partner Fuel.
 * Sesuai referensi resmi DESIGN_SYSTEM.md dan styles.css (`body.dark`).
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF389CF2),
    onPrimary = Color(0xFF002B55),
    primaryContainer = Color(0xFF173B61),
    onPrimaryContainer = Color(0xFFC7E3FF),
    secondary = MariAccentCyan,
    onSecondary = Color(0xFF003547),
    secondaryContainer = Color(0xFF0D3F54),
    onSecondaryContainer = Color(0xFFBCEEFF),
    tertiary = Color(0xFF6B8BCC),
    onTertiary = Color.White,
    background = MariBgDark,
    onBackground = MariTextDark,
    surface = MariSurfaceDark,
    onSurface = MariTextDark,
    surfaceVariant = Color(0xFF1E2B3D),
    onSurfaceVariant = MariMutedDark,
    outline = MariBorderDark,
    outlineVariant = Color(0xFF35465D),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF5A1D1D),
    onErrorContainer = Color(0xFFFFB4AB)
)

@Composable
fun MariPartnerFuelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Tetap prioritaskan identitas warna resmi Mari Partner
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                windowInsetsController.isAppearanceLightStatusBars = !darkTheme
                windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
