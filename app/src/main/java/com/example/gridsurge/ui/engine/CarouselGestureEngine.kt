package com.example.gridsurge.ui.engine

object CarouselGestureEngine {
    private const val SWIPE_THRESHOLD_DP = 48f
    private const val VELOCITY_THRESHOLD_PX = 1000f

    /**
     * Evaluates whether a horizontal drag gesture constitutes a mode switch.
     */
    fun evaluateSwipe(
        dragDistancePx: Float,
        dragVelocityPx: Float,
        densityDpi: Float
    ): Int {
        val swipeThresholdPx = SWIPE_THRESHOLD_DP * (densityDpi / 160f)

        return when {
            dragVelocityPx > VELOCITY_THRESHOLD_PX || dragDistancePx > swipeThresholdPx -> -1 // Swipe Right -> Previous
            dragVelocityPx < -VELOCITY_THRESHOLD_PX || dragDistancePx < -swipeThresholdPx -> 1  // Swipe Left -> Next
            else -> 0 // Drag did not meet threshold; snap back
        }
    }
}
