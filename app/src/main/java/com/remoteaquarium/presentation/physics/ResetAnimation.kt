package com.remoteaquarium.presentation.physics

import kotlin.math.cos
import kotlin.math.sin

/**
 * Manages the reset animation after the crown display period.
 *
 * Behavior rules:
 *  1. Crown drops off — falls downward with slow rotation (1.5s)
 *  2. Predator shrinks back to scale 1.0 gradually (2s lerp)
 *  3. Dead fish respawn from random screen edges at scale 1.0 (staggered)
 *  4. Complete when all animations done (~3s)
 */
class ResetAnimation {

    data class CrownDropState(
        val dropOffsetY: Float,
        val rotationCos: Float,
        val rotationSin: Float,
    )

    var respawnPositions: Map<Int, Pair<Float, Float>> = emptyMap()
        private set

    private var startTime = 0f
    private var predatorIdx = -1
    private var originalPredatorScale = 1f
    private var worldW = 0f
    private var worldH = 0f

    companion object {
        private const val CROWN_DROP_DURATION = 1.5f
        private const val CROWN_DROP_GRAVITY = 400f
        private const val CROWN_ROTATION_SPEED = 3f
        private const val SHRINK_DURATION = 2f
        private const val RESPAWN_STAGGER = 0.1f
        private const val TOTAL_DURATION = 3f
    }

    fun clear() {
        respawnPositions = emptyMap()
    }

    fun start(
        startTime: Float,
        predatorIndex: Int,
        predatorScale: Float,
        deadFishIndices: List<Int>,
        worldWidth: Float,
        worldHeight: Float,
    ) {
        this.startTime = startTime
        this.predatorIdx = predatorIndex
        this.originalPredatorScale = predatorScale
        this.worldW = worldWidth
        this.worldH = worldHeight
        this.respawnPositions = deadFishIndices.associateWith { randomEdgePosition(worldWidth, worldHeight) }
    }

    fun crownState(elapsedTime: Float): CrownDropState {
        val t = (elapsedTime - startTime).coerceAtLeast(0f)
        val dropY = 0.5f * CROWN_DROP_GRAVITY * t * t
        val angle = t * CROWN_ROTATION_SPEED
        return CrownDropState(
            dropOffsetY = dropY,
            rotationCos = cos(angle),
            rotationSin = sin(angle),
        )
    }

    fun predatorScale(elapsedTime: Float): Float {
        val t = (elapsedTime - startTime).coerceAtLeast(0f)
        val progress = (t / SHRINK_DURATION).coerceAtMost(1f)
        return originalPredatorScale + (1f - originalPredatorScale) * progress
    }

    fun fishRespawnTime(fishIndex: Int): Float {
        val order = respawnPositions.keys.sorted().indexOf(fishIndex)
        return if (order >= 0) startTime + 0.5f + order * RESPAWN_STAGGER else Float.MAX_VALUE
    }

    fun isComplete(elapsedTime: Float): Boolean {
        return elapsedTime - startTime >= TOTAL_DURATION
    }

    private fun randomEdgePosition(w: Float, h: Float): Pair<Float, Float> {
        return when ((0..3).random()) {
            0 -> -50f to (Math.random() * h).toFloat()       // left
            1 -> (w + 50f) to (Math.random() * h).toFloat()  // right
            2 -> (Math.random() * w).toFloat() to -50f       // top
            else -> (Math.random() * w).toFloat() to (h + 50f) // bottom
        }
    }
}
