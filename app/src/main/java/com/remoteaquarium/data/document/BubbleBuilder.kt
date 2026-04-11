package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext

object BubbleBuilder {

    private val radii = floatArrayOf(5f, 4f, 6f, 4f, 5f, 3f)
    private val colors = intArrayOf(
        0xFF00FFFF.toInt(), 0xFFFF00FF.toInt(), 0xFF00FFFF.toInt(),
        0xFFFFFF00.toInt(), 0xFFFF0080.toInt(), 0xFF00FF66.toInt(),
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
                val radius = radii[i]
                val color = colors[i]

                writer.rcPaint.setColor(color).commit()
                drawCircle(bx.toFloat(), by.toFloat(), radius)
            }
        }
    }
}
