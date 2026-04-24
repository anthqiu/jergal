package me.qsx.jergal

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.qsx.jergal.dualscreen.DualScreenCoordinator
import me.qsx.jergal.dualscreen.DualScreenPairLauncher
import me.qsx.jergal.dualscreen.RomLibraryRepository
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
        RomLibraryRepository.ensureInitialized(applicationContext)
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
                val libraryState by RomLibraryRepository.state.collectAsState()
                val selectedGame by DualScreenCoordinator.selectedGame.collectAsState()
                LauncherBottomScreen(
                    activity = this@BottomScreenActivity,
                    games = libraryState.games,
                    selectedGame = selectedGame,
                    libraryMessage = libraryState.libraryMessage,
                    syncMessage = libraryState.syncMessage,
                    onSelect = DualScreenCoordinator::highlightGame,
                    onLaunchGame = { game ->
                        RomLibraryRepository.launchGame(this@BottomScreenActivity, game)
                    },
                    onOpenSettings = ::openSettingsOnCurrentDisplay,
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

    private fun openSettingsOnCurrentDisplay() {
        val options = ActivityOptions.makeBasic().apply {
            setLaunchDisplayId(display?.displayId ?: Display.DEFAULT_DISPLAY)
        }
        val intent = Intent(this, SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent, options.toBundle())
    }
}
