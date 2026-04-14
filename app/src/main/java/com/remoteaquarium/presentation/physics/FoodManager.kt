package com.remoteaquarium.presentation.physics

import kotlin.math.sqrt

/**
 * Manages food particles spawned by tap-to-feed.
 *
 * Behavior rules:
 *  1. Tap spawns food → particle sinks with gravity and drag
 *  2. Fish nearby     → attraction force pulls fish toward food
 *  3. Fish touches    → food consumed, fish marked as eating
 *  4. At floor        → food rests (velocity zeroed)
 */
class FoodManager(
    private val world: PhysicsWorld,
    private val maxFood: Int = 50,
    private val sinkSpeed: Float = 25f,
    private val eatDistance: Float = 80f,
    private val attractionForce: Float = 1200f,
    private val tiltDampen: Float = 0.15f,
) {
    companion object {
        private const val SINK_DRAG = 0.98f
    }

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

    fun checkEating(
        fishObjects: List<PhysicsObject>,
        fishAlive: BooleanArray? = null,
        skipIndex: Int = -1,
    ): Set<Int> {
        val eaten = mutableSetOf<FoodParticle>()
        val eatingFishIndices = mutableSetOf<Int>()
        for ((index, fish) in fishObjects.withIndex()) {
            if (fishAlive != null && !fishAlive[index]) continue
            if (index == skipIndex) continue
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
            food.obj.vy *= SINK_DRAG
            food.obj.y += food.obj.vy * dt

            if (food.obj.y > world.height - world.margin) {
                food.obj.y = world.height - world.margin
                food.obj.vy = 0f
            }
        }
    }

    val tiltDampenFactor: Float get() = tiltDampen
}
