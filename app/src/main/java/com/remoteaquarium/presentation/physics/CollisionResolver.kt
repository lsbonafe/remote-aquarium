package com.remoteaquarium.presentation.physics

import kotlin.math.sqrt

object CollisionResolver {

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

    fun resolveAll(objects: List<PhysicsObject>) {
        for (i in objects.indices) {
            for (j in i + 1 until objects.size) {
                resolve(objects[i], objects[j])
            }
        }
    }
}
