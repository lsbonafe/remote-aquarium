package com.remoteaquarium.presentation.physics

/**
 * Applies physics to a bubble each frame.
 *
 * Behavior rules:
 *  1. Always         → rise with buoyancy, respond to tilt, apply drag
 *  2. Reached surface → respawn at the bottom with random position
 */
object BubblePhysics {

    private const val BUOYANCY = -80f
    private const val MIN_RESPAWN_VY = -20f
    private const val RESPAWN_VY_RANGE = 30f

    fun update(bubble: PhysicsObject, tiltX: Float, tiltY: Float, dt: Float, world: PhysicsWorld) {
        rise(bubble, tiltX, tiltY, dt)
        world.clampAndBounce(bubble)

        if (reachedSurface(bubble, world)) {
            respawnAtBottom(bubble, world)
        }
    }

    private fun rise(bubble: PhysicsObject, tiltX: Float, tiltY: Float, dt: Float) {
        bubble.vy += BUOYANCY * dt
        bubble.vx += tiltX * bubble.gravityScale * dt
        bubble.vy += tiltY * bubble.gravityScale * dt
        bubble.vx *= bubble.drag
        bubble.vy *= bubble.drag
        bubble.x += bubble.vx * dt
        bubble.y += bubble.vy * dt
    }

    private fun reachedSurface(bubble: PhysicsObject, world: PhysicsWorld): Boolean {
        return bubble.y <= world.margin
    }

    private fun respawnAtBottom(bubble: PhysicsObject, world: PhysicsWorld) {
        bubble.y = world.height - world.margin
        bubble.x = world.margin + (Math.random() * (world.width - world.margin * 2)).toFloat()
        bubble.vy = MIN_RESPAWN_VY - (Math.random() * RESPAWN_VY_RANGE).toFloat()
        bubble.vx = 0f
    }
}
