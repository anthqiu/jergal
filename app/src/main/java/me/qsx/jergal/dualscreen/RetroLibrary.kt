package me.qsx.jergal.dualscreen

import android.net.Uri
import me.qsx.jergal.ui.theme.ThemeColorRole

/**
 * Immutable launcher metadata for a single retro game tile and its top-screen showcase content.
 */
data class RetroGame(
    val id: String,
    val title: String,
    val logo: String,
    val tagline: String,
    val heroSummary: String,
    val playerLabel: String,
    val platformLabel: String,
    val accentRole: ThemeColorRole,
    val secondaryAccentRole: ThemeColorRole,
    val romUri: Uri? = null,
    val romPath: String? = null,
    val launchArgumentsTemplate: String? = null,
    val launchPackageName: String? = null,
    val launchActivityName: String? = null,
    val metadataSources: List<String> = emptyList(),
    val isLaunchable: Boolean = false,
)
