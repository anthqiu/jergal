package xyz.qhurc.jergal.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import xyz.qhurc.jergal.model.AppInfo
import xyz.qhurc.jergal.model.JC
import xyz.qhurc.jergal.ui.items.createAndroidAppGridItem
import xyz.qhurc.jergal.util.JergalLog

const val TAG = "AndroidAppsScreen"

@Composable
fun createAndroidAppsScreen() {
    val apps = getApps(LocalContext.current.packageManager)

    val focusedItem = remember { mutableStateOf(-1) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(JC.ANDROID_APP_TILE_WIDTH),
        content = {
            items(apps) { app ->
                createAndroidAppGridItem(app = app)
            }
        }
    )
}

fun getApps(packageManager: PackageManager): List<AppInfo> {
    JergalLog.verbose(TAG, "Calling getLaunchableApps")
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val apps = packageManager.queryIntentActivities(intent, 0)
    return apps.map { resolveInfoToAppInfo(it, packageManager) }.sorted()
}

private fun resolveInfoToAppInfo(
    resolveInfo: ResolveInfo, packageManager: PackageManager
): AppInfo {
    val label = resolveInfo.loadLabel(packageManager).toString()
    val packageName = resolveInfo.activityInfo.packageName
    val icon = resolveInfo.loadIcon(packageManager).toBitmap().asImageBitmap()
    return AppInfo(label, packageName, icon)
}

