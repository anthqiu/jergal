package me.qsx.jergal.dualscreen

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

internal data class LauncherAppEntry(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable?,
)

internal fun loadLauncherApps(packageManager: PackageManager): List<LauncherAppEntry> {
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return packageManager.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        .asSequence()
        .mapNotNull { resolveInfo -> resolveInfo.toLauncherAppEntry(packageManager) }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
        .toList()
}

private fun ResolveInfo.toLauncherAppEntry(
    packageManager: PackageManager,
): LauncherAppEntry? {
    val activityInfo = activityInfo ?: return null
    val label = loadLabel(packageManager)?.toString()?.takeIf { it.isNotBlank() }
        ?: activityInfo.applicationInfo.loadLabel(packageManager)?.toString()?.takeIf { it.isNotBlank() }
        ?: activityInfo.packageName
    return LauncherAppEntry(
        label = label,
        packageName = activityInfo.packageName,
        activityName = activityInfo.name,
        icon = loadIcon(packageManager),
    )
}
