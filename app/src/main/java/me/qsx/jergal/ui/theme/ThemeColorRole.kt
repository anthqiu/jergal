package me.qsx.jergal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Theme color roles that game metadata may reference without hardcoding concrete color values.
 */
enum class ThemeColorRole {
    Primary,
    Secondary,
    Tertiary,
    PrimaryContainer,
    SecondaryContainer,
    TertiaryContainer,
}

/**
 * Resolves this theme role to its current Material color.
 */
@Composable
fun ThemeColorRole.color(): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when (this) {
        ThemeColorRole.Primary -> colorScheme.primary
        ThemeColorRole.Secondary -> colorScheme.secondary
        ThemeColorRole.Tertiary -> colorScheme.tertiary
        ThemeColorRole.PrimaryContainer -> colorScheme.primaryContainer
        ThemeColorRole.SecondaryContainer -> colorScheme.secondaryContainer
        ThemeColorRole.TertiaryContainer -> colorScheme.tertiaryContainer
    }
}

/**
 * Resolves this theme role to the matching on-color from the current Material color scheme.
 */
@Composable
fun ThemeColorRole.onColor(): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when (this) {
        ThemeColorRole.Primary -> colorScheme.onPrimary
        ThemeColorRole.Secondary -> colorScheme.onSecondary
        ThemeColorRole.Tertiary -> colorScheme.onTertiary
        ThemeColorRole.PrimaryContainer -> colorScheme.onPrimaryContainer
        ThemeColorRole.SecondaryContainer -> colorScheme.onSecondaryContainer
        ThemeColorRole.TertiaryContainer -> colorScheme.onTertiaryContainer
    }
}
