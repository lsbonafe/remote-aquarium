package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.sin

object BubbleBuilder {

    private data class BubbleSpec(
        val baseX: Float,
        val baseY: Float,
        val radius: Float,
        val riseSpeed: Float,
        val wobbleSpeed: Float,
        val wobbleAmplitude: Float,
        val phase: Float,
        val color: Int,
    )

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        t: RFloat,
    ) {
        val bubbles = listOf(
            BubbleSpec(w * 0.20f, h * 0.85f, 5f, 23f, 2.0f, 8f, 0f, 0xFF00FFFF.toInt()),
            BubbleSpec(w * 0.35f, h * 0.90f, 4f, 17f, 1.5f, 6f, 1f, 0xFFFF00FF.toInt()),
            BubbleSpec(w * 0.50f, h * 0.88f, 6f, 20f, 1.8f, 10f, 2f, 0xFF00FFFF.toInt()),
            BubbleSpec(w * 0.65f, h * 0.92f, 4f, 26f, 2.2f, 7f, 3f, 0xFFFFFF00.toInt()),
            BubbleSpec(w * 0.80f, h * 0.86f, 5f, 14f, 1.6f, 9f, 4f, 0xFFFF0080.toInt()),
            BubbleSpec(w * 0.10f, h * 0.95f, 3f, 29f, 2.5f, 5f, 5f, 0xFF00FF66.toInt()),
        )

        with(ctx) {
            for (bubble in bubbles) {
                val wobble = (sin(t * bubble.wobbleSpeed + rf(bubble.phase)) * bubble.wobbleAmplitude).flush()
                val bx = (rf(bubble.baseX) + wobble).flush()
                val by = (rf(bubble.baseY) - t * bubble.riseSpeed).flush()

                writer.rcPaint.setColor(bubble.color).commit()
                drawCircle(bx.toFloat(), by.toFloat(), bubble.radius)
            }
        }
    }
}
