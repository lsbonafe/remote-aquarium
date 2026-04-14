package com.remoteaquarium.presentation.physics

import kotlin.math.sqrt

/**
 * Resolves fish-to-fish collisions via position separation and velocity exchange.
 *
 * Behavior rules:
 *  1. Not overlapping → no action
 *  2. Overlapping     → push apart by half the overlap each
 *  3. Approaching     → exchange velocity along collision normal
 *  4. Separating      → no velocity change (let them drift apart)
 */
object CollisionResolver {

    private const val SEPARATION_FACTOR = 0.5f
    private const val VELOCITY_EXCHANGE_FACTOR = 0.5f

    fun resolve(a: PhysicsObject, b: PhysicsObject) {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val distSq = dx * dx + dy * dy
        val minDist = a.radius + b.radius

        if (distSq >= minDist * minDist || distSq == 0f) return

        val dist = sqrt(distSq)
        val nx = dx / dist
        val ny = dy / dist
        val overlap = minDist - dist

        a.x -= nx * overlap * SEPARATION_FACTOR
        a.y -= ny * overlap * SEPARATION_FACTOR
        b.x += nx * overlap * SEPARATION_FACTOR
        b.y += ny * overlap * SEPARATION_FACTOR

        val dvx = a.vx - b.vx
        val dvy = a.vy - b.vy
        val dot = dvx * nx + dvy * ny

        if (dot > 0) {
            a.vx -= dot * nx * VELOCITY_EXCHANGE_FACTOR
            a.vy -= dot * ny * VELOCITY_EXCHANGE_FACTOR
            b.vx += dot * nx * VELOCITY_EXCHANGE_FACTOR
            b.vy += dot * ny * VELOCITY_EXCHANGE_FACTOR
        }
    }

    fun resolveAll(objects: List<PhysicsObject>, alive: BooleanArray? = null) {
        for (i in objects.indices) {
            if (alive != null && !alive[i]) continue
            for (j in i + 1 until objects.size) {
                if (alive != null && !alive[j]) continue
                resolve(objects[i], objects[j])
            }
        }
    }
}
