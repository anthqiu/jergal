package xyz.qhurc.jergal.model.ui.navigationitem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import xyz.qhurc.jergal.model.Platform

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

    override fun getNormalIcon(): ImageVector {
        return Icons.Outlined.Home
    }

    override fun getSelectedIcon(): ImageVector {
        return Icons.Filled.Home
    }

    override fun onClick() {

    }
}