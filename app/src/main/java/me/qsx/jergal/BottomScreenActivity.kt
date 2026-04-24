package me.qsx.jergal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.qsx.jergal.dualscreen.DualScreenCoordinator
import me.qsx.jergal.dualscreen.DualScreenPairLauncher
import me.qsx.jergal.dualscreen.RetroLibrary
import me.qsx.jergal.dualscreen.TaskRecentsController
import me.qsx.jergal.ui.LauncherBottomScreen
import me.qsx.jergal.ui.theme.JergalTheme

/**
 * Hosts the bottom-screen launcher experience, including the game library grid and app drawer.
 */
class BottomScreenActivity : ComponentActivity() {
    private var hasCompletedInitialResume = false

    /**
     * Registers the bottom screen, configures coordinated back handling, and renders the library UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TaskRecentsController.excludeCurrentTaskFromRecents(this)
        DualScreenCoordinator.register(DualScreenCoordinator.BOTTOM_SCREEN, this)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // The launcher root should remain stable instead of closing.
                }
            }
        )
        setContent {
            JergalTheme(darkTheme = true, dynamicColor = false) {
                val selectedGame by DualScreenCoordinator.selectedGame.collectAsState()
                LauncherBottomScreen(
                    activity = this@BottomScreenActivity,
                    games = RetroLibrary.games,
                    selectedGame = selectedGame,
                    onSelect = DualScreenCoordinator::highlightGame,
                )
            }
        }
    }

    /**
     * Marks the bottom-screen task as visible to the dual-screen home router.
     */
    override fun onStart() {
        super.onStart()
        DualScreenCoordinator.markScreenStarted(DualScreenCoordinator.BOTTOM_SCREEN)
    }

    /**
     * Re-applies recents exclusion and restores the top screen if only the secondary home returned.
     */
    override fun onResume() {
        super.onResume()
        TaskRecentsController.excludeCurrentTaskFromRecents(this)
        if (!hasCompletedInitialResume) {
            hasCompletedInitialResume = true
            return
        }
        DualScreenPairLauncher.restoreTopScreenFromBottom(this)
    }

    /**
     * Marks the bottom-screen task as no longer visible to the dual-screen home router.
     */
    override fun onStop() {
        DualScreenCoordinator.markScreenStopped(DualScreenCoordinator.BOTTOM_SCREEN)
        super.onStop()
    }

    /**
     * Unregisters the bottom screen and mirrors process-local shutdown to the paired top screen.
     */
    override fun onDestroy() {
        val shouldMirrorExit = isFinishing && !isChangingConfigurations
        DualScreenCoordinator.unregister(DualScreenCoordinator.BOTTOM_SCREEN)
        if (shouldMirrorExit) {
            DualScreenCoordinator.finishAll(this)
        }
        super.onDestroy()
    }
}
