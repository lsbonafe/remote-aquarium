package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext

object BubbleBuilder {

    private val radii = floatArrayOf(5f, 4f, 6f, 4f, 5f, 3f)
    private val colors = intArrayOf(
        NeonPalette.CYAN, NeonPalette.MAGENTA, NeonPalette.CYAN,
        NeonPalette.YELLOW, NeonPalette.HOT_PINK, NeonPalette.GREEN,
    )

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        bubblePositions: List<Pair<RFloat, RFloat>>,
    ) {
        with(ctx) {
            for (i in bubblePositions.indices) {
                val (bx, by) = bubblePositions[i]
                circle(bx, by, radii[i], color = colors[i])
            }
        }
    }
}
