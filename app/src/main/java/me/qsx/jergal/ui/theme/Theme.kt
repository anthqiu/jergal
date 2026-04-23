package me.qsx.jergal.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Ember,
    onPrimary = NightInk,
    primaryContainer = Amber,
    onPrimaryContainer = NightInk,
    secondary = SkyPulse,
    onSecondary = NightInk,
    secondaryContainer = Lagoon,
    onSecondaryContainer = Mist,
    tertiary = Amber,
    onTertiary = NightInk,
    background = NightInk,
    onBackground = Mist,
    surface = NightSurface,
    onSurface = Mist,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = MistMuted,
    outline = OutlineBlue
)

private val LightColorScheme = lightColorScheme(
    primary = Ocean,
    onPrimary = Sand,
    primaryContainer = CoralMist,
    onPrimaryContainer = NightInk,
    secondary = Lagoon,
    onSecondary = Sand,
    secondaryContainer = SkyPulse,
    onSecondaryContainer = NightInk,
    tertiary = Amber,
    onTertiary = NightInk,
    background = Sand,
    onBackground = NightInk,
    surface = Paper,
    onSurface = NightInk,
    surfaceVariant = Cloud,
    onSurfaceVariant = Slate,
    outline = OutlineBlue
)

/**
 * Applies the shared Jergal Material theme for both screens.
 */
@Composable
fun JergalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = JergalShapes,
        content = content
    )
}
