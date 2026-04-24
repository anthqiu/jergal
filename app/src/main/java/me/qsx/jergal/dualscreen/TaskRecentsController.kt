package me.qsx.jergal.dualscreen

import android.app.Activity
import android.app.ActivityManager

/**
 * Applies best-effort runtime recents exclusion to the current task.
 */
internal object TaskRecentsController {
    /**
     * Marks the activity task as excluded from Recents when it is visible to the app process.
     */
    fun excludeCurrentTaskFromRecents(activity: Activity) {
        val activityManager = activity.getSystemService(ActivityManager::class.java) ?: return
        activityManager.appTasks
            .firstOrNull { appTask -> appTask.taskInfo.taskId == activity.taskId }
            ?.setExcludeFromRecents(true)
    }
}
