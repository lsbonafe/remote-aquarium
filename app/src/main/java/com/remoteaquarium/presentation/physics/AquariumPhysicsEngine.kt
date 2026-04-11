package com.remoteaquarium.presentation.physics

import com.remoteaquarium.domain.model.SensorData
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class PhysicsObject(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
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
    val bubbles: List<Pair<Float, Float>>,
)

class AquariumPhysicsEngine(
    private val width: Float,
    private val height: Float,
    private val margin: Float = 40f,
) {
    private val fishObjects = listOf(
        // Large (heavy, slow swim)
        PhysicsObject(width * 0.3f, height * 0.25f, drag = 0.96f, gravityScale = 600f, restitution = 0.25f, radius = 52f, swimSpeedX = 0.25f, swimSpeedY = 0.15f, swimForce = 30f, swimPhase = 0f),
        PhysicsObject(width * 0.7f, height * 0.40f, drag = 0.95f, gravityScale = 550f, restitution = 0.22f, radius = 48f, swimSpeedX = 0.2f, swimSpeedY = 0.18f, swimForce = 28f, swimPhase = 1.2f),
        PhysicsObject(width * 0.5f, height * 0.60f, drag = 0.96f, gravityScale = 580f, restitution = 0.2f, radius = 45f, swimSpeedX = 0.3f, swimSpeedY = 0.12f, swimForce = 32f, swimPhase = 2.5f),
        // Medium (moderate swim)
        PhysicsObject(width * 0.2f, height * 0.35f, drag = 0.97f, gravityScale = 750f, restitution = 0.3f, radius = 34f, swimSpeedX = 0.4f, swimSpeedY = 0.25f, swimForce = 50f, swimPhase = 0.8f),
        PhysicsObject(width * 0.8f, height * 0.50f, drag = 0.97f, gravityScale = 800f, restitution = 0.32f, radius = 38f, swimSpeedX = 0.35f, swimSpeedY = 0.3f, swimForce = 45f, swimPhase = 3.1f),
        PhysicsObject(width * 0.4f, height * 0.70f, drag = 0.96f, gravityScale = 720f, restitution = 0.28f, radius = 32f, swimSpeedX = 0.45f, swimSpeedY = 0.2f, swimForce = 55f, swimPhase = 1.7f),
        PhysicsObject(width * 0.6f, height * 0.30f, drag = 0.97f, gravityScale = 780f, restitution = 0.3f, radius = 36f, swimSpeedX = 0.38f, swimSpeedY = 0.28f, swimForce = 48f, swimPhase = 4.2f),
        PhysicsObject(width * 0.15f, height * 0.55f, drag = 0.96f, gravityScale = 740f, restitution = 0.28f, radius = 33f, swimSpeedX = 0.42f, swimSpeedY = 0.22f, swimForce = 52f, swimPhase = 2.0f),
        PhysicsObject(width * 0.85f, height * 0.65f, drag = 0.97f, gravityScale = 810f, restitution = 0.32f, radius = 35f, swimSpeedX = 0.33f, swimSpeedY = 0.27f, swimForce = 46f, swimPhase = 5.5f),
        // Small (fast swim, darty)
        PhysicsObject(width * 0.25f, height * 0.20f, drag = 0.98f, gravityScale = 1000f, restitution = 0.4f, radius = 22f, swimSpeedX = 0.7f, swimSpeedY = 0.5f, swimForce = 80f, swimPhase = 0.3f),
        PhysicsObject(width * 0.75f, height * 0.28f, drag = 0.98f, gravityScale = 1050f, restitution = 0.42f, radius = 18f, swimSpeedX = 0.8f, swimSpeedY = 0.45f, swimForce = 85f, swimPhase = 1.9f),
        PhysicsObject(width * 0.45f, height * 0.42f, drag = 0.98f, gravityScale = 980f, restitution = 0.38f, radius = 21f, swimSpeedX = 0.65f, swimSpeedY = 0.55f, swimForce = 75f, swimPhase = 3.7f),
        PhysicsObject(width * 0.55f, height * 0.75f, drag = 0.98f, gravityScale = 1020f, restitution = 0.4f, radius = 18f, swimSpeedX = 0.75f, swimSpeedY = 0.48f, swimForce = 82f, swimPhase = 5.0f),
        PhysicsObject(width * 0.1f, height * 0.68f, drag = 0.98f, gravityScale = 1060f, restitution = 0.42f, radius = 22f, swimSpeedX = 0.85f, swimSpeedY = 0.4f, swimForce = 90f, swimPhase = 2.3f),
        PhysicsObject(width * 0.9f, height * 0.38f, drag = 0.98f, gravityScale = 1100f, restitution = 0.44f, radius = 20f, swimSpeedX = 0.6f, swimSpeedY = 0.6f, swimForce = 78f, swimPhase = 4.8f),
        PhysicsObject(width * 0.35f, height * 0.48f, drag = 0.98f, gravityScale = 970f, restitution = 0.38f, radius = 16f, swimSpeedX = 0.9f, swimSpeedY = 0.35f, swimForce = 88f, swimPhase = 0.6f),
        PhysicsObject(width * 0.65f, height * 0.58f, drag = 0.98f, gravityScale = 1080f, restitution = 0.43f, radius = 21f, swimSpeedX = 0.72f, swimSpeedY = 0.52f, swimForce = 83f, swimPhase = 3.3f),
        PhysicsObject(width * 0.5f, height * 0.15f, drag = 0.98f, gravityScale = 950f, restitution = 0.36f, radius = 18f, swimSpeedX = 0.68f, swimSpeedY = 0.42f, swimForce = 76f, swimPhase = 1.4f),
    )

    private val bubbleObjects = listOf(
        PhysicsObject(width * 0.20f, height * 0.85f, vy = -60f, drag = 0.99f, gravityScale = 150f, restitution = 0.1f, radius = 5f),
        PhysicsObject(width * 0.40f, height * 0.90f, vy = -50f, drag = 0.99f, gravityScale = 120f, restitution = 0.1f, radius = 4f),
        PhysicsObject(width * 0.60f, height * 0.80f, vy = -70f, drag = 0.99f, gravityScale = 130f, restitution = 0.1f, radius = 6f),
        PhysicsObject(width * 0.80f, height * 0.88f, vy = -55f, drag = 0.99f, gravityScale = 140f, restitution = 0.1f, radius = 4f),
        PhysicsObject(width * 0.30f, height * 0.75f, vy = -65f, drag = 0.99f, gravityScale = 110f, restitution = 0.1f, radius = 5f),
        PhysicsObject(width * 0.70f, height * 0.92f, vy = -45f, drag = 0.99f, gravityScale = 160f, restitution = 0.1f, radius = 3f),
    )

    private var lastTimeNanos = System.nanoTime()
    private var elapsedTimeSec = 0f
    private var lastSignificantTiltTime = 0f
    private var previousTiltX = 0f
    private var previousTiltY = 0f

    companion object {
        const val REST_ACCEL_Y = 0.55f
        private const val BUBBLE_BUOYANCY = -80f
        private const val IDLE_THRESHOLD_SEC = 5f
        private const val TILT_CHANGE_THRESHOLD = 0.05f
    }

    fun update(sensor: SensorData): PhysicsState {
        val now = System.nanoTime()
        val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceAtMost(0.05f)
        lastTimeNanos = now
        elapsedTimeSec += dt

        val tiltX = -sensor.accelX
        val tiltY = (sensor.accelY - REST_ACCEL_Y)

        // Detect significant tilt change
        if (abs(tiltX - previousTiltX) > TILT_CHANGE_THRESHOLD ||
            abs(tiltY - previousTiltY) > TILT_CHANGE_THRESHOLD
        ) {
            lastSignificantTiltTime = elapsedTimeSec
        }
        previousTiltX = tiltX
        previousTiltY = tiltY

        val isIdle = (elapsedTimeSec - lastSignificantTiltTime) > IDLE_THRESHOLD_SEC
        // Smooth blend: 0 = full tilt physics, 1 = full idle swim
        val idleBlend = if (isIdle) {
            ((elapsedTimeSec - lastSignificantTiltTime - IDLE_THRESHOLD_SEC) / 2f).coerceAtMost(1f)
        } else {
            0f
        }

        for ((i, fish) in fishObjects.withIndex()) {
            updateFish(fish, tiltX, tiltY, dt, idleBlend)
        }

        // Fish-to-fish collision
        for (i in fishObjects.indices) {
            for (j in i + 1 until fishObjects.size) {
                resolveCollision(fishObjects[i], fishObjects[j])
            }
        }

        for (bubble in bubbleObjects) {
            bubble.vy += BUBBLE_BUOYANCY * dt
            updateObject(bubble, tiltX, tiltY, dt)
            if (bubble.y <= margin) {
                bubble.y = height - margin
                bubble.x = margin + (Math.random() * (width - margin * 2)).toFloat()
                bubble.vy = -(20f + (Math.random() * 30f).toFloat())
                bubble.vx = 0f
            }
        }

        return PhysicsState(
            fish = fishObjects.map { it.x to it.y },
            bubbles = bubbleObjects.map { it.x to it.y },
        )
    }

    private fun updateFish(fish: PhysicsObject, tiltX: Float, tiltY: Float, dt: Float, idleBlend: Float) {
        // Tilt physics (fades out when idle)
        val tiltFactor = 1f - idleBlend
        fish.vx += tiltX * fish.gravityScale * dt * tiltFactor
        fish.vy += tiltY * fish.gravityScale * dt * tiltFactor

        // Idle swim force (fades in when idle)
        if (idleBlend > 0f) {
            val t = elapsedTimeSec
            fish.vx += sin((t * fish.swimSpeedX + fish.swimPhase).toDouble()).toFloat() * fish.swimForce * dt * idleBlend
            fish.vy += cos((t * fish.swimSpeedY + fish.swimPhase * 1.3f).toDouble()).toFloat() * fish.swimForce * 0.6f * dt * idleBlend
        }

        fish.vx *= fish.drag
        fish.vy *= fish.drag
        fish.x += fish.vx * dt
        fish.y += fish.vy * dt

        if (fish.x < margin) { fish.x = margin; fish.vx = -fish.vx * fish.restitution }
        if (fish.x > width - margin) { fish.x = width - margin; fish.vx = -fish.vx * fish.restitution }
        if (fish.y < margin) { fish.y = margin; fish.vy = -fish.vy * fish.restitution }
        if (fish.y > height - margin) { fish.y = height - margin; fish.vy = -fish.vy * fish.restitution }
    }

    private fun updateObject(obj: PhysicsObject, tiltX: Float, tiltY: Float, dt: Float) {
        obj.vx += tiltX * obj.gravityScale * dt
        obj.vy += tiltY * obj.gravityScale * dt
        obj.vx *= obj.drag
        obj.vy *= obj.drag
        obj.x += obj.vx * dt
        obj.y += obj.vy * dt

        if (obj.x < margin) { obj.x = margin; obj.vx = -obj.vx * obj.restitution }
        if (obj.x > width - margin) { obj.x = width - margin; obj.vx = -obj.vx * obj.restitution }
        if (obj.y < margin) { obj.y = margin; obj.vy = -obj.vy * obj.restitution }
        if (obj.y > height - margin) { obj.y = height - margin; obj.vy = -obj.vy * obj.restitution }
    }

    private fun resolveCollision(a: PhysicsObject, b: PhysicsObject) {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val distSq = dx * dx + dy * dy
        val minDist = a.radius + b.radius

        if (distSq >= minDist * minDist || distSq == 0f) return

        val dist = sqrt(distSq)
        val nx = dx / dist
        val ny = dy / dist
        val overlap = minDist - dist

        a.x -= nx * overlap * 0.5f
        a.y -= ny * overlap * 0.5f
        b.x += nx * overlap * 0.5f
        b.y += ny * overlap * 0.5f

        val dvx = a.vx - b.vx
        val dvy = a.vy - b.vy
        val dot = dvx * nx + dvy * ny

        if (dot > 0) {
            a.vx -= dot * nx * 0.5f
            a.vy -= dot * ny * 0.5f
            b.vx += dot * nx * 0.5f
            b.vy += dot * ny * 0.5f
        }
    }
}
