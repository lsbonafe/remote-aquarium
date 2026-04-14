package com.remoteaquarium.presentation.physics

import kotlin.math.cos
import kotlin.math.sin

/**
 * Applies movement forces to a fish based on the current situation.
 *
 * Behavior rules:
 *  1. Chasing food    → attract toward food + dampened tilt response
 *  2. Idle swimming   → reduced tilt + sinusoidal wander
 *  3. Normal          → full tilt response from accelerometer
 *
 * After forces are applied, drag and position integration always run.
 */
object FishMotion {

    private const val IDLE_SWIM_Y_PHASE_SCALE = 1.3f
    private const val IDLE_SWIM_Y_FORCE_SCALE = 0.6f

    fun applyForces(
        fish: PhysicsObject,
        tiltX: Float,
        tiltY: Float,
        dt: Float,
        idleBlend: Float,
        elapsedTime: Float,
        foodTarget: FoodManager.FoodParticle?,
        foodManager: FoodManager,
    ) {
        when {
            foodTarget != null -> chaseFood(fish, tiltX, tiltY, dt, foodTarget, foodManager)
            idleBlend > 0f     -> idleSwim(fish, tiltX, tiltY, dt, idleBlend, elapsedTime)
            else               -> followTilt(fish, tiltX, tiltY, dt)
        }

        applyDragAndIntegrate(fish, dt)
    }

    private fun chaseFood(
        fish: PhysicsObject,
        tiltX: Float,
        tiltY: Float,
        dt: Float,
        food: FoodManager.FoodParticle,
        foodManager: FoodManager,
    ) {
        foodManager.applyAttraction(fish, food, dt)
        fish.vx += tiltX * fish.gravityScale * dt * foodManager.tiltDampenFactor
        fish.vy += tiltY * fish.gravityScale * dt * foodManager.tiltDampenFactor
    }

    private fun idleSwim(
        fish: PhysicsObject,
        tiltX: Float,
        tiltY: Float,
        dt: Float,
        idleBlend: Float,
        elapsedTime: Float,
    ) {
        val tiltFactor = 1f - idleBlend
        fish.vx += tiltX * fish.gravityScale * dt * tiltFactor
        fish.vy += tiltY * fish.gravityScale * dt * tiltFactor

        fish.vx += sin((elapsedTime * fish.swimSpeedX + fish.swimPhase).toDouble()).toFloat() * fish.swimForce * dt * idleBlend
        fish.vy += cos((elapsedTime * fish.swimSpeedY + fish.swimPhase * IDLE_SWIM_Y_PHASE_SCALE).toDouble()).toFloat() * fish.swimForce * IDLE_SWIM_Y_FORCE_SCALE * dt * idleBlend
    }

    private fun followTilt(
        fish: PhysicsObject,
        tiltX: Float,
        tiltY: Float,
        dt: Float,
    ) {
        fish.vx += tiltX * fish.gravityScale * dt
        fish.vy += tiltY * fish.gravityScale * dt
    }

    private fun applyDragAndIntegrate(fish: PhysicsObject, dt: Float) {
        fish.vx *= fish.drag
        fish.vy *= fish.drag
        fish.x += fish.vx * dt
        fish.y += fish.vy * dt
    }
}
