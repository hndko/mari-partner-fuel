package id.maripartner.fuel.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// Mari Partner Fuel — Official Design Tokens (DESIGN_SYSTEM.md Reference)
// ============================================================================

// 1. Core Brand Colors
val MariBrandNavy = Color(0xFF0C2456)        // Sidebar, Hero, Headings kuat
val MariPrimaryBlue = Color(0xFF087FE5)      // CTA, Links, Selected state
val MariAccentCyan = Color(0xFF11A7EE)       // Highlights, Accent badges

// Alias Brand
val MariCobaltBlue = MariBrandNavy
val MariElectricBlue = MariPrimaryBlue
val MariCyanBlue = MariAccentCyan

// 2. Background & Surface Colors (Light)
val MariBgLight = Color(0xFFF5F7FB)          // Background halaman terang
val MariSurfaceLight = Color(0xFFFFFFFF)     // Surface Card / Form terang
val MariTextLight = Color(0xFF182338)        // Teks utama terang
val MariMutedLight = Color(0xFF788397)       // Teks keterangan / subtitle
val MariBorderLight = Color(0xFFE9EDF4)      // Border garis dan pemisah halus
val MariBorderSubtle = Color(0xFFDCE4EF)     // Border tombol outline

// 3. Background & Surface Colors (Dark)
val MariBgDark = Color(0xFF0E1727)           // Background halaman gelap
val MariSurfaceDark = Color(0xFF172337)      // Surface Card / Form gelap
val MariTextDark = Color(0xFFE7EDF6)         // Teks utama gelap
val MariMutedDark = Color(0xFFA9B7CA)        // Teks keterangan gelap
val MariBorderDark = Color(0xFF29374B)       // Border garis gelap

// 4. Semantic Status Colors
val MariSuccess = Color(0xFF169B70)          // Status Berhasil / Positif
val MariSuccessBg = Color(0xFFEDF8F3)        // Background chip Berhasil
val MariWarning = Color(0xFFC9871C)          // Perlu Perhatian / Warning
val MariWarningBg = Color(0xFFFFF5E6)        // Background chip Warning
val MariDanger = Color(0xFFD85B55)           // Aksi Destruktif / Error
val MariDangerBg = Color(0xFFFFF1F0)         // Background chip Error
val MariInfo = Color(0xFF1677C9)             // Informasi / Netral aktif
val MariInfoBg = Color(0xFFEDF5FF)           // Background chip Info
val MariAiTag = Color(0xFF7C5BD2)            // Tag Smart AI / OCR
val MariAiTagBg = Color(0xFFF2EDFF)          // Background chip AI

// Backward-compatibility aliases
val MariDarkSlate = MariTextLight
val MariSubtleSlate = MariMutedLight
val SuccessGreen = MariSuccess
val WarningOrange = MariWarning
val ErrorRedLight = MariDanger
val ErrorRedDark = Color(0xFFF87171)

// 5. Official Gradients
val MariBrandGradient = Brush.horizontalGradient(
    colors = listOf(MariBrandNavy, MariPrimaryBlue, MariAccentCyan)
)

val MariHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF09255B), Color(0xFF0756A9), Color(0xFF078EE4))
)

val MariButtonGradient = Brush.horizontalGradient(
    colors = listOf(MariPrimaryBlue, MariAccentCyan)
)
