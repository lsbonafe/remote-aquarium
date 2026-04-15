package com.remoteaquarium.presentation.physics

/**
 * Animates fish mouth open/close when eating food.
 *
 * Behavior rules:
 *  1. Just ate food → mouth snaps fully open
 *  2. Otherwise     → mouth gradually closes
 */
object MouthAnimation {

    private const val CLOSE_SPEED = 3.3f

    fun update(fish: List<PhysicsObject>, eatingFishIndices: Set<Int>, dt: Float) {
        for (i in fish.indices) {
            fish[i].mouthOpen = when {
                i in eatingFishIndices -> 1f
                else -> (fish[i].mouthOpen - CLOSE_SPEED * dt).coerceAtLeast(0f)
            }
        }
    }
}
