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
    val crownX: Float = -100f,
    val crownY: Float = -100f,
    val crownCos: Float = 1f,
    val crownSin: Float = 0f,
    val crownScale: Float = 0f,
)

internal fun lerpAngle(current: Float, target: Float, factor: Float): Float {
    var diff = (target - current).toDouble()
    while (diff > PI) diff -= 2 * PI
    while (diff < -PI) diff += 2 * PI
    return current + diff.toFloat() * factor
}

class AquariumPhysicsEngine(
    private val width: Float,
    private val height: Float,
) {
    private val world = PhysicsWorld(width, height)
    private val idleDetector = IdleDetector()
    private val foodManager = FoodManager(world)
    private val fishObjects = FishConfigs.create(width, height)
    private val bubbleObjects = FishConfigs.createBubbles(width, height)
    private val stateMachine = PredatorCycleStateMachine(fishObjects.size)
    private val resetAnimation = ResetAnimation()

    private val fishMouthOpen = FloatArray(fishObjects.size)
    private val fishScale = FloatArray(fishObjects.size) { 1f }
    private val swallowTimers = FloatArray(fishObjects.size) { -1f }
    private var lastTimeNanos = System.nanoTime()
    private var elapsedTimeSec = 0f

    companion object {
        const val REST_ACCEL_Y = 0.55f
        private const val NANOS_TO_SEC = 1_000_000_000f
        private const val MAX_DT = 0.05f
        private const val PREDATOR_TILT_DAMPEN = 0.15f
        private const val PREDATOR_DRAG = 0.99f
        private const val CENTER_SWIM_SPEED = 3f
        private const val RESPAWN_INWARD_SPEED = 0.5f
    }

    fun update(sensor: SensorData): PhysicsState {
        val now = System.nanoTime()
        val dt = ((now - lastTimeNanos) / NANOS_TO_SEC).coerceAtMost(MAX_DT)
        lastTimeNanos = now
        elapsedTimeSec += dt

        val tiltX = -sensor.accelX
        val tiltY = (sensor.accelY - REST_ACCEL_Y)
        val idleBlend = idleDetector.update(tiltX, tiltY, elapsedTimeSec)

        stateMachine.update(fishScale, elapsedTimeSec)

        // Fish movement — each fish delegates to its situational behavior
        var cycle = stateMachine.currentState
        for ((i, fish) in fishObjects.withIndex()) {
            if (SwallowAnimation.isBeingSwallowed(swallowTimers, i)) continue
            when {
                cycle == CycleState.RESET   -> {
                    respawnIfReady(i, fish)
                    FishMotion.applyDragAndIntegrate(fish, dt)
                }
                !stateMachine.isAlive(i)    -> continue
                stateMachine.isPredator(i)  -> huntPrey(fish, tiltX, tiltY, dt)
                else                        -> {
                    val foodTarget = if (foodManager.hasFood) foodManager.findNearestTarget(fish) else null
                    FishMotion.applyForces(fish, tiltX, tiltY, dt, idleBlend, elapsedTimeSec, foodTarget, foodManager)
                }
            }
            world.clampAndBounce(fish)
        }

        // Predator eating prey (with cooldown between kills)
        PredatorHunting.updateCooldown(dt)
        val predatorEating = if (cycle == CycleState.PREDATOR) {
            val eaten = PredatorHunting.checkPreyEating(fishObjects, stateMachine, swallowTimers)
            eaten.forEach { SwallowAnimation.startSwallow(swallowTimers, it) }
            eaten
        } else emptySet()

        SwallowAnimation.update(swallowTimers, fishObjects, fishScale, stateMachine, dt)

        // Re-check state after swallows (last prey death may trigger CROWN)
        stateMachine.update(fishScale, elapsedTimeSec)
        cycle = stateMachine.currentState

        // Food eating (predator skipped)
        val alive = BooleanArray(fishObjects.size) { stateMachine.isAlive(it) }
        val foodEating = foodManager.checkEating(fishObjects, alive, stateMachine.predatorIndex)

        // Reactions to eating
        val mouthEating = foodEating + if (predatorEating.isNotEmpty()) setOf(stateMachine.predatorIndex) else emptySet()
        MouthAnimation.update(fishMouthOpen, mouthEating, dt)
        GrowthTracker.update(fishScale, foodEating)

        // Facing direction for non-predator fish
        for ((i, fish) in fishObjects.withIndex()) {
            if (!stateMachine.isAlive(i) || stateMachine.isPredator(i)) continue
            val foodTarget = if (foodManager.hasFood) foodManager.findNearestTarget(fish) else null
            FacingDirection.update(fish, foodTarget, idleBlend, dt)
        }

        CollisionResolver.resolveAll(fishObjects, alive)

        for (bubble in bubbleObjects) BubblePhysics.update(bubble, tiltX, tiltY, dt, world)
        foodManager.updatePositions(dt)

        // Reset animation
        if (cycle == CycleState.RESET) {
            startResetIfNeeded()
            fishScale[stateMachine.predatorIndex] = resetAnimation.predatorScale(elapsedTimeSec)
            if (resetAnimation.isComplete(elapsedTimeSec)) {
                completeReset()
                cycle = stateMachine.currentState
            }
        }

        val crown = CrownPosition.calculate(cycle, stateMachine.predatorIndex, fishObjects, fishScale, resetAnimation, elapsedTimeSec)

        return PhysicsState(
            fish = fishObjects.map { it.x to it.y },
            fishAngles = fishObjects.map { cos(it.currentAngle) to sin(it.currentAngle) },
            fishMouthOpen = fishMouthOpen.toList(),
            fishScale = fishScale.toList(),
            bubbles = bubbleObjects.map { it.x to it.y },
            food = foodManager.positions,
            crownX = crown.x, crownY = crown.y,
            crownCos = crown.cos, crownSin = crown.sin,
            crownScale = crown.scale,
        )
    }

    fun feed(x: Float, y: Float) {
        foodManager.spawn(x, y, elapsedTimeSec)
    }

    private fun huntPrey(predator: PhysicsObject, tiltX: Float, tiltY: Float, dt: Float) {
        val preyIdx = PredatorHunting.findNearestPrey(predator, fishObjects, stateMachine, swallowTimers)
        val anyHuntable = stateMachine.preyIndices().any { swallowTimers[it] < 0f }
        when {
            preyIdx != null -> {
                PredatorHunting.applyAttraction(predator, fishObjects[preyIdx], dt)
                FacingDirection.updateTowardTarget(predator, fishObjects[preyIdx].x, fishObjects[preyIdx].y, dt)
            }
            !anyHuntable -> {
                // No huntable prey left — swim to center and wait for crown
                swimToCenter(predator, dt)
            }
            else -> {
                // Prey being swallowed — just coast
                FacingDirection.update(predator, null, 0f, dt)
            }
        }
        predator.vx += tiltX * predator.gravityScale * dt * PREDATOR_TILT_DAMPEN
        predator.vy += tiltY * predator.gravityScale * dt * PREDATOR_TILT_DAMPEN
        predator.vx *= PREDATOR_DRAG
        predator.vy *= PREDATOR_DRAG
        predator.x += predator.vx * dt
        predator.y += predator.vy * dt
    }

    private fun swimToCenter(fish: PhysicsObject, dt: Float) {
        val centerX = width / 2
        val centerY = height / 2
        val dx = centerX - fish.x
        val dy = centerY - fish.y
        val steer = (CENTER_SWIM_SPEED * dt).coerceAtMost(1f)
        fish.vx += dx * steer
        fish.vy += dy * steer
        FacingDirection.updateTowardTarget(fish, centerX, centerY, dt)
    }

    private fun respawnIfReady(index: Int, fish: PhysicsObject) {
        if (!stateMachine.isAlive(index) && elapsedTimeSec >= resetAnimation.fishRespawnTime(index)) {
            val (rx, ry) = resetAnimation.respawnPositions[index] ?: return
            fish.x = rx
            fish.y = ry
            fish.vx = (width / 2 - rx) * RESPAWN_INWARD_SPEED
            fish.vy = (height / 2 - ry) * RESPAWN_INWARD_SPEED
            fishScale[index] = 1f
            stateMachine.markAlive(index)
        }
    }

    private fun startResetIfNeeded() {
        if (resetAnimation.respawnPositions.isEmpty()) {
            val deadFish = fishObjects.indices.filter { !stateMachine.isAlive(it) }
            resetAnimation.start(elapsedTimeSec, stateMachine.predatorIndex,
                fishScale[stateMachine.predatorIndex], deadFish, width, height)
        }
    }

    private fun completeReset() {
        stateMachine.completeReset()
        PredatorHunting.reset()
        resetAnimation.clear()
        for (i in fishScale.indices) fishScale[i] = 1f
        for (i in swallowTimers.indices) swallowTimers[i] = -1f
        for (i in fishMouthOpen.indices) fishMouthOpen[i] = 0f
    }
}
