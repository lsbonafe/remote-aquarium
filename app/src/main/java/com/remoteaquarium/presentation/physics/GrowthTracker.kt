package com.remoteaquarium.presentation.physics

/**
 * Tracks fish size growth from eating food.
 *
 * Behavior rules:
 *  1. Just ate food → grow by 10% of original size
 *  2. Otherwise     → keep current scale
 */
object GrowthTracker {

    private const val GROWTH_PER_EAT = 0.1f

    fun update(scaleState: FloatArray, eatingFishIndices: Set<Int>) {
        for (i in eatingFishIndices) {
            scaleState[i] += GROWTH_PER_EAT
        }
    }
}
