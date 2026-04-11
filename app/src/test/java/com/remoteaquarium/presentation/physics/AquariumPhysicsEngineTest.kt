package com.remoteaquarium.presentation.physics

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.sqrt

class AquariumPhysicsEngineTest {

    @Test
    fun `overlapping fish get pushed apart`() {
        val a = PhysicsObject(x = 100f, y = 100f, radius = 30f)
        val b = PhysicsObject(x = 120f, y = 100f, radius = 30f)
        // Overlap: distance=20, minDist=60, overlap=40

        resolveCollision(a, b)

        val dist = sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y))
        assertTrue(dist >= 59f, "Fish should be pushed to at least minDist apart, got $dist")
    }

    @Test
    fun `distant fish are not affected`() {
        val a = PhysicsObject(x = 100f, y = 100f, radius = 30f)
        val b = PhysicsObject(x = 300f, y = 300f, radius = 30f)
        val origAx = a.x
        val origBx = b.x

        resolveCollision(a, b)

        assertTrue(a.x == origAx && b.x == origBx, "Distant fish should not move")
    }

    @Test
    fun `approaching fish exchange velocity`() {
        val a = PhysicsObject(x = 100f, y = 100f, vx = 50f, vy = 0f, radius = 30f)
        val b = PhysicsObject(x = 140f, y = 100f, vx = -50f, vy = 0f, radius = 30f)

        resolveCollision(a, b)

        assertTrue(a.vx < 50f, "Fish A should slow down, got vx=${a.vx}")
        assertTrue(b.vx > -50f, "Fish B should slow down, got vx=${b.vx}")
    }

    @Test
    fun `collision respects different radii`() {
        val big = PhysicsObject(x = 100f, y = 100f, radius = 50f)
        val small = PhysicsObject(x = 140f, y = 100f, radius = 20f)
        // minDist=70, distance=40, overlap=30

        resolveCollision(big, small)

        val dist = sqrt((small.x - big.x) * (small.x - big.x) + (small.y - big.y) * (small.y - big.y))
        assertTrue(dist >= 69f, "Should be pushed to at least minDist, got $dist")
    }

    @Test
    fun `separating fish dont exchange velocity`() {
        val a = PhysicsObject(x = 100f, y = 100f, vx = -50f, vy = 0f, radius = 30f)
        val b = PhysicsObject(x = 140f, y = 100f, vx = 50f, vy = 0f, radius = 30f)
        val origAvx = a.vx
        val origBvx = b.vx

        resolveCollision(a, b)

        // Position pushed apart but velocity not exchanged (dot <= 0)
        assertTrue(abs(a.vx - origAvx) < 0.01f, "Separating fish A velocity should not change")
        assertTrue(abs(b.vx - origBvx) < 0.01f, "Separating fish B velocity should not change")
    }

    // Use reflection to access the private method for unit testing
    private fun resolveCollision(a: PhysicsObject, b: PhysicsObject) {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val method = engine.javaClass.getDeclaredMethod(
            "resolveCollision",
            PhysicsObject::class.java,
            PhysicsObject::class.java,
        )
        method.isAccessible = true
        method.invoke(engine, a, b)
    }
}
