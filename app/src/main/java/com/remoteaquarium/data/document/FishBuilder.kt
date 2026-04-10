package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.cos
import androidx.compose.remote.creation.sin

object FishBuilder {

    private data class FishSpec(
        val baseCx: Float,
        val baseCy: Float,
        val bodyWidth: Float,
        val bodyHeight: Float,
        val swimSpeedX: Float,
        val swimSpeedY: Float,
        val swimRangeX: Float,
        val swimRangeY: Float,
        val phase: Float,
        val bodyColor: Int,
        val finColor: Int,
    )

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        t: RFloat,
        accelX: RFloat,
        accelY: RFloat,
    ) {
        val fishes = listOf(
            FishSpec(w * 0.3f, h * 0.25f, 28f, 14f, 0.4f, 0.25f, w * 0.15f, h * 0.04f, 0f,
                0xFFFF0080.toInt(), 0xFFFF00FF.toInt()),
            FishSpec(w * 0.7f, h * 0.35f, 22f, 11f, 0.6f, 0.3f, w * 0.12f, h * 0.03f, 2f,
                0xFF00FFFF.toInt(), 0xFF00CCFF.toInt()),
            FishSpec(w * 0.5f, h * 0.55f, 32f, 16f, 0.3f, 0.2f, w * 0.18f, h * 0.05f, 4f,
                0xFFFFFF00.toInt(), 0xFFFFCC00.toInt()),
            FishSpec(w * 0.15f, h * 0.45f, 18f, 9f, 0.7f, 0.35f, w * 0.10f, h * 0.03f, 1f,
                0xFF00FF66.toInt(), 0xFF00CC44.toInt()),
            FishSpec(w * 0.85f, h * 0.60f, 24f, 12f, 0.5f, 0.28f, w * 0.14f, h * 0.04f, 3f,
                0xFFFF6600.toInt(), 0xFFFF3300.toInt()),
            FishSpec(w * 0.4f, h * 0.70f, 20f, 10f, 0.55f, 0.32f, w * 0.11f, h * 0.035f, 5f,
                0xFFCC00FF.toInt(), 0xFF9900FF.toInt()),
        )

        for (fish in fishes) {
            drawFish(ctx, t, accelX, accelY, fish)
        }
    }

    private fun drawFish(
        ctx: RemoteComposeContext,
        t: RFloat,
        accelX: RFloat,
        accelY: RFloat,
        fish: FishSpec,
    ) {
        with(ctx) {
            val cx = (rf(fish.baseCx) + sin(t * fish.swimSpeedX + rf(fish.phase)) * fish.swimRangeX + accelX * 40f).flush()
            val cy = (rf(fish.baseCy) + cos(t * fish.swimSpeedY + rf(fish.phase)) * fish.swimRangeY + accelY * 25f).flush()
            val bw = fish.bodyWidth
            val bh = fish.bodyHeight

            // Body
            writer.rcPaint.setColor(fish.bodyColor).commit()
            drawOval((cx - bw).toFloat(), (cy - bh).toFloat(), (cx + bw).toFloat(), (cy + bh).toFloat())

            // Tail
            writer.rcPaint.setColor(fish.finColor).commit()
            drawOval((cx - bw * 1.4f).toFloat(), (cy - bh * 0.8f).toFloat(), (cx - bw * 0.7f).toFloat(), (cy + bh * 0.8f).toFloat())

            // Eye
            writer.rcPaint.setColor(0xFFFFFFFF.toInt()).commit()
            drawCircle((cx + bw * 0.5f).toFloat(), (cy - bh * 0.3f).toFloat(), bw * 0.12f)
        }
    }
}
