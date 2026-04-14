package com.remoteaquarium.presentation.physics

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos

/**
 * Determines which way a fish should face based on the current situation.
 *
 * Behavior rules:
 *  1. Chasing food    → face toward the food (full 360°)
 *  2. After eating    → settle to left or right, whichever is closer to the last heading
 *  3. Idle swimming   → gradually return to facing right (the default pose)
 */
object FacingDirection {

    private const val FACE_RIGHT = 0f
    private val FACE_LEFT = PI.toFloat()

    private const val CHASE_SPEED = 8f
    private const val SETTLE_SPEED = 3f
    private const val IDLE_SPEED = 3f
    private const val LEAN_DEADZONE = 0.1f

    fun update(
        fish: PhysicsObject,
        foodTarget: FoodManager.FoodParticle?,
        idleBlend: Float,
        dt: Float,
    ) {
        val (targetAngle, turnSpeed) = when {
            foodTarget != null -> faceTowardFood(fish, foodTarget)
            idleBlend > 0f    -> returnToDefault(idleBlend)
            else              -> settleToNearestSide(fish)
        }

        fish.currentAngle = lerpAngle(fish.currentAngle, targetAngle, (turnSpeed * dt).coerceAtMost(1f))
    }

    private fun faceTowardFood(fish: PhysicsObject, food: FoodManager.FoodParticle): Pair<Float, Float> {
        val angle = atan2(
            (food.obj.y - fish.y).toDouble(),
            (food.obj.x - fish.x).toDouble(),
        ).toFloat()
        return angle to CHASE_SPEED
    }

    private fun settleToNearestSide(fish: PhysicsObject): Pair<Float, Float> {
        val lean = cos(fish.currentAngle.toDouble()).toFloat()
        val target = when {
            lean > LEAN_DEADZONE  -> FACE_RIGHT
            lean < -LEAN_DEADZONE -> FACE_LEFT
            else                  -> if (fish.swimPhase.toInt() % 2 == 0) FACE_RIGHT else FACE_LEFT
        }
        return target to SETTLE_SPEED
    }

    private fun returnToDefault(idleBlend: Float): Pair<Float, Float> {
        return FACE_RIGHT to IDLE_SPEED * idleBlend
    }
}
