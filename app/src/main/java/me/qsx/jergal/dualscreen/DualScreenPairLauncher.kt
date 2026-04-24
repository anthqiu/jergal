package me.qsx.jergal.dualscreen

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.Display
import me.qsx.jergal.BottomScreenActivity
import me.qsx.jergal.TopScreenActivity

/**
 * Launches and restores the paired dual-screen activities on their target displays.
 */
object DualScreenPairLauncher {
    /**
     * Launches or brings both screen activities to the foreground on their target displays.
     */
    fun launchPair(context: Context) {
        DualScreenSafetyNetManager.ensureSafetyNetActivities(context)
        val targets = resolveDisplayTargets(context)
        if (!DualScreenCoordinator.isScreenVisible(DualScreenCoordinator.TOP_SCREEN)) {
            launchTopHomeOnDisplay(context, targets.topDisplayId)
        }
        if (!DualScreenCoordinator.isScreenVisible(DualScreenCoordinator.BOTTOM_SCREEN)) {
            launchBottomHomeOnDisplay(context, targets.bottomDisplayId)
        }
    }

    /**
     * Restores the bottom-screen activity when the top screen returns to the foreground.
     */
    fun restoreBottomScreenFromTop(activity: Activity) {
        val targets = resolveDisplayTargets(activity)
        if (
            targets.bottomDisplayId == targets.topDisplayId ||
            DualScreenCoordinator.isScreenVisible(DualScreenCoordinator.BOTTOM_SCREEN)
        ) {
            return
        }
        launchBottomHomeOnDisplay(activity, targets.bottomDisplayId)
    }

    /**
     * Restores the top-screen activity when the bottom screen returns to the foreground on its own.
     */
    fun restoreTopScreenFromBottom(activity: Activity) {
        val targets = resolveDisplayTargets(activity)
        if (
            targets.bottomDisplayId == targets.topDisplayId ||
            DualScreenCoordinator.isScreenVisible(DualScreenCoordinator.TOP_SCREEN)
        ) {
            return
        }
        launchTopHomeOnDisplay(activity, targets.topDisplayId)
    }

    private fun resolveDisplayTargets(context: Context): DisplayTargets {
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

    private fun launchTopHomeOnDisplay(
        context: Context,
        displayId: Int,
    ) {
        launchOnDisplay(
            context = context,
            displayId = displayId,
            intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                setClass(context, TopScreenActivity::class.java)
            },
        )
    }

    private fun launchBottomHomeOnDisplay(
        context: Context,
        displayId: Int,
    ) {
        launchOnDisplay(
            context = context,
            displayId = displayId,
            intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_SECONDARY_HOME)
                setClass(context, BottomScreenActivity::class.java)
            },
        )
    }

    private fun launchOnDisplay(
        context: Context,
        displayId: Int,
        intent: Intent,
    ) {
        val options = ActivityOptions.makeBasic().apply {
            setLaunchDisplayId(displayId)
        }
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        )
        context.startActivity(intent, options.toBundle())
    }
}

private data class DisplayTargets(
    val topDisplayId: Int,
    val bottomDisplayId: Int,
)
