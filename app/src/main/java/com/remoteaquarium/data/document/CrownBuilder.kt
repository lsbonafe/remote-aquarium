package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext

/**
 * Draws a golden crown on the winning fish.
 *
 * Crown structure:
 *  - Band: wide flat oval at the crown center
 *  - 3 jewel points: circles above the band (left, center, right)
 *  - Center gem: small colored circle on the center point
 *
 * Hidden at (-100, -100) when inactive. Rotation follows the fish
 * (or spins independently during the drop animation).
 */
object CrownBuilder {

    private const val BAND_WIDTH = 25f
    private const val BAND_HEIGHT = 7f
    private const val POINT_RADIUS = 5f
    private const val CENTER_POINT_RADIUS = 6f
    private const val GEM_RADIUS = 3f
    private const val POINT_OFFSET_Y = -12f
    private const val CENTER_POINT_OFFSET_Y = -16f
    private const val SIDE_POINT_OFFSET_X = 10f

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        crownX: RFloat,
        crownY: RFloat,
        crownCos: RFloat,
        crownSin: RFloat,
        crownScale: RFloat,
    ) {
        with(ctx) {
            drawBand(crownX, crownY, crownCos, crownSin, crownScale)
            drawJewels(crownX, crownY, crownCos, crownSin, crownScale)
        }
    }

    private fun RemoteComposeContext.drawBand(
        cx: RFloat, cy: RFloat, cosA: RFloat, sinA: RFloat, scale: RFloat,
    ) {
        val bw = (scale * BAND_WIDTH).flush()
        val bh = (scale * BAND_HEIGHT).flush()
        oval(cx - bw, cy - bh, cx + bw, cy + bh, color = NeonPalette.YELLOW)
    }

    private fun RemoteComposeContext.drawJewels(
        cx: RFloat, cy: RFloat, cosA: RFloat, sinA: RFloat, scale: RFloat,
    ) {
        // Left point
        drawPoint(cx, cy, cosA, sinA, scale, -SIDE_POINT_OFFSET_X, POINT_OFFSET_Y, POINT_RADIUS, NeonPalette.AMBER)
        // Center point
        drawPoint(cx, cy, cosA, sinA, scale, 0f, CENTER_POINT_OFFSET_Y, CENTER_POINT_RADIUS, NeonPalette.YELLOW)
        // Right point
        drawPoint(cx, cy, cosA, sinA, scale, SIDE_POINT_OFFSET_X, POINT_OFFSET_Y, POINT_RADIUS, NeonPalette.AMBER)
        // Center gem
        drawPoint(cx, cy, cosA, sinA, scale, 0f, CENTER_POINT_OFFSET_Y, GEM_RADIUS, NeonPalette.HOT_PINK)
    }

    private fun RemoteComposeContext.drawPoint(
        cx: RFloat, cy: RFloat, cosA: RFloat, sinA: RFloat, scale: RFloat,
        offsetX: Float, offsetY: Float, radius: Float, color: Int,
    ) {
        val sox = (scale * offsetX).flush()
        val soy = (scale * offsetY).flush()
        val px = (cx + cosA * sox - sinA * soy).flush()
        val py = (cy + sinA * sox + cosA * soy).flush()
        val r = (scale * radius).flush()
        circle(px, py, r, color = color)
    }
}
