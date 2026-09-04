package com.example.gridsurge.features.adventure.core

import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

data class StageStarTargets(
    val threeStarSeconds: Int = 45,
    val twoStarSeconds: Int = 90
)

class AdventureGameController(
    private val viewScope: CoroutineScope,
    private val hudTimerTextView: TextView?,
    private val starTargets: StageStarTargets
) {
    private val matchTimer = AdventureMatchTimer(viewScope) { formattedTime, _ ->
        hudTimerTextView?.text = "TIME: $formattedTime"
    }

    fun onStageBriefingDismissed() {
        matchTimer.start()
    }

    fun onSystemPause() {
        matchTimer.pause()
    }

    fun onSystemResume() {
        matchTimer.resume()
    }

    /** Called when the final guardian core is destroyed. */
    fun onStageVictory(onShowVictoryDialog: (earnedStars: Int, elapsedSeconds: Long) -> Unit) {
        val finalSeconds = matchTimer.stop()
        val earnedStars = evaluateStarRating(finalSeconds, starTargets)
        
        onShowVictoryDialog(earnedStars, finalSeconds)
    }

    private fun evaluateStarRating(elapsedSeconds: Long, targets: StageStarTargets): Int {
        return when {
            elapsedSeconds <= targets.threeStarSeconds -> 3
            elapsedSeconds <= targets.twoStarSeconds -> 2
            else -> 1 // Passing baseline for clearing all cores
        }
    }
}
