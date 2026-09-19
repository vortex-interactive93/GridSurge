package com.example.gridsurge.hub.model

import java.util.Random

class StarParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 1f, // Depth layer: 1.0 (distant, slow) to 3.0 (near, fast)
    var size: Float = 2f,
    var alpha: Float = 0.5f,
    var baseAlpha: Float = 0.5f
)

class SubspaceStarfieldState(val count: Int = 45) {
    val particles = Array(count) { StarParticle() }
    private var isInitialized = false

    fun initIfNeeded(width: Float, height: Float) {
        if (isInitialized || width <= 0f || height <= 0f) return
        val random = Random(1337L)
        particles.forEach { p ->
            p.x = random.nextFloat() * width
            p.y = random.nextFloat() * height
            p.z = 1f + random.nextFloat() * 2.2f
            p.size = (1.2f * p.z).coerceIn(1.5f, 4.5f)
            p.baseAlpha = (0.25f * p.z).coerceIn(0.2f, 0.75f)
            p.alpha = p.baseAlpha
        }
        isInitialized = true
    }

    fun update(width: Float, height: Float, velocityOffset: Float) {
        if (width <= 0f || height <= 0f) return
        particles.forEach { p ->
            // Warp parallax drift reacting to pager offset
            p.x -= velocityOffset * p.z * 1.8f

            // Screen boundary wrapping
            if (p.x < 0f) p.x += width
            if (p.x > width) p.x -= width
        }
    }
}
