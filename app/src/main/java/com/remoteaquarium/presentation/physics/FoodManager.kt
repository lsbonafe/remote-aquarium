package com.remoteaquarium.presentation.physics

import kotlin.math.sqrt

class FoodManager(
    private val world: PhysicsWorld,
    private val maxFood: Int = 50,
    private val sinkSpeed: Float = 25f,
    private val eatDistance: Float = 80f,
    private val attractionForce: Float = 1200f,
    private val tiltDampen: Float = 0.15f,
) {
    data class FoodParticle(
        val obj: PhysicsObject,
        val spawnTime: Float,
    )

    private val particles = mutableListOf<FoodParticle>()

    val hasFood: Boolean get() = particles.isNotEmpty()

    val positions: List<Pair<Float, Float>> get() = particles.map { it.obj.x to it.obj.y }

    fun spawn(x: Float, y: Float, elapsedTime: Float) {
        if (particles.size >= maxFood) return
        particles.add(
            FoodParticle(
                obj = PhysicsObject(x = x, y = y, vy = 0f, drag = 0.98f),
                spawnTime = elapsedTime,
            )
        )
    }

    fun findNearestTarget(fish: PhysicsObject): FoodParticle? {
        return particles.minByOrNull { f ->
            val dx = f.obj.x - fish.x
            val dy = f.obj.y - fish.y
            dx * dx + dy * dy
        }
    }

    fun applyAttraction(fish: PhysicsObject, target: FoodParticle, dt: Float) {
        val dx = target.obj.x - fish.x
        val dy = target.obj.y - fish.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist > 1f) {
            val nx = dx / dist
            val ny = dy / dist
            fish.vx += nx * attractionForce * dt
            fish.vy += ny * attractionForce * dt
        }
    }

    fun checkEating(fishObjects: List<PhysicsObject>): Set<Int> {
        val eaten = mutableSetOf<FoodParticle>()
        val eatingFishIndices = mutableSetOf<Int>()
        for ((index, fish) in fishObjects.withIndex()) {
            val eatDist = fish.radius + eatDistance
            for (food in particles) {
                if (food in eaten) continue
                val dx = food.obj.x - fish.x
                val dy = food.obj.y - fish.y
                if (dx * dx + dy * dy < eatDist * eatDist) {
                    eaten.add(food)
                    eatingFishIndices.add(index)
                    break
                }
            }
        }
        particles.removeAll(eaten)
        return eatingFishIndices
    }

    fun updatePositions(dt: Float) {
        particles.forEach { food ->
            food.obj.vy += sinkSpeed * dt
            food.obj.vy *= 0.98f
            food.obj.y += food.obj.vy * dt

            if (food.obj.y > world.height - world.margin) {
                food.obj.y = world.height - world.margin
                food.obj.vy = 0f
            }
        }
    }

    val tiltDampenFactor: Float get() = tiltDampen
}
