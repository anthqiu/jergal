package me.qsx.jergal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.qsx.jergal.dualscreen.RomLibraryRepository
import me.qsx.jergal.dualscreen.TaskRecentsController
import me.qsx.jergal.ui.LauncherSettingsScreen
import me.qsx.jergal.ui.theme.JergalTheme

/**
 * Hosts launcher settings for ROM library selection and platform subscription management.
 */
class SettingsActivity : ComponentActivity() {
    /**
     * Initializes the shared library repository and renders the settings UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TaskRecentsController.excludeCurrentTaskFromRecents(this)
        RomLibraryRepository.ensureInitialized(applicationContext)
        enableEdgeToEdge()
        setContent {
            JergalTheme(darkTheme = true, dynamicColor = false) {
                LauncherSettingsScreen(activity = this)
            }
        }
    }
}
