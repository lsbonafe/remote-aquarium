package com.remoteaquarium.presentation.physics

import kotlin.math.sqrt

/**
 * Controls the predator's hunting behavior — chasing and eating smaller fish.
 *
 * Behavior rules:
 *  1. Always target nearest huntable prey (re-evaluates every frame)
 *  2. Steer toward target with speed proportional to distance
 *  3. After eating, brief cooldown before next kill
 *  4. Skip fish already being swallowed
 */
object PredatorHunting {

    private const val CHASE_SPEED = 400f
    private const val MIN_SPEED = 200f
    private const val EAT_DISTANCE = 80f
    private const val STEER_FACTOR = 6f
    private const val EAT_COOLDOWN = 0.5f

    private var cooldownRemaining: Float = 0f

    fun updateCooldown(dt: Float) {
        if (cooldownRemaining > 0f) cooldownRemaining -= dt
    }

    fun findNearestPrey(
        predator: PhysicsObject,
        fishObjects: List<PhysicsObject>,
        stateMachine: PredatorCycleStateMachine,
        swallowTimers: FloatArray? = null,
    ): Int? {
        val huntable = stateMachine.preyIndices()
            .filter { swallowTimers == null || swallowTimers[it] < 0f }

        return huntable.minByOrNull { i ->
            val dx = fishObjects[i].x - predator.x
            val dy = fishObjects[i].y - predator.y
            dx * dx + dy * dy
        }
    }

    fun applyAttraction(predator: PhysicsObject, prey: PhysicsObject, dt: Float) {
        val dx = prey.x - predator.x
        val dy = prey.y - predator.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 1f) return

        val nx = dx / dist
        val ny = dy / dist

        val speed = (CHASE_SPEED + dist).coerceAtLeast(MIN_SPEED)
        val targetVx = nx * speed
        val targetVy = ny * speed

        val steerFactor = (STEER_FACTOR * dt).coerceAtMost(1f)
        predator.vx += (targetVx - predator.vx) * steerFactor
        predator.vy += (targetVy - predator.vy) * steerFactor
    }

    fun checkPreyEating(
        fishObjects: List<PhysicsObject>,
        stateMachine: PredatorCycleStateMachine,
        swallowTimers: FloatArray,
    ): Set<Int> {
        if (cooldownRemaining > 0f) return emptySet()
        val predIdx = stateMachine.predatorIndex
        if (predIdx < 0) return emptySet()

        val predator = fishObjects[predIdx]

        for (i in stateMachine.preyIndices()) {
            if (swallowTimers[i] >= 0f) continue
            val prey = fishObjects[i]
            val dx = prey.x - predator.x
            val dy = prey.y - predator.y
            val eatDist = predator.radius + EAT_DISTANCE
            if (dx * dx + dy * dy < eatDist * eatDist) {
                cooldownRemaining = EAT_COOLDOWN
                return setOf(i)
            }
        }

        return emptySet()
    }

    fun reset() {
        cooldownRemaining = 0f
    }
}
