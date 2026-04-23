package me.qsx.jergal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.qsx.jergal.dualscreen.DualScreenCoordinator
import me.qsx.jergal.ui.LauncherTopScreen
import me.qsx.jergal.ui.theme.JergalTheme

/**
 * Hosts the top-screen showcase that reflects the current bottom-screen game selection.
 */
class TopScreenActivity : ComponentActivity() {
    /**
     * Registers the top screen, configures coordinated back handling, and renders the showcase UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DualScreenCoordinator.register(DualScreenCoordinator.TOP_SCREEN, this)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    DualScreenCoordinator.finishAll(this@TopScreenActivity)
                }
            }
        )
        setContent {
            JergalTheme(darkTheme = true, dynamicColor = false) {
                val selectedGame by DualScreenCoordinator.selectedGame.collectAsState()
                LauncherTopScreen(game = selectedGame)
            }
        }
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
}
