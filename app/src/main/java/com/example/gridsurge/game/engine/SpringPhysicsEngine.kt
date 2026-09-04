package com.example.gridsurge.game.engine

import kotlin.math.*

/**
 * Analytical closed-form solution for an underdamped harmonic oscillator.
 * Guarantees smooth, frame-rate independent spring trajectories.
 */
class SpringPhysicsEngine(
    val naturalFrequency: Float = 28.0f, // omega_n: speed of the snap
    val dampingRatio: Float = 0.70f      // zeta: amount of overshoot (0.7-0.8 is "juicy")
) {
    private val gamma = dampingRatio * naturalFrequency
    private val omegaD = naturalFrequency * sqrt(1.0f - dampingRatio * dampingRatio)

    // Cached coefficients for the current animation
    private var coeffAx = 0f
    private var coeffAy = 0f
    private var coeffBx = 0f
    private var coeffBy = 0f
    
    private var targetX = 0f
    private var targetY = 0f
    private var elapsedTime = 0f
    
    var isSettled = true
        private set

    fun start(
        startX: Float, startY: Float,
        destX: Float, destY: Float,
        velX: Float = 0f, velY: Float = 0f
    ) {
        targetX = destX
        targetY = destY
        
        coeffAx = startX - destX
        coeffAy = startY - destY
        
        coeffBx = (velX + gamma * coeffAx) / omegaD
        coeffBy = (velY + gamma * coeffAy) / omegaD
        
        elapsedTime = 0f
        isSettled = false
    }

    /**
     * Updates and returns the new position as a Pair(x, y).
     */
    fun update(dt: Float): Pair<Float, Float> {
        if (isSettled) return Pair(targetX, targetY)

        elapsedTime += dt
        val t = elapsedTime
        
        val decay = exp(-gamma * t)
        val cosVal = cos(omegaD * t)
        val sinVal = sin(omegaD * t)

        val posX = targetX + decay * (coeffAx * cosVal + coeffBx * sinVal)
        val posY = targetY + decay * (coeffAy * cosVal + coeffBy * sinVal)

        // Velocity for settling check
        val velX = decay * ((coeffBx * omegaD - gamma * coeffAx) * cosVal - (coeffAx * omegaD + gamma * coeffBx) * sinVal)
        val velY = decay * ((coeffBy * omegaD - gamma * coeffAy) * cosVal - (coeffAy * omegaD + gamma * coeffBy) * sinVal)

        // Convergence check
        val distSq = (posX - targetX).pow(2) + (posY - targetY).pow(2)
        val speedSq = velX.pow(2) + velY.pow(2)

        if (distSq < 0.25f && speedSq < 1.0f) {
            isSettled = true
            return Pair(targetX, targetY)
        }

        return Pair(posX, posY)
    }
}
