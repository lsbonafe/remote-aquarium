package com.remoteaquarium.presentation.physics

import kotlin.math.abs

class IdleDetector(
    private val idleThresholdSec: Float = 5f,
    private val blendRampSec: Float = 2f,
    private val tiltChangeThreshold: Float = 0.05f,
) {
    private var lastSignificantTiltTime = 0f
    private var previousTiltX = 0f
    private var previousTiltY = 0f

    fun update(tiltX: Float, tiltY: Float, elapsedTimeSec: Float): Float {
        if (abs(tiltX - previousTiltX) > tiltChangeThreshold ||
            abs(tiltY - previousTiltY) > tiltChangeThreshold
        ) {
            lastSignificantTiltTime = elapsedTimeSec
        }
        previousTiltX = tiltX
        previousTiltY = tiltY

        val isIdle = (elapsedTimeSec - lastSignificantTiltTime) > idleThresholdSec
        return if (isIdle) {
            ((elapsedTimeSec - lastSignificantTiltTime - idleThresholdSec) / blendRampSec).coerceAtMost(1f)
        } else {
            0f
        }
    }
}
