package com.remoteaquarium.presentation.physics

import com.remoteaquarium.domain.model.SensorData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class PhysicsObject(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var currentAngle: Float = 0f,
    val drag: Float = 0.96f,
    val gravityScale: Float = 300f,
    val restitution: Float = 0.3f,
    val radius: Float = 30f,
    val swimSpeedX: Float = 0.3f,
    val swimSpeedY: Float = 0.2f,
    val swimForce: Float = 40f,
    val swimPhase: Float = 0f,
)

data class PhysicsState(
    val fish: List<Pair<Float, Float>>,
    val fishAngles: List<Pair<Float, Float>>,
    val bubbles: List<Pair<Float, Float>>,
    val food: List<Pair<Float, Float>>,
)

internal fun lerpAngle(current: Float, target: Float, factor: Float): Float {
    var diff = (target - current).toDouble()
    while (diff > PI) diff -= 2 * PI
    while (diff < -PI) diff += 2 * PI
    return current + diff.toFloat() * factor
}

class AquariumPhysicsEngine(
    width: Float,
    height: Float,
) {
    private val world = PhysicsWorld(width, height)
    private val idleDetector = IdleDetector()
    private val foodManager = FoodManager(world)
    private val fishObjects = FishConfigs.create(width, height)
    private val bubbleObjects = FishConfigs.createBubbles(width, height)

    private var lastTimeNanos = System.nanoTime()
    private var elapsedTimeSec = 0f

    companion object {
        const val REST_ACCEL_Y = 0.55f
        private const val BUBBLE_BUOYANCY = -80f
        private const val BUBBLE_MIN_RESPAWN_VY = -20f
        private const val BUBBLE_RESPAWN_VY_RANGE = 30f
        private const val IDLE_SWIM_Y_PHASE_SCALE = 1.3f
        private const val IDLE_SWIM_Y_FORCE_SCALE = 0.6f
        private const val NANOS_TO_SEC = 1_000_000_000f
        private const val MAX_DT = 0.05f
    }

    fun update(sensor: SensorData): PhysicsState {
        val now = System.nanoTime()
        val dt = ((now - lastTimeNanos) / NANOS_TO_SEC).coerceAtMost(MAX_DT)
        lastTimeNanos = now
        elapsedTimeSec += dt

        val tiltX = -sensor.accelX
        val tiltY = (sensor.accelY - REST_ACCEL_Y)

        val idleBlend = idleDetector.update(tiltX, tiltY, elapsedTimeSec)

        // Fish: find food targets, apply forces, integrate
        for (fish in fishObjects) {
            val foodTarget = if (foodManager.hasFood) foodManager.findNearestTarget(fish) else null
            updateFish(fish, tiltX, tiltY, dt, idleBlend, foodTarget)
            world.clampAndBounce(fish)
        }

        // Eating check
        foodManager.checkEating(fishObjects)

        // Fish-to-fish collision
        CollisionResolver.resolveAll(fishObjects)

        // Bubbles
        for (bubble in bubbleObjects) {
            bubble.vy += BUBBLE_BUOYANCY * dt
            bubble.vx += tiltX * bubble.gravityScale * dt
            bubble.vy += tiltY * bubble.gravityScale * dt
            bubble.vx *= bubble.drag
            bubble.vy *= bubble.drag
            bubble.x += bubble.vx * dt
            bubble.y += bubble.vy * dt
            world.clampAndBounce(bubble)
            if (bubble.y <= world.margin) {
                bubble.y = world.height - world.margin
                bubble.x = world.margin + (Math.random() * (world.width - world.margin * 2)).toFloat()
                bubble.vy = BUBBLE_MIN_RESPAWN_VY - (Math.random() * BUBBLE_RESPAWN_VY_RANGE).toFloat()
                bubble.vx = 0f
            }
        }

        // Food
        foodManager.updatePositions(dt)

        return PhysicsState(
            fish = fishObjects.map { it.x to it.y },
            fishAngles = fishObjects.map { cos(it.currentAngle) to sin(it.currentAngle) },
            bubbles = bubbleObjects.map { it.x to it.y },
            food = foodManager.positions,
        )
    }

    fun feed(x: Float, y: Float) {
        foodManager.spawn(x, y, elapsedTimeSec)
    }

    private fun updateFish(
        fish: PhysicsObject,
        tiltX: Float,
        tiltY: Float,
        dt: Float,
        idleBlend: Float,
        foodTarget: FoodManager.FoodParticle?,
    ) {
        if (foodTarget != null) {
            foodManager.applyAttraction(fish, foodTarget, dt)
            fish.vx += tiltX * fish.gravityScale * dt * foodManager.tiltDampenFactor
            fish.vy += tiltY * fish.gravityScale * dt * foodManager.tiltDampenFactor
        } else {
            val tiltFactor = 1f - idleBlend
            fish.vx += tiltX * fish.gravityScale * dt * tiltFactor
            fish.vy += tiltY * fish.gravityScale * dt * tiltFactor

            if (idleBlend > 0f) {
                val t = elapsedTimeSec
                fish.vx += sin((t * fish.swimSpeedX + fish.swimPhase).toDouble()).toFloat() * fish.swimForce * dt * idleBlend
                fish.vy += cos((t * fish.swimSpeedY + fish.swimPhase * IDLE_SWIM_Y_PHASE_SCALE).toDouble()).toFloat() * fish.swimForce * IDLE_SWIM_Y_FORCE_SCALE * dt * idleBlend
            }
        }

        fish.vx *= fish.drag
        fish.vy *= fish.drag
        fish.x += fish.vx * dt
        fish.y += fish.vy * dt

        FacingDirection.update(fish, foodTarget, idleBlend, dt)
    }
}
