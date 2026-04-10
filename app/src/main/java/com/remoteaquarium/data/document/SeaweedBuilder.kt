package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.sin

object SeaweedBuilder {

    private data class SeaweedStalk(
        val baseX: Float,
        val baseY: Float,
        val topY: Float,
        val swaySpeed: Float,
        val swayPhase: Float,
        val color: Int,
    )

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        t: RFloat,
        accelX: RFloat,
    ) {
        val sandTop = h * 0.82f
        val stalks = listOf(
            SeaweedStalk(w * 0.06f, sandTop, sandTop - h * 0.18f, 1.2f, 0f, 0xFF00FF66.toInt()),
            SeaweedStalk(w * 0.12f, sandTop, sandTop - h * 0.22f, 1.5f, 2f, 0xFF00FFCC.toInt()),
            SeaweedStalk(w * 0.28f, sandTop, sandTop - h * 0.15f, 1.8f, 4f, 0xFF00FF66.toInt()),
            SeaweedStalk(w * 0.42f, sandTop, sandTop - h * 0.20f, 1.0f, 1f, 0xFF33FF99.toInt()),
            SeaweedStalk(w * 0.58f, sandTop, sandTop - h * 0.12f, 2.0f, 3f, 0xFF00FFCC.toInt()),
            SeaweedStalk(w * 0.72f, sandTop, sandTop - h * 0.25f, 1.3f, 5f, 0xFF00FF66.toInt()),
            SeaweedStalk(w * 0.85f, sandTop, sandTop - h * 0.17f, 1.6f, 2.5f, 0xFF33FF99.toInt()),
            SeaweedStalk(w * 0.93f, sandTop, sandTop - h * 0.20f, 1.1f, 4.5f, 0xFF00FFCC.toInt()),
        )

        with(ctx) {
            for (stalk in stalks) {
                val sway = (sin(t * stalk.swaySpeed + rf(stalk.swayPhase)) * 15f + accelX * 12f).flush()
                val tipX = (rf(stalk.baseX) + sway).flush()

                writer.rcPaint.setColor(stalk.color).setStrokeWidth(4f).commit()
                drawLine(stalk.baseX, stalk.baseY, tipX.toFloat(), stalk.topY)

                writer.rcPaint.setColor(0xFF66FF99.toInt()).commit()
                drawCircle(tipX.toFloat(), stalk.topY, 5f)
            }
        }
    }
}
