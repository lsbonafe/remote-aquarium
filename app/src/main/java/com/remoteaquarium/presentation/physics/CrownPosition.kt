package com.remoteaquarium.presentation.physics

import kotlin.math.cos
import kotlin.math.sin

/**
 * Computes the crown's position, rotation, and scale each frame.
 *
 * Behavior rules:
 *  1. CROWN state  → crown sits on the winner's head, follows its position and angle
 *  2. RESET state  → crown detaches and falls with rotation (via ResetAnimation offsets)
 *  3. Otherwise    → crown is hidden off-screen
 */
object CrownPosition {

    private const val HEAD_OFFSET_FACTOR = 40f
    private const val HIDDEN = -100f

    data class State(
        val x: Float,
        val y: Float,
        val cos: Float,
        val sin: Float,
        val scale: Float,
    )

    fun calculate(
        cycleState: CycleState,
        predatorIndex: Int,
        fishObjects: List<PhysicsObject>,
        fishScale: FloatArray,
        resetAnimation: ResetAnimation,
        elapsedTimeSec: Float,
    ): State = when {
        predatorIndex < 0               -> hidden()
        cycleState == CycleState.CROWN  -> onHead(predatorIndex, fishObjects, fishScale)
        cycleState == CycleState.RESET  -> dropping(predatorIndex, fishObjects, fishScale, resetAnimation, elapsedTimeSec)
        else                            -> hidden()
    }

    private fun onHead(predatorIndex: Int, fishObjects: List<PhysicsObject>, fishScale: FloatArray): State {
        val fish = fishObjects[predatorIndex]
        val headOffset = fishScale[predatorIndex] * HEAD_OFFSET_FACTOR
        return State(
            x = fish.x,
            y = fish.y - headOffset,
            cos = cos(fish.currentAngle),
            sin = sin(fish.currentAngle),
            scale = fishScale[predatorIndex],
        )
    }

    private fun dropping(
        predatorIndex: Int,
        fishObjects: List<PhysicsObject>,
        fishScale: FloatArray,
        resetAnimation: ResetAnimation,
        elapsedTimeSec: Float,
    ): State {
        val fish = fishObjects[predatorIndex]
        val drop = resetAnimation.crownState(elapsedTimeSec)
        val headOffset = fishScale[predatorIndex] * HEAD_OFFSET_FACTOR
        return State(
            x = fish.x,
            y = fish.y - headOffset + drop.dropOffsetY,
            cos = drop.rotationCos,
            sin = drop.rotationSin,
            scale = fishScale[predatorIndex],
        )
    }

    private fun hidden(): State = State(HIDDEN, HIDDEN, 1f, 0f, 0f)
}
