package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.sin

object WaterLayerBuilder {

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        t: RFloat,
        accelX: RFloat,
    ) {
        with(ctx) {
            // Dark background
            writer.rcPaint.setColor(0xFF0A0A2E.toInt()).commit()
            drawRect(0f, 0f, w, h)

            // Cyan wave at top
            writer.rcPaint.setColor(0xFF00FFFF.toInt()).setStrokeWidth(3f).commit()
            val waveShift = (accelX * 30f).flush()
            for (i in 0 until 8) {
                val x1 = w * i / 8f
                val x2 = w * (i + 1) / 8f
                val wy1 = (rf(72f) + sin(t * 2f + rf(i * 1.5f) + waveShift) * 36f).flush()
                val wy2 = (rf(72f) + sin(t * 2f + rf((i + 1) * 1.5f) + waveShift) * 36f).flush()
                drawLine(x1, wy1.toFloat(), x2, wy2.toFloat())
            }
        }
    }
}
