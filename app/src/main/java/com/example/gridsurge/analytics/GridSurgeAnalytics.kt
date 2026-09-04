package com.example.gridsurge.analytics

import android.os.Bundle
import android.util.Log

/**
 * GridSurge Analytics Hub.
 * Dispatches event telemetry to Firebase and local logs.
 */
object GridSurgeAnalytics {
    private const val TAG = "GridSurgeAnalytics"

    // Match Lifecycles
    fun logMatchStarted(mode: String, level: Int = 0) {
        val bundle = Bundle().apply {
            putString("match_mode", mode)
            putInt("level_index", level)
        }
        logFirebaseEvent("GS_match_started", bundle)
    }

    fun logMatchCompleted(mode: String, score: Long, result: String) {
        val bundle = Bundle().apply {
            putString("match_mode", mode)
            putLong("final_score", score)
            putString("result_status", result)
        }
        logFirebaseEvent("GS_match_completed", bundle)
    }

    fun logAdventureResult(level: Int, score: Long, stars: Int, success: Boolean) {
        val bundle = Bundle().apply {
            putInt("level_index", level)
            putLong("score", score)
            putInt("stars_earned", stars)
            putBoolean("is_success", success)
        }
        logFirebaseEvent("GS_adventure_level_result", bundle)
    }

    fun logClashResult(playerScore: Long, rivalScore: Long, isWinner: Boolean) {
        val bundle = Bundle().apply {
            putLong("player_score", playerScore)
            putLong("rival_score", rivalScore)
            putBoolean("is_player_winner", isWinner)
        }
        logFirebaseEvent("GS_clash_match_result", bundle)
    }

    fun logReviveAction(actionType: String, starsSpent: Int) {
        val bundle = Bundle().apply {
            putString("revive_type", actionType)
            putInt("cost_stars", starsSpent)
        }
        logFirebaseEvent("GS_matrix_revive_action", bundle)
    }

    fun logShareEvent(type: String) {
        val bundle = Bundle().apply {
            putString("card_type", type)
        }
        logFirebaseEvent("GS_match_card_shared", bundle)
    }

    private fun logFirebaseEvent(name: String, params: Bundle) {
        // Placeholder for actual FirebaseAnalytics.getInstance(context).logEvent(name, params)
        Log.d(TAG, "EVENT // $name: $params")
    }
}
