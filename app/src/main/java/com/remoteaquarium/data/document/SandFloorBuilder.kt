package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RemoteComposeContext

object SandFloorBuilder {

    fun draw(ctx: RemoteComposeContext, w: Float, h: Float) {
        with(ctx) {
            val sandTop = h * 0.82f

            // Dark purple sand
            writer.rcPaint.setColor(0xFF1A0A2E.toInt()).commit()
            drawRect(0f, sandTop, w, h)

            // Bright neon pebbles
            val pebbleX = floatArrayOf(0.08f, 0.22f, 0.35f, 0.52f, 0.68f, 0.78f, 0.92f)
            val pebbleR = floatArrayOf(5f, 7f, 4f, 6f, 5f, 8f, 4f)
            val pebbleColors = intArrayOf(
                0xFF00FFFF.toInt(), 0xFFFF00FF.toInt(), 0xFF00FF66.toInt(),
                0xFFFFFF00.toInt(), 0xFFFF0080.toInt(), 0xFF00CCFF.toInt(),
                0xFFCC00FF.toInt(),
            )
            for (i in pebbleX.indices) {
                writer.rcPaint.setColor(pebbleColors[i]).commit()
                drawCircle(w * pebbleX[i], sandTop + h * 0.04f, pebbleR[i])
            }

            // Neon coral
            writer.rcPaint.setColor(0xFFFF0066.toInt()).commit()
            drawCircle(w * 0.15f, sandTop + h * 0.02f, 10f)
            drawCircle(w * 0.13f, sandTop + h * 0.01f, 7f)

            writer.rcPaint.setColor(0xFF00FFCC.toInt()).commit()
            drawCircle(w * 0.75f, sandTop + h * 0.02f, 9f)
            drawCircle(w * 0.77f, sandTop + h * 0.01f, 7f)
        }
    }
}
