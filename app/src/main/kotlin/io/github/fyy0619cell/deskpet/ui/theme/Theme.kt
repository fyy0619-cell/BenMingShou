package io.github.fyy0619cell.deskpet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Palette {
    val Paper = Color(0xFFF6F4F1)
    val Card = Color(0xFFFCFBF9)
    val Ink = Color(0xFF231E29)
    val Muted = Color(0xFF8C8592)
    val Hairline = Color(0x1F231E29)
    val Gold = Color(0xFFB98A2E)
}

/** The user's pet tints the whole app once adopted; before that everything is ink. */
val LocalAccent = staticCompositionLocalOf { Palette.Ink }

private val Serif = FontFamily.Serif

private val type = Typography(
    displayMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 38.sp, lineHeight = 50.sp, letterSpacing = 1.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 26.sp, lineHeight = 38.sp, letterSpacing = 0.5.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 28.sp, letterSpacing = 0.3.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 3.sp),
)

@Composable
fun DeskTheme(accent: Color = Palette.Ink, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            background = Palette.Paper,
            surface = Palette.Card,
            onBackground = Palette.Ink,
            onSurface = Palette.Ink,
            outline = Palette.Hairline,
        ),
        typography = type,
    ) {
        CompositionLocalProvider(LocalAccent provides accent, content = content)
    }
}
