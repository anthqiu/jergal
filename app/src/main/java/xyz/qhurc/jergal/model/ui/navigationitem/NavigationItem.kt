package xyz.qhurc.jergal.model.ui.navigationitem

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

abstract class NavigationItem: Comparable<NavigationItem> {

    abstract fun getName(): String
    protected abstract fun getNormalIconImageVector(): ImageVector
    protected abstract fun getSelectedIconImageVector(): ImageVector
    @Composable
    open fun getNormalIcon() {
        Icon(getNormalIconImageVector(), "")
    }
    @Composable
    open fun getSelectedIcon() {
        Icon(getSelectedIconImageVector(), "")
    }
    abstract fun onClick()

    override fun compareTo(other: NavigationItem): Int {
        return getName() compareTo other.getName()
    }

    @Composable
    open fun createNavigationItem(navController: NavController) {

    }
}
