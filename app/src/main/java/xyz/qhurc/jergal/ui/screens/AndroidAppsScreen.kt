package xyz.qhurc.jergal.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.graphics.drawable.toBitmap
import xyz.qhurc.jergal.model.JergalConstants
import xyz.qhurc.jergal.util.JergalLog

const val TAG = "AndroidAppsScreen"

data class AppInfo(
    val label: String, val packageName: String, val icon: ImageBitmap
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun createAndroidAppsScreen() {
    val apps = getApps(LocalContext.current.packageManager)
    val selectedCardColors = cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    val nonSelectedCardColors = cardColors(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    )

    val focusedItem = remember { mutableStateOf(-1) }

    LazyVerticalGrid(columns = GridCells.Adaptive(JergalConstants.ANDROID_APP_TILE_WIDTH),
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars),
        content = {
//            item(span = { GridItemSpan(maxLineSpan) }) {
//                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
//            }
            itemsIndexed(apps) { idx, app ->
                val focused = remember { mutableStateOf(false) }
                val interactionSource = remember { MutableInteractionSource() }
                interactionSource.collectIsHoveredAsState()
                Card(
                    onClick = {},
                    colors = nonSelectedCardColors,
                    interactionSource = interactionSource
                ) {
                    Column(
                        modifier = Modifier
                            .size(
                                JergalConstants.ANDROID_APP_TILE_WIDTH,
                                JergalConstants.ANDROID_APP_TILE_HEIGHT
                            )
                            .align(CenterHorizontally)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(JergalConstants.ANDROID_APP_ICON_BOX_SIZE)
                                .align(CenterHorizontally)
                        )
                        {
                            Image(
                                bitmap = app.icon,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(JergalConstants.ANDROID_APP_ICON_SIZE)
                                    .align(Alignment.Center)
                            )
                        }
                        Text(
                            text = app.label,
                            modifier = Modifier.align(CenterHorizontally),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
//            item(span = { GridItemSpan(maxLineSpan) }) {
//                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
//            }
        }
    )
}

fun getApps(packageManager: PackageManager): List<AppInfo> {
    JergalLog.verbose(TAG, "Calling getLaunchableApps")
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val apps = packageManager.queryIntentActivities(intent, 0)
    return apps.map { resolveInfoToAppInfo(it, packageManager) }
}

private fun resolveInfoToAppInfo(
    resolveInfo: ResolveInfo, packageManager: PackageManager
): AppInfo {
    val label = resolveInfo.loadLabel(packageManager).toString()
    val packageName = resolveInfo.activityInfo.packageName
    val icon = resolveInfo.loadIcon(packageManager).toBitmap().asImageBitmap()
    return AppInfo(label, packageName, icon)
}

