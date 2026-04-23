package me.qsx.jergal.dualscreen

import android.app.Activity
import java.lang.ref.WeakReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Coordinates shared game selection and mirrored lifecycle shutdown across both screen activities.
 */
object DualScreenCoordinator {
    const val TOP_SCREEN = "top_screen"
    const val BOTTOM_SCREEN = "bottom_screen"

    private val trackedActivities = linkedMapOf<String, WeakReference<Activity>>()
    private var shutdownInProgress = false

    private val _selectedGame = MutableStateFlow(RetroLibrary.games.first())
    val selectedGame = _selectedGame.asStateFlow()

    /**
     * Updates the shared highlighted game that both screens observe.
     */
    fun highlightGame(game: RetroGame) {
        _selectedGame.value = game
    }

    /**
     * Registers an activity instance as one side of the coordinated dual-screen session.
     */
    @Synchronized
    fun register(key: String, activity: Activity) {
        trackedActivities[key] = WeakReference(activity)
        shutdownInProgress = false
    }

    /**
     * Removes an activity from the coordinated dual-screen session.
     */
    @Synchronized
    fun unregister(key: String) {
        trackedActivities.remove(key)
        if (trackedActivities.values.none { it.get() != null }) {
            shutdownInProgress = false
        }
    }

    /**
     * Finishes every tracked screen activity so both screens exit together.
     */
    @Synchronized
    fun finishAll(origin: Activity? = null) {
        if (shutdownInProgress) {
            return
        }
        shutdownInProgress = true
        trackedActivities.values
            .mapNotNull { it.get() }
            .forEach { activity ->
                if (activity !== origin && !activity.isFinishing) {
                    activity.finish()
                }
            }
        if (origin != null && !origin.isFinishing) {
            origin.finish()
        }
    }
}
