package me.qsx.jergal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import me.qsx.jergal.dualscreen.DualScreenCoordinator
import me.qsx.jergal.dualscreen.DualScreenPairLauncher
import me.qsx.jergal.dualscreen.RomLibraryRepository
import me.qsx.jergal.dualscreen.TaskRecentsController
import me.qsx.jergal.ui.LauncherTopScreen
import me.qsx.jergal.ui.theme.JergalTheme

/**
 * Hosts the top-screen showcase that reflects the current bottom-screen game selection.
 */
class TopScreenActivity : ComponentActivity() {
    private var hasCompletedInitialResume = false

    /**
     * Registers the top screen, configures coordinated back handling, and renders the showcase UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TaskRecentsController.excludeCurrentTaskFromRecents(this)
        RomLibraryRepository.ensureInitialized(applicationContext)
        DualScreenCoordinator.register(DualScreenCoordinator.TOP_SCREEN, this)
        enableEdgeToEdge()
        configureImmersiveTopScreen()
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // The top launcher surface should not close into another desktop.
                }
            }
        )
        setContent {
            JergalTheme(darkTheme = true, dynamicColor = false) {
                val libraryState by RomLibraryRepository.state.collectAsState()
                val selectedGame by DualScreenCoordinator.selectedGame.collectAsState()
                LauncherTopScreen(
                    game = selectedGame,
                    emptyStateText = libraryState.libraryMessage,
                    syncMessage = libraryState.syncMessage,
                )
            }
        }
    }

    /**
     * Marks the top-screen task as visible to the dual-screen home router.
     */
    override fun onStart() {
        super.onStart()
        DualScreenCoordinator.markScreenStarted(DualScreenCoordinator.TOP_SCREEN)
    }

    /**
     * Restores the bottom screen after the launcher returns to the foreground from another app.
     */
    override fun onResume() {
        super.onResume()
        TaskRecentsController.excludeCurrentTaskFromRecents(this)
        hideTopScreenSystemBars()
        if (!hasCompletedInitialResume) {
            hasCompletedInitialResume = true
            return
        }
        DualScreenPairLauncher.restoreBottomScreenFromTop(this)
    }

    /**
     * Reapplies immersive system-bar hiding when the top screen regains focus.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideTopScreenSystemBars()
        }
    }

    /**
     * Marks the top-screen task as no longer visible to the dual-screen home router.
     */
    override fun onStop() {
        DualScreenCoordinator.markScreenStopped(DualScreenCoordinator.TOP_SCREEN)
        super.onStop()
    }

    /**
     * Unregisters the top screen and mirrors process-local shutdown to the paired bottom screen.
     */
    override fun onDestroy() {
        val shouldMirrorExit = isFinishing && !isChangingConfigurations
        DualScreenCoordinator.unregister(DualScreenCoordinator.TOP_SCREEN)
        if (shouldMirrorExit) {
            DualScreenCoordinator.finishAll(this)
        }
        super.onDestroy()
    }

    private fun configureImmersiveTopScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideTopScreenSystemBars()
    }

    private fun hideTopScreenSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
