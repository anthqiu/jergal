package xyz.qhurc.jergal.model.ui.navigationitem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.filled.Apps
import androidx.compose.ui.graphics.vector.ImageVector

class AppsNavigationItem: NavigationItem() {
    override fun getName(): String {
        return "Apps"
    }

    override fun getNormalIcon(): ImageVector {
        return Icons.Outlined.Apps
    }

    override fun getSelectedIcon(): ImageVector {
        return Icons.Filled.Apps
    }

    override fun onClick() {

    }
}