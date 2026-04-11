package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.sin

object SeaweedBuilder {

    private const val SWAY_AMPLITUDE = 15f
    private const val TILT_SENSITIVITY = 12f
    private const val STALK_WIDTH = 4f
    private const val TIP_RADIUS = 5f

    private data class SeaweedSpec(
        val xFraction: Float,
        val heightFraction: Float,
        val swaySpeed: Float,
        val swayPhase: Float,
        val color: Int,
    )

    private data class ResolvedStalk(
        val baseX: Float,
        val baseY: Float,
        val topY: Float,
        val swaySpeed: Float,
        val swayPhase: Float,
        val color: Int,
    )

    private val specs = listOf(
        SeaweedSpec(xFraction = 0.06f, heightFraction = 0.18f, swaySpeed = 1.2f, swayPhase = 0f, color = NeonPalette.NEON_GREEN),
        SeaweedSpec(xFraction = 0.12f, heightFraction = 0.22f, swaySpeed = 1.5f, swayPhase = 2f, color = NeonPalette.NEON_TEAL),
        SeaweedSpec(xFraction = 0.28f, heightFraction = 0.15f, swaySpeed = 1.8f, swayPhase = 4f, color = NeonPalette.NEON_GREEN),
        SeaweedSpec(xFraction = 0.42f, heightFraction = 0.20f, swaySpeed = 1.0f, swayPhase = 1f, color = NeonPalette.MINT),
        SeaweedSpec(xFraction = 0.58f, heightFraction = 0.12f, swaySpeed = 2.0f, swayPhase = 3f, color = NeonPalette.NEON_TEAL),
        SeaweedSpec(xFraction = 0.72f, heightFraction = 0.25f, swaySpeed = 1.3f, swayPhase = 5f, color = NeonPalette.NEON_GREEN),
        SeaweedSpec(xFraction = 0.85f, heightFraction = 0.17f, swaySpeed = 1.6f, swayPhase = 2.5f, color = NeonPalette.MINT),
        SeaweedSpec(xFraction = 0.93f, heightFraction = 0.20f, swaySpeed = 1.1f, swayPhase = 4.5f, color = NeonPalette.NEON_TEAL),
    )

    private fun SeaweedSpec.resolve(w: Float, h: Float): ResolvedStalk {
        val sandTop = AquariumLayout.sandTop(h)
        return ResolvedStalk(
            baseX = w * xFraction,
            baseY = sandTop,
            topY = sandTop - h * heightFraction,
            swaySpeed = swaySpeed,
            swayPhase = swayPhase,
            color = color,
        )
    }

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        t: RFloat,
        accelX: RFloat,
    ) {
        val stalks = specs.map { it.resolve(w, h) }

        with(ctx) {
            for (stalk in stalks) {
                val sway = (sin(t * stalk.swaySpeed + rf(stalk.swayPhase)) * SWAY_AMPLITUDE + accelX * TILT_SENSITIVITY).flush()
                val tipX = (rf(stalk.baseX) + sway).flush()

                line(stalk.baseX, stalk.baseY, tipX, stalk.topY, color = stalk.color, strokeWidth = STALK_WIDTH)
                circle(tipX, rf(stalk.topY), TIP_RADIUS, color = NeonPalette.LEAF_TIP)
            }
        }
    }
}
