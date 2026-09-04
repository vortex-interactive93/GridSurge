package com.example.gridsurge.game.replay

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ReplayMove(
    val timestampMs: Long,
    val slotIndex: Int,
    val shapeId: Int,
    val targetRow: Int,
    val targetCol: Int,
    val occupiedOffsets: List<Pair<Int, Int>>,
    val colorInt: Int,
    val linesCleared: Int,
    val scoreAfterMove: Long,
    val comboStreak: Int
)

data class MatchReplayData(
    val matchId: String,
    val matchSeed: Long,
    val gameMode: String,
    val matchDurationSec: Int,
    val finalPlayerScore: Long,
    val finalRivalScore: Long,
    val isVictory: Boolean,
    val playerMoves: List<ReplayMove>,
    val rivalMoves: List<ReplayMove>
) {
    /**
     * Serializes telemetry into a compressed Base64 string for 1-tap deep links or clipboard sharing (~1.5 KB).
     */
    fun toReplayCode(): String {
        val root = JSONObject().apply {
            put("id", matchId)
            put("seed", matchSeed)
            put("mode", gameMode)
            put("dur", matchDurationSec)
            put("pScore", finalPlayerScore)
            put("rScore", finalRivalScore)
            put("win", isVictory)

            val pMovesArray = JSONArray()
            playerMoves.forEach { move ->
                pMovesArray.put(JSONArray().apply {
                    put(move.timestampMs)
                    put(move.slotIndex)
                    put(move.shapeId)
                    put(move.targetRow)
                    put(move.targetCol)
                    
                    val offsetsArr = JSONArray()
                    move.occupiedOffsets.forEach { (dr, dc) ->
                        offsetsArr.put(dr)
                        offsetsArr.put(dc)
                    }
                    put(offsetsArr)
                    
                    put(move.colorInt)
                    put(move.linesCleared)
                    put(move.scoreAfterMove)
                    put(move.comboStreak)
                })
            }
            put("pMoves", pMovesArray)

            val rMovesArray = JSONArray()
            rivalMoves.forEach { move ->
                rMovesArray.put(JSONArray().apply {
                    put(move.timestampMs)
                    put(move.slotIndex)
                    put(move.shapeId)
                    put(move.targetRow)
                    put(move.targetCol)

                    val offsetsArr = JSONArray()
                    move.occupiedOffsets.forEach { (dr, dc) ->
                        offsetsArr.put(dr)
                        offsetsArr.put(dc)
                    }
                    put(offsetsArr)

                    put(move.colorInt)
                    put(move.linesCleared)
                    put(move.scoreAfterMove)
                    put(move.comboStreak)
                })
            }
            put("rMoves", rMovesArray)
        }

        return Base64.encodeToString(root.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    companion object {
        fun fromReplayCode(code: String): MatchReplayData? {
            return try {
                val jsonStr = String(Base64.decode(code, Base64.DEFAULT), Charsets.UTF_8)
                val root = JSONObject(jsonStr)

                val pMovesList = mutableListOf<ReplayMove>()
                val pArray = root.getJSONArray("pMoves")
                for (i in 0 until pArray.length()) {
                    val m = pArray.getJSONArray(i)
                    
                    val offArr = m.getJSONArray(5)
                    val offsets = mutableListOf<Pair<Int, Int>>()
                    for (j in 0 until offArr.length() step 2) {
                        offsets.add(offArr.getInt(j) to offArr.getInt(j+1))
                    }

                    pMovesList.add(
                        ReplayMove(
                            timestampMs = m.getLong(0),
                            slotIndex = m.getInt(1),
                            shapeId = m.getInt(2),
                            targetRow = m.getInt(3),
                            targetCol = m.getInt(4),
                            occupiedOffsets = offsets,
                            colorInt = m.getInt(6),
                            linesCleared = m.getInt(7),
                            scoreAfterMove = m.getLong(8),
                            comboStreak = m.getInt(9)
                        )
                    )
                }

                val rMovesList = mutableListOf<ReplayMove>()
                val rArray = root.getJSONArray("rMoves")
                for (i in 0 until rArray.length()) {
                    val m = rArray.getJSONArray(i)

                    val offArr = m.getJSONArray(5)
                    val offsets = mutableListOf<Pair<Int, Int>>()
                    for (j in 0 until offArr.length() step 2) {
                        offsets.add(offArr.getInt(j) to offArr.getInt(j+1))
                    }

                    rMovesList.add(
                        ReplayMove(
                            timestampMs = m.getLong(0),
                            slotIndex = m.getInt(1),
                            shapeId = m.getInt(2),
                            targetRow = m.getInt(3),
                            targetCol = m.getInt(4),
                            occupiedOffsets = offsets,
                            colorInt = m.getInt(6),
                            linesCleared = m.getInt(7),
                            scoreAfterMove = m.getLong(8),
                            comboStreak = m.getInt(9)
                        )
                    )
                }

                MatchReplayData(
                    matchId = root.getString("id"),
                    matchSeed = root.getLong("seed"),
                    gameMode = root.getString("mode"),
                    matchDurationSec = root.getInt("dur"),
                    finalPlayerScore = root.getLong("pScore"),
                    finalRivalScore = root.getLong("rScore"),
                    isVictory = root.getBoolean("win"),
                    playerMoves = pMovesList,
                    rivalMoves = rMovesList
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

object MatchTelemetryRecorder {

    private var activeMatchId: String = ""
    private var activeMatchSeed: Long = 0L
    private var activeMode: String = "BLITZ_CLASH"
    private var matchStartTimeMs: Long = 0L
    private var isRecording: Boolean = false

    private val playerMoveBuffer = mutableListOf<ReplayMove>()
    private val rivalMoveBuffer = mutableListOf<ReplayMove>()

    /**
     * Initializes match recording buffer in RAM.
     */
    fun startSession(matchSeed: Long, gameMode: String = "BLITZ_CLASH") {
        reset()
        activeMatchId = UUID.randomUUID().toString().substring(0, 8).uppercase()
        activeMatchSeed = matchSeed
        activeMode = gameMode
        matchStartTimeMs = System.currentTimeMillis()
        isRecording = true
    }

    /**
     * Records a player polyomino placement.
     */
    fun logPlayerMove(
        slotIndex: Int,
        shapeId: Int,
        targetRow: Int,
        targetCol: Int,
        occupiedOffsets: List<Pair<Int, Int>>,
        colorInt: Int,
        linesCleared: Int,
        scoreAfterMove: Long,
        comboStreak: Int
    ) {
        if (!isRecording) return
        val offsetMs = System.currentTimeMillis() - matchStartTimeMs
        playerMoveBuffer.add(
            ReplayMove(
                timestampMs = offsetMs,
                slotIndex = slotIndex,
                shapeId = shapeId,
                targetRow = targetRow,
                targetCol = targetCol,
                occupiedOffsets = occupiedOffsets,
                colorInt = colorInt,
                linesCleared = linesCleared,
                scoreAfterMove = scoreAfterMove,
                comboStreak = comboStreak
            )
        )
    }

    /**
     * Records a simulated or live rival action.
     */
    fun logRivalMove(
        slotIndex: Int,
        shapeId: Int,
        targetRow: Int,
        targetCol: Int,
        occupiedOffsets: List<Pair<Int, Int>>,
        colorInt: Int,
        linesCleared: Int,
        scoreAfterMove: Long,
        comboStreak: Int
    ) {
        if (!isRecording) return
        val offsetMs = System.currentTimeMillis() - matchStartTimeMs
        rivalMoveBuffer.add(
            ReplayMove(
                timestampMs = offsetMs,
                slotIndex = slotIndex,
                shapeId = shapeId,
                targetRow = targetRow,
                targetCol = targetCol,
                occupiedOffsets = occupiedOffsets,
                colorInt = colorInt,
                linesCleared = linesCleared,
                scoreAfterMove = scoreAfterMove,
                comboStreak = comboStreak
            )
        )
    }

    /**
     * Finalizes match recording and generates a frozen MatchReplayData payload.
     */
    fun finishSession(
        finalPlayerScore: Long,
        finalRivalScore: Long,
        matchDurationSec: Int = 75
    ): MatchReplayData {
        isRecording = false
        val isVictory = finalPlayerScore >= finalRivalScore

        return MatchReplayData(
            matchId = activeMatchId,
            matchSeed = activeMatchSeed,
            gameMode = activeMode,
            matchDurationSec = matchDurationSec,
            finalPlayerScore = finalPlayerScore,
            finalRivalScore = finalRivalScore,
            isVictory = isVictory,
            playerMoves = playerMoveBuffer.toList(),
            rivalMoves = rivalMoveBuffer.toList()
        )
    }

    /**
     * Wipes memory buffer when match is aborted or dismissed without saving.
     */
    fun reset() {
        isRecording = false
        playerMoveBuffer.clear()
        rivalMoveBuffer.clear()
        matchStartTimeMs = 0L
        activeMatchSeed = 0L
    }
}
