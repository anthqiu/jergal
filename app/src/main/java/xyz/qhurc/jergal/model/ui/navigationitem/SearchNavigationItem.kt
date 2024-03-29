package xyz.qhurc.jergal.model.ui.navigationitem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

object SearchNavigationItem: NavigationItem() {
    override fun getName(): String {
        return "Search"
    }

    override fun getNormalIconImageVector(): ImageVector {
        return Icons.Outlined.Search
    }

    override fun getSelectedIconImageVector(): ImageVector {
        return Icons.Filled.Search
    }

    override fun onClick() {

    }
}