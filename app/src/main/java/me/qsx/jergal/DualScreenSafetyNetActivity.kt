package me.qsx.jergal

import android.os.Bundle
import androidx.activity.ComponentActivity
import me.qsx.jergal.dualscreen.DualScreenPairLauncher
import me.qsx.jergal.dualscreen.TaskRecentsController

/**
 * Invisible fallback activity that restores the dual-screen home pair if a display stack returns to it.
 */
class DualScreenSafetyNetActivity : ComponentActivity() {
    private var hasEnteredBackground = false
    private var hasSettledBehindLauncher = false

    /**
     * Creates the fallback activity without rendering foreground UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TaskRecentsController.excludeCurrentTaskFromRecents(this)
    }

    /**
     * Moves a newly created fallback task behind the launcher, then restores the paired home
     * activities if the fallback later returns to the foreground.
     */
    override fun onResume() {
        super.onResume()
        TaskRecentsController.excludeCurrentTaskFromRecents(this)
        if (!hasEnteredBackground) {
            if (!hasSettledBehindLauncher) {
                hasSettledBehindLauncher = true
                window.decorView.post {
                    if (!isFinishing && !isDestroyed) {
                        moveTaskToBack(true)
                    }
                }
            }
            return
        }
        DualScreenPairLauncher.launchPair(this)
    }

    /**
     * Arms the fallback once the real home activities cover this task for the first time.
     */
    override fun onStop() {
        hasEnteredBackground = true
        super.onStop()
    }
}
