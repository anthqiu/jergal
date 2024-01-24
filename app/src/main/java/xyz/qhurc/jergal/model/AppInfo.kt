package xyz.qhurc.jergal.model

import androidx.compose.ui.graphics.ImageBitmap

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap
) : Comparable<AppInfo> {
    override fun compareTo(other: AppInfo): Int {
        return when {
            this.label != other.label -> this.label compareTo other.label
            this.packageName != other.packageName -> this.packageName compareTo other.packageName
            else -> 0
        }
    }
}