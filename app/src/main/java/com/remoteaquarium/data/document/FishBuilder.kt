package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext

object FishBuilder {

    data class FishVisual(
        val bodyWidth: Float,
        val bodyHeight: Float,
        val bodyColor: Int,
        val finColor: Int,
    )

    val visuals = listOf(
        // Large
        FishVisual(bodyWidth = 70f, bodyHeight = 35f, bodyColor = NeonPalette.HOT_PINK, finColor = NeonPalette.MAGENTA),
        FishVisual(bodyWidth = 65f, bodyHeight = 32f, bodyColor = NeonPalette.YELLOW, finColor = NeonPalette.YELLOW_DARK),
        FishVisual(bodyWidth = 60f, bodyHeight = 30f, bodyColor = NeonPalette.CYAN, finColor = NeonPalette.CYAN_DARK),
        // Medium
        FishVisual(bodyWidth = 45f, bodyHeight = 22f, bodyColor = NeonPalette.GREEN, finColor = NeonPalette.GREEN_DARK),
        FishVisual(bodyWidth = 50f, bodyHeight = 25f, bodyColor = NeonPalette.ORANGE, finColor = NeonPalette.ORANGE_DARK),
        FishVisual(bodyWidth = 42f, bodyHeight = 21f, bodyColor = NeonPalette.PURPLE, finColor = NeonPalette.PURPLE_DARK),
        FishVisual(bodyWidth = 48f, bodyHeight = 24f, bodyColor = NeonPalette.HOT_PINK, finColor = NeonPalette.MAGENTA_DARK),
        FishVisual(bodyWidth = 44f, bodyHeight = 22f, bodyColor = NeonPalette.CYAN_DARK, finColor = NeonPalette.CYAN_MID),
        FishVisual(bodyWidth = 46f, bodyHeight = 23f, bodyColor = NeonPalette.YELLOW_DARK, finColor = NeonPalette.YELLOW_MID),
        // Small
        FishVisual(bodyWidth = 30f, bodyHeight = 15f, bodyColor = NeonPalette.AQUA, finColor = NeonPalette.AQUA_DARK),
        FishVisual(bodyWidth = 25f, bodyHeight = 12f, bodyColor = NeonPalette.SALMON, finColor = NeonPalette.SALMON_DARK),
        FishVisual(bodyWidth = 28f, bodyHeight = 14f, bodyColor = NeonPalette.LIGHT_CYAN, finColor = NeonPalette.LIGHT_CYAN_DARK),
        FishVisual(bodyWidth = 24f, bodyHeight = 12f, bodyColor = NeonPalette.LIGHT_YELLOW, finColor = NeonPalette.LIGHT_YELLOW_DARK),
        FishVisual(bodyWidth = 30f, bodyHeight = 15f, bodyColor = NeonPalette.AMBER, finColor = NeonPalette.AMBER_DARK),
        FishVisual(bodyWidth = 26f, bodyHeight = 13f, bodyColor = NeonPalette.LAVENDER, finColor = NeonPalette.LAVENDER_DARK),
        FishVisual(bodyWidth = 22f, bodyHeight = 11f, bodyColor = NeonPalette.NEON_TEAL, finColor = NeonPalette.TEAL_DARK),
        FishVisual(bodyWidth = 28f, bodyHeight = 14f, bodyColor = NeonPalette.PEACH, finColor = NeonPalette.PEACH_DARK),
        FishVisual(bodyWidth = 24f, bodyHeight = 12f, bodyColor = NeonPalette.LIGHT_GREEN, finColor = NeonPalette.LIGHT_GREEN_DARK),
    )

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        fishPositions: List<Pair<RFloat, RFloat>>,
        fishAngles: List<Pair<RFloat, RFloat>>,
        fishMouthOpen: List<RFloat>,
    ) {
        with(ctx) {
            for (i in fishPositions.indices) {
                val (fx, fy) = fishPositions[i]
                val (cosA, sinA) = fishAngles[i]
                val v = visuals[i]
                rotatedFish(fx, fy, v.bodyWidth, v.bodyHeight, v.bodyColor, v.finColor, cosA, sinA, fishMouthOpen[i])
            }
        }
    }
}
