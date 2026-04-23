package me.qsx.jergal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.qsx.jergal.dualscreen.DualScreenCoordinator
import me.qsx.jergal.dualscreen.RetroLibrary
import me.qsx.jergal.ui.LauncherBottomScreen
import me.qsx.jergal.ui.theme.JergalTheme

/**
 * Hosts the bottom-screen launcher experience, including the game library grid and app drawer.
 */
class BottomScreenActivity : ComponentActivity() {
    /**
     * Registers the bottom screen, configures coordinated back handling, and renders the library UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DualScreenCoordinator.register(DualScreenCoordinator.BOTTOM_SCREEN, this)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    DualScreenCoordinator.finishAll(this@BottomScreenActivity)
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
