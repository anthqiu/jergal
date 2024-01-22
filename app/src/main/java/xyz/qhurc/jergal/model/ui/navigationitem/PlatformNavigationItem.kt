package xyz.qhurc.jergal.model.ui.navigationitem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.qhurc.jergal.model.platform.Platform

class PlatformNavigationItem private constructor(): NavigationItem() {
    lateinit var platform: Platform

    constructor(name: String): this() {
        platform = Platform(name)
    }

    constructor(platform: Platform): this() {
        this.platform = platform
    }

    override fun getName(): String {
        return platform.name
    }

    fun getVendor(): String {
        return platform.vendor
    }

    override fun getNormalIconImageVector(): ImageVector {
        return Icons.Outlined.Home
    }

    override fun getSelectedIconImageVector(): ImageVector {
        return Icons.Filled.VideogameAsset
    }

    @Composable
    override fun getNormalIcon() {
        return createIcon(text = platform.shortName)
    }

    @Composable
    fun createIcon(text: String) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(24.dp)
        ) {
            Text(
                text.uppercase(),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black
                )
            )
        }
    }

    override fun onClick() {

    }

    override fun compareTo(other: NavigationItem): Int {
        return if (other is PlatformNavigationItem)
            this.platform compareTo other.platform
        else
            super.compareTo(other)
    }
}