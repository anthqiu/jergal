package xyz.qhurc.jergal.model.ui.navigationitem

import androidx.compose.ui.graphics.vector.ImageVector

abstract class NavigationItem: Comparable<NavigationItem> {

    abstract fun getName(): String
    abstract fun getNormalIcon(): ImageVector
    abstract fun getSelectedIcon(): ImageVector
    abstract fun onClick(): Unit

    override fun compareTo(other: NavigationItem): Int {
        return getName() compareTo other.getName()
    }
}
