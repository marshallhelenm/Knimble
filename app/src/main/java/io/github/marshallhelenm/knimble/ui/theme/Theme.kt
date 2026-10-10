package io.github.marshallhelenm.knimble.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.marshallhelenm.knimble.model.Tier

// Primary is the palette's darkest blue because it's the only one that keeps white text readable.
private val LightColors = lightColorScheme(
    primary = Cerulean,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCBE6F2),
    onPrimaryContainer = Color(0xFF0E3446),
    secondary = Zomp,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3EEE4),
    onSecondaryContainer = Color(0xFF123D30),
    tertiary = Pumpkin,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC8),
    onTertiaryContainer = Color(0xFF4A1E05),
    error = Strawberry,
    onError = Color.White,
    background = Cream,
    onBackground = Cocoa,
    surface = Cream,
    onSurface = Cocoa,
    surfaceVariant = Oat,
    onSurfaceVariant = Color(0xFF5E514A),
    // Material's defaults for these are cool lavenders; warm them to match the cream.
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFCF3E7),
    surfaceContainer = Oat,
    surfaceContainerHigh = Color(0xFFF0E4D4),
    surfaceContainerHighest = Color(0xFFEADDCB),
    outlineVariant = Color(0xFFDCCFC2)
)

private val DarkColors = darkColorScheme(
    primary = Saffron,
    onPrimary = Bark,
    primaryContainer = Color(0xFF5A4614),
    onPrimaryContainer = Color(0xFFFFE6A6),
    secondary = Pistachio,
    onSecondary = Bark,
    secondaryContainer = Color(0xFF34482A),
    onSecondaryContainer = Color(0xFFD8EEC6),
    tertiary = Coral,
    onTertiary = Bark,
    tertiaryContainer = Color(0xFF5E2D17),
    onTertiaryContainer = Color(0xFFFFDBCB),
    error = Color(0xFFFF8A8C),
    onError = Bark,
    background = Bark,
    onBackground = Oat,
    surface = Bark,
    onSurface = Oat,
    surfaceVariant = Walnut,
    onSurfaceVariant = Color(0xFFD6C6BA),
    surfaceContainerLowest = Color(0xFF1D1715),
    surfaceContainerLow = Color(0xFF2C2420),
    surfaceContainer = Walnut,
    surfaceContainerHigh = Color(0xFF3D322D),
    surfaceContainerHighest = Color(0xFF483B35),
    outlineVariant = Color(0xFF5A4C45)
)

/** Each tier wears its own stripe of the rainbow. */
fun Tier.color(): Color = when (this) {
    Tier.SMALL -> Pistachio
    Tier.MEDIUM -> Saffron
    Tier.LARGE -> Coral
}

@Composable
fun KnimbleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
