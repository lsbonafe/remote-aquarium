package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.sin

object WaterLayerBuilder {

    private const val WAVE_SEGMENTS = 8
    private const val WAVE_BASE_Y = 72f
    private const val WAVE_AMPLITUDE = 36f
    private const val WAVE_SPEED = 2f
    private const val WAVE_PHASE_STEP = 1.5f
    private const val TILT_SENSITIVITY = 30f

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        t: RFloat,
        accelX: RFloat,
    ) {
        with(ctx) {
            rect(0f, 0f, w, h, color = NeonPalette.DEEP_NAVY)

            val waveShift = (accelX * TILT_SENSITIVITY).flush()
            val segmentWidth = w / WAVE_SEGMENTS

            for (i in 0 until WAVE_SEGMENTS) {
                val x1 = segmentWidth * i
                val x2 = segmentWidth * (i + 1)
                val phase1 = i * WAVE_PHASE_STEP
                val phase2 = (i + 1) * WAVE_PHASE_STEP
                val wy1 = (rf(WAVE_BASE_Y) + sin(t * WAVE_SPEED + rf(phase1) + waveShift) * WAVE_AMPLITUDE).flush()
                val wy2 = (rf(WAVE_BASE_Y) + sin(t * WAVE_SPEED + rf(phase2) + waveShift) * WAVE_AMPLITUDE).flush()
                line(x1, wy1, x2, wy2, color = NeonPalette.CYAN, strokeWidth = 3f)
            }
        }
    }
}
