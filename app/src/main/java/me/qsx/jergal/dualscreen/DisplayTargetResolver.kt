package me.qsx.jergal.dualscreen

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display

internal data class DisplayTargets(
    val topDisplayId: Int,
    val bottomDisplayId: Int,
)

internal object DisplayTargetResolver {
    fun resolve(context: Context): DisplayTargets {
        val displayManager = context.getSystemService(DisplayManager::class.java)
        val displays = displayManager?.displays?.sortedBy { it.displayId }.orEmpty()
        val topDisplayId = displays.firstOrNull { it.displayId == Display.DEFAULT_DISPLAY }?.displayId
            ?: displays.firstOrNull()?.displayId
            ?: Display.DEFAULT_DISPLAY
        val bottomDisplayId = displays.firstOrNull { it.displayId != topDisplayId }?.displayId
            ?: topDisplayId
        return DisplayTargets(
            topDisplayId = topDisplayId,
            bottomDisplayId = bottomDisplayId,
        )
    }
}
