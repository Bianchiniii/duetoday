package br.com.contaemdia.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F6B4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F4E7),
    onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF8A6A16),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE9A8),
    onSecondaryContainer = Color(0xFF2A1E00),
    tertiary = Color(0xFF2F665B),
    tertiaryContainer = Color(0xFFCDEDE5),
    background = Color(0xFFF7FAF4),
    onBackground = Color(0xFF18201B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18201B),
    surfaceVariant = Color(0xFFE0E9E1),
    onSurfaceVariant = Color(0xFF414942),
    surfaceContainerLow = Color(0xFFF0F6EF),
    surfaceContainer = Color(0xFFEAF2E8),
    outline = Color(0xFF6E7A70),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8EE0BF),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF005139),
    onPrimaryContainer = Color(0xFFA9FCD9),
    secondary = Color(0xFFE6C568),
    onSecondary = Color(0xFF493700),
    secondaryContainer = Color(0xFF684F00),
    onSecondaryContainer = Color(0xFFFFE39A),
    tertiary = Color(0xFFA6D6CC),
    tertiaryContainer = Color(0xFF144E44),
    background = Color(0xFF101511),
    onBackground = Color(0xFFE0E5DD),
    surface = Color(0xFF171D19),
    onSurface = Color(0xFFE0E5DD),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC0C9C0),
    surfaceContainerLow = Color(0xFF1C231F),
    surfaceContainer = Color(0xFF222A25),
    outline = Color(0xFF8A958B),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

@Composable
fun ContaEmDiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        shapes = AppShapes,
        content = content,
    )
}
