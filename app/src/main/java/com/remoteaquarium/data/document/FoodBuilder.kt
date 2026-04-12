package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext

object FoodBuilder {

    private const val FOOD_RADIUS = 8f
    private const val GLOW_RADIUS = 14f

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        foodPositions: List<Pair<RFloat, RFloat>>,
    ) {
        with(ctx) {
            for (i in foodPositions.indices) {
                val (fx, fy) = foodPositions[i]
                // Glow
                circle(fx, fy, GLOW_RADIUS, color = NeonPalette.FOOD_GLOW)
                // Core
                circle(fx, fy, FOOD_RADIUS, color = NeonPalette.FOOD_BROWN)
            }
        }
    }
}
