package com.example.mykasir.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Gunakan warna dari Color.kt
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = OrangeAccent,
    background = BackgroundPrimary,
    surface = SurfacePrimary,
    onPrimary = TextOnPrimary, // Teks di atas 'primary' (Putih)
    onSecondary = Color.White,
    onBackground = TextPrimary, // Teks di atas 'background' (Hitam)
    onSurface = TextPrimary, // Teks di atas 'surface' (Hitam)
    error = RedWarning
)

private val DarkColorScheme = darkColorScheme(
    // Anda bisa atur tema gelap nanti, untuk sekarang kita samakan
    primary = PrimaryBlue,
    secondary = OrangeAccent,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = TextOnPrimary,
    onBackground = Color.White,
    onSurface = Color.White,
    error = RedWarning
)

@Composable
fun MyKasirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Pastikan Anda punya file Typography.kt
        shapes = Shapes(
            small = RoundedCornerShape(6.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(20.dp)
        ),
        content = content
    )
}