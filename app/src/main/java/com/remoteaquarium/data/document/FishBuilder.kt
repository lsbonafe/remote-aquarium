package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext

object FishBuilder {

    private data class FishVisual(
        val bodyWidth: Float,
        val bodyHeight: Float,
        val bodyColor: Int,
        val finColor: Int,
    )

    private val visuals = listOf(
        // Large fish
        FishVisual(70f, 35f, 0xFFFF0080.toInt(), 0xFFFF00FF.toInt()),
        FishVisual(65f, 32f, 0xFFFFFF00.toInt(), 0xFFFFCC00.toInt()),
        FishVisual(60f, 30f, 0xFF00FFFF.toInt(), 0xFF00CCFF.toInt()),
        // Medium fish
        FishVisual(45f, 22f, 0xFF00FF66.toInt(), 0xFF00CC44.toInt()),
        FishVisual(50f, 25f, 0xFFFF6600.toInt(), 0xFFFF3300.toInt()),
        FishVisual(42f, 21f, 0xFFCC00FF.toInt(), 0xFF9900FF.toInt()),
        FishVisual(48f, 24f, 0xFFFF0080.toInt(), 0xFFCC0066.toInt()),
        FishVisual(44f, 22f, 0xFF00CCFF.toInt(), 0xFF0099CC.toInt()),
        FishVisual(46f, 23f, 0xFFFFCC00.toInt(), 0xFFCC9900.toInt()),
        // Small fish
        FishVisual(30f, 15f, 0xFF00FF99.toInt(), 0xFF00CC77.toInt()),
        FishVisual(25f, 12f, 0xFFFF3399.toInt(), 0xFFCC2277.toInt()),
        FishVisual(28f, 14f, 0xFF66FFFF.toInt(), 0xFF33CCCC.toInt()),
        FishVisual(24f, 12f, 0xFFFFFF66.toInt(), 0xFFCCCC33.toInt()),
        FishVisual(30f, 15f, 0xFFFF9900.toInt(), 0xFFCC7700.toInt()),
        FishVisual(26f, 13f, 0xFF9966FF.toInt(), 0xFF7744CC.toInt()),
        FishVisual(22f, 11f, 0xFF00FFCC.toInt(), 0xFF00CC99.toInt()),
        FishVisual(28f, 14f, 0xFFFF6699.toInt(), 0xFFCC4477.toInt()),
        FishVisual(24f, 12f, 0xFF66FF66.toInt(), 0xFF44CC44.toInt()),
    )

    fun draw(
        ctx: RemoteComposeContext,
        w: Float,
        h: Float,
        fishPositions: List<Pair<RFloat, RFloat>>,
    ) {
        for (i in fishPositions.indices) {
            val (fx, fy) = fishPositions[i]
            val visual = visuals[i]
            drawFish(ctx, fx, fy, visual)
        }
    }

    private fun drawFish(
        ctx: RemoteComposeContext,
        cx: RFloat,
        cy: RFloat,
        visual: FishVisual,
    ) {
        with(ctx) {
            val bw = visual.bodyWidth
            val bh = visual.bodyHeight

            // Body
            writer.rcPaint.setColor(visual.bodyColor).commit()
            drawOval(
                (cx - bw).toFloat(), (cy - bh).toFloat(),
                (cx + bw).toFloat(), (cy + bh).toFloat(),
            )

            // Tail
            writer.rcPaint.setColor(visual.finColor).commit()
            drawOval(
                (cx - bw * 1.4f).toFloat(), (cy - bh * 0.8f).toFloat(),
                (cx - bw * 0.7f).toFloat(), (cy + bh * 0.8f).toFloat(),
            )

            // Eye
            writer.rcPaint.setColor(0xFFFFFFFF.toInt()).commit()
            drawCircle((cx + bw * 0.5f).toFloat(), (cy - bh * 0.3f).toFloat(), bw * 0.12f)
            writer.rcPaint.setColor(0xFF000000.toInt()).commit()
            drawCircle((cx + bw * 0.55f).toFloat(), (cy - bh * 0.3f).toFloat(), bw * 0.06f)
        }
    }
}
