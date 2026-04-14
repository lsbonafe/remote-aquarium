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
    val fishMouthOpen: List<Float>,
    val fishScale: List<Float>,
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

    private val fishMouthOpen = FloatArray(fishObjects.size)
    private val fishScale = FloatArray(fishObjects.size) { 1f }
    private var lastTimeNanos = System.nanoTime()
    private var elapsedTimeSec = 0f

    companion object {
        const val REST_ACCEL_Y = 0.55f
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

        // Eating check + mouth animation
        val eatingFish = foodManager.checkEating(fishObjects)
        MouthAnimation.update(fishMouthOpen, eatingFish, dt)
        GrowthTracker.update(fishScale, eatingFish)

        // Fish-to-fish collision
        CollisionResolver.resolveAll(fishObjects)

        // Bubbles
        for (bubble in bubbleObjects) {
            BubblePhysics.update(bubble, tiltX, tiltY, dt, world)
        }

        // Food
        foodManager.updatePositions(dt)

        return PhysicsState(
            fish = fishObjects.map { it.x to it.y },
            fishAngles = fishObjects.map { cos(it.currentAngle) to sin(it.currentAngle) },
            fishMouthOpen = fishMouthOpen.toList(),
            fishScale = fishScale.toList(),
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
        FishMotion.applyForces(fish, tiltX, tiltY, dt, idleBlend, elapsedTimeSec, foodTarget, foodManager)
        FacingDirection.update(fish, foodTarget, idleBlend, dt)
    }
}
