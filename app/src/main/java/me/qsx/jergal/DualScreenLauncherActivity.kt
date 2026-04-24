package me.qsx.jergal

import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import me.qsx.jergal.dualscreen.DualScreenCoordinator
import me.qsx.jergal.dualscreen.DualScreenPairLauncher
import me.qsx.jergal.dualscreen.TaskRecentsController

/**
 * Lightweight entry activity that launches the paired top-screen and bottom-screen activities.
 */
class DualScreenLauncherActivity : ComponentActivity() {
    private var hasLaunchedDualScreenPair = false
    private val requestHomeRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            launchDualScreenPairIfNeeded()
        }

    /**
     * Requests the Home role when needed, then starts both screen activities on their target displays.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TaskRecentsController.excludeCurrentTaskFromRecents(this)
        if (DualScreenCoordinator.isDualHomeVisible()) {
            finish()
            return
        }
        if (!requestHomeRoleIfNeeded()) {
            launchDualScreenPairIfNeeded()
        }
    }

    private fun requestHomeRoleIfNeeded(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false
        }
        val roleManager = getSystemService(RoleManager::class.java) ?: return false
        if (!roleManager.isRoleAvailable(RoleManager.ROLE_HOME) || roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
            return false
        }
        requestHomeRole.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
        return true
    }

    private fun launchDualScreenPairIfNeeded() {
        if (hasLaunchedDualScreenPair || isFinishing || isDestroyed) {
            return
        }
        hasLaunchedDualScreenPair = true
        DualScreenPairLauncher.launchPair(this)
        finish()
    }
}
