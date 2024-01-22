package xyz.qhurc.jergal.model.ui.navigationitem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object SettingsNavigationItem: NavigationItem() {
    override fun getName(): String {
        return "Settings"
    }

    override fun getNormalIconImageVector(): ImageVector {
        return Icons.Outlined.Settings
    }

    override fun getSelectedIconImageVector(): ImageVector {
        return Icons.Filled.Settings
    }

    override fun onClick() {

    }
}