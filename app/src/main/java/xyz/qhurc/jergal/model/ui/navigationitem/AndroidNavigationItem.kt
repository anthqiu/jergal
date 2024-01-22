package xyz.qhurc.jergal.model.ui.navigationitem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.outlined.Android
import androidx.compose.ui.graphics.vector.ImageVector

object AndroidNavigationItem : NavigationItem() {
    override fun getName(): String {
        return "Apps"
    }

    override fun getNormalIconImageVector(): ImageVector {
        return Icons.Outlined.Android
    }

    override fun getSelectedIconImageVector(): ImageVector {
        return Icons.Filled.Android
    }

    override fun onClick() {

    }
}