package me.qsx.jergal.dualscreen

import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.Uri
import android.view.Display
import me.qsx.jergal.DualScreenSafetyNetActivity

/**
 * Maintains one fallback task per display so the dual-screen home flow can be restored if a stack empties.
 */
internal object DualScreenSafetyNetManager {
    /**
     * Ensures that each active display has a dedicated fallback activity task.
     */
    fun ensureSafetyNetActivities(context: Context) {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val targetDisplayIds = resolveTargetDisplayIds(context)
        for (displayId in targetDisplayIds) {
            if (hasSafetyNetTask(activityManager, displayId)) {
                continue
            }
            val intent = Intent(context, DualScreenSafetyNetActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                data = safetyNetUriForDisplay(displayId)
            }
            val options = ActivityOptions.makeBasic().apply {
                setLaunchDisplayId(displayId)
            }
            context.startActivity(intent, options.toBundle())
        }
    }

    private fun resolveTargetDisplayIds(context: Context): Set<Int> {
        val displayManager = context.getSystemService(DisplayManager::class.java)
        val activeDisplayIds = displayManager?.displays
            ?.mapTo(linkedSetOf()) { it.displayId }
            .orEmpty()
        return linkedSetOf<Int>().apply {
            add(Display.DEFAULT_DISPLAY)
            addAll(activeDisplayIds)
        }
    }

    private fun hasSafetyNetTask(
        activityManager: ActivityManager?,
        displayId: Int,
    ): Boolean {
        val targetUri = safetyNetUriForDisplay(displayId)
        return activityManager?.appTasks.orEmpty()
            .mapNotNull { appTask -> appTask.taskInfo.baseIntent?.data }
            .any { uri -> uri == targetUri }
    }

    private fun safetyNetUriForDisplay(displayId: Int): Uri {
        return Uri.parse("jergal://safetynet/$displayId")
    }
}
