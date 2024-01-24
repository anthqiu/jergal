package xyz.qhurc.jergal.model

import android.content.Context
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

typealias JC = JergalConstants

object JergalConstants {
    // requires init
    lateinit var DEFAULT_CARD_COLORS: CardColors
    lateinit var THEMED_CARD_COLORS: CardColors

    // config
    const val CONFIG_FILENAME = "config.json"

    // navigation bar
    val NAVIGATION_ITEM_ICON_SIZE = 24.dp
    val SCREEN_INSET = 24.dp

    // android apps
    val ANDROID_APP_TILE_WIDTH = 110.dp
    val ANDROID_APP_TILE_HEIGHT = 110.dp
    val ANDROID_APP_ICON_LABEL_INTERVAL = 8.dp
    val ANDROID_APP_ICON_SIZE = 48.dp

    // platforms
    val PLATFORM_ITEM_WIDTH = 300.dp
    val PLATFOTM_ITEM_ICON_WIDTH = 80.dp

    @Composable
    fun initComposableVal(context: Context) {
        DEFAULT_CARD_COLORS = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )

        THEMED_CARD_COLORS = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}