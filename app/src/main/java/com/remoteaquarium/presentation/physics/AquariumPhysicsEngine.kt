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
    var mouthOpen: Float = 0f,
    var scale: Float = 1f,
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

    // Pre-allocated buffers to avoid per-frame list allocations
    private val fishPositions = MutableList(fishObjects.size) { 0f to 0f }
    private val fishAngleData = MutableList(fishObjects.size) { 1f to 0f }
    private val fishMouthData = MutableList(fishObjects.size) { 0f }
    private val fishScaleData = MutableList(fishObjects.size) { 1f }
    private val bubblePositionData = MutableList(bubbleObjects.size) { 0f to 0f }

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
        MouthAnimation.update(fishObjects, eatingFish, dt)
        GrowthTracker.update(fishObjects, eatingFish)

        // Fish-to-fish collision
        CollisionResolver.resolveAll(fishObjects)

        // Bubbles
        for (bubble in bubbleObjects) {
            BubblePhysics.update(bubble, tiltX, tiltY, dt, world)
        }

        // Food
        foodManager.updatePositions(dt)

        // Fill pre-allocated buffers (avoids per-frame list allocations)
        for (i in fishObjects.indices) {
            val f = fishObjects[i]
            fishPositions[i] = f.x to f.y
            fishAngleData[i] = cos(f.currentAngle) to sin(f.currentAngle)
            fishMouthData[i] = f.mouthOpen
            fishScaleData[i] = f.scale
        }
        for (i in bubbleObjects.indices) {
            bubblePositionData[i] = bubbleObjects[i].x to bubbleObjects[i].y
        }

        return PhysicsState(
            fish = fishPositions,
            fishAngles = fishAngleData,
            fishMouthOpen = fishMouthData,
            fishScale = fishScaleData,
            bubbles = bubblePositionData,
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
