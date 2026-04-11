package com.remoteaquarium.presentation.physics

import com.remoteaquarium.domain.model.SensorData

data class PhysicsObject(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val drag: Float = 0.96f,
    val gravityScale: Float = 300f,
    val restitution: Float = 0.3f,
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
        // Large (heavier feel but still responsive)
        PhysicsObject(width * 0.3f, height * 0.25f, drag = 0.96f, gravityScale = 600f, restitution = 0.25f),
        PhysicsObject(width * 0.7f, height * 0.40f, drag = 0.95f, gravityScale = 550f, restitution = 0.22f),
        PhysicsObject(width * 0.5f, height * 0.60f, drag = 0.96f, gravityScale = 580f, restitution = 0.2f),
        // Medium (responsive)
        PhysicsObject(width * 0.2f, height * 0.35f, drag = 0.97f, gravityScale = 750f, restitution = 0.3f),
        PhysicsObject(width * 0.8f, height * 0.50f, drag = 0.97f, gravityScale = 800f, restitution = 0.32f),
        PhysicsObject(width * 0.4f, height * 0.70f, drag = 0.96f, gravityScale = 720f, restitution = 0.28f),
        PhysicsObject(width * 0.6f, height * 0.30f, drag = 0.97f, gravityScale = 780f, restitution = 0.3f),
        PhysicsObject(width * 0.15f, height * 0.55f, drag = 0.96f, gravityScale = 740f, restitution = 0.28f),
        PhysicsObject(width * 0.85f, height * 0.65f, drag = 0.97f, gravityScale = 810f, restitution = 0.32f),
        // Small (very reactive, zippy)
        PhysicsObject(width * 0.25f, height * 0.20f, drag = 0.98f, gravityScale = 1000f, restitution = 0.4f),
        PhysicsObject(width * 0.75f, height * 0.28f, drag = 0.98f, gravityScale = 1050f, restitution = 0.42f),
        PhysicsObject(width * 0.45f, height * 0.42f, drag = 0.98f, gravityScale = 980f, restitution = 0.38f),
        PhysicsObject(width * 0.55f, height * 0.75f, drag = 0.98f, gravityScale = 1020f, restitution = 0.4f),
        PhysicsObject(width * 0.1f, height * 0.68f, drag = 0.98f, gravityScale = 1060f, restitution = 0.42f),
        PhysicsObject(width * 0.9f, height * 0.38f, drag = 0.98f, gravityScale = 1100f, restitution = 0.44f),
        PhysicsObject(width * 0.35f, height * 0.48f, drag = 0.98f, gravityScale = 970f, restitution = 0.38f),
        PhysicsObject(width * 0.65f, height * 0.58f, drag = 0.98f, gravityScale = 1080f, restitution = 0.43f),
        PhysicsObject(width * 0.5f, height * 0.15f, drag = 0.98f, gravityScale = 950f, restitution = 0.36f),
    )

    private val bubbleObjects = listOf(
        PhysicsObject(width * 0.20f, height * 0.85f, vy = -60f, drag = 0.99f, gravityScale = 150f, restitution = 0.1f),
        PhysicsObject(width * 0.40f, height * 0.90f, vy = -50f, drag = 0.99f, gravityScale = 120f, restitution = 0.1f),
        PhysicsObject(width * 0.60f, height * 0.80f, vy = -70f, drag = 0.99f, gravityScale = 130f, restitution = 0.1f),
        PhysicsObject(width * 0.80f, height * 0.88f, vy = -55f, drag = 0.99f, gravityScale = 140f, restitution = 0.1f),
        PhysicsObject(width * 0.30f, height * 0.75f, vy = -65f, drag = 0.99f, gravityScale = 110f, restitution = 0.1f),
        PhysicsObject(width * 0.70f, height * 0.92f, vy = -45f, drag = 0.99f, gravityScale = 160f, restitution = 0.1f),
    )

    private var lastTimeNanos = System.nanoTime()

    companion object {
        const val REST_ACCEL_Y = 0.55f
        private const val BUBBLE_BUOYANCY = -80f
    }

    fun update(sensor: SensorData): PhysicsState {
        val now = System.nanoTime()
        val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceAtMost(0.05f)
        lastTimeNanos = now

        val tiltX = -sensor.accelX
        val tiltY = (sensor.accelY - REST_ACCEL_Y)

        for (fish in fishObjects) {
            updateObject(fish, tiltX, tiltY, dt)
        }

        for (bubble in bubbleObjects) {
            // Bubbles get buoyancy (always rise) + tilt
            bubble.vy += BUBBLE_BUOYANCY * dt
            updateObject(bubble, tiltX, tiltY, dt)
            // Respawn at bottom when bubble reaches top
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
}
