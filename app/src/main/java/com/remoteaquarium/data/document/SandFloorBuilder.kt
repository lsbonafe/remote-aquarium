package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RemoteComposeContext

object SandFloorBuilder {

    private data class PebbleSpec(
        val xFraction: Float,
        val yOffset: Float,
        val radius: Float,
        val color: Int,
    )

    private data class ResolvedPebble(
        val x: Float,
        val y: Float,
        val radius: Float,
        val color: Int,
    )

    private val pebbles = listOf(
        PebbleSpec(xFraction = 0.08f, yOffset = 0.04f, radius = 5f, color = NeonPalette.CYAN),
        PebbleSpec(xFraction = 0.22f, yOffset = 0.04f, radius = 7f, color = NeonPalette.MAGENTA),
        PebbleSpec(xFraction = 0.35f, yOffset = 0.04f, radius = 4f, color = NeonPalette.GREEN),
        PebbleSpec(xFraction = 0.52f, yOffset = 0.04f, radius = 6f, color = NeonPalette.YELLOW),
        PebbleSpec(xFraction = 0.68f, yOffset = 0.04f, radius = 5f, color = NeonPalette.HOT_PINK),
        PebbleSpec(xFraction = 0.78f, yOffset = 0.04f, radius = 8f, color = NeonPalette.CYAN_DARK),
        PebbleSpec(xFraction = 0.92f, yOffset = 0.04f, radius = 4f, color = NeonPalette.PURPLE),
    )

    private val corals = listOf(
        PebbleSpec(xFraction = 0.15f, yOffset = 0.02f, radius = 10f, color = NeonPalette.CORAL_PINK),
        PebbleSpec(xFraction = 0.13f, yOffset = 0.01f, radius = 7f, color = NeonPalette.CORAL_PINK),
        PebbleSpec(xFraction = 0.75f, yOffset = 0.02f, radius = 9f, color = NeonPalette.NEON_TEAL),
        PebbleSpec(xFraction = 0.77f, yOffset = 0.01f, radius = 7f, color = NeonPalette.NEON_TEAL),
    )

    private fun PebbleSpec.resolve(w: Float, h: Float): ResolvedPebble {
        val sandTop = AquariumLayout.sandTop(h)
        return ResolvedPebble(
            x = w * xFraction,
            y = sandTop + h * yOffset,
            radius = radius,
            color = color,
        )
    }

    fun draw(ctx: RemoteComposeContext, w: Float, h: Float) {
        val sandTop = AquariumLayout.sandTop(h)
        val resolvedPebbles = pebbles.map { it.resolve(w, h) }
        val resolvedCorals = corals.map { it.resolve(w, h) }

        with(ctx) {
            rect(0f, sandTop, w, h, color = NeonPalette.DARK_PURPLE)

            for (p in resolvedPebbles) {
                circle(p.x, p.y, p.radius, color = p.color)
            }

            for (c in resolvedCorals) {
                circle(c.x, c.y, c.radius, color = c.color)
            }
        }
    }
}
