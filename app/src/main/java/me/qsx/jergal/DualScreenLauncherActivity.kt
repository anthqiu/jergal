package me.qsx.jergal

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.view.Display

/**
 * Lightweight entry activity that launches the paired top-screen and bottom-screen activities.
 */
class DualScreenLauncherActivity : ComponentActivity() {
    /**
     * Starts both screen activities on their target displays and immediately finishes the launcher.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchDualScreenPair()
        finish()
    }

    private fun launchDualScreenPair() {
        val displayManager = getSystemService(DisplayManager::class.java)
        val displays = displayManager?.displays?.sortedBy { it.displayId }.orEmpty()
        val topDisplayId = displays.firstOrNull { it.displayId == Display.DEFAULT_DISPLAY }?.displayId
            ?: displays.firstOrNull()?.displayId
            ?: Display.DEFAULT_DISPLAY
        val bottomDisplayId = displays.firstOrNull { it.displayId != topDisplayId }?.displayId
            ?: topDisplayId

        launchOnDisplay(TopScreenActivity::class.java, topDisplayId)
        launchOnDisplay(BottomScreenActivity::class.java, bottomDisplayId)
    }

    private fun launchOnDisplay(target: Class<out Activity>, displayId: Int) {
        val options = ActivityOptions.makeBasic().apply {
            setLaunchDisplayId(displayId)
        }
        val intent = Intent(this, target).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent, options.toBundle())
    }
}
