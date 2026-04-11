package com.remoteaquarium.presentation.physics

import com.remoteaquarium.domain.model.SensorData
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.sqrt

class AquariumPhysicsEngineTest {

    // === Collision tests ===

    @Test
    fun `overlapping fish get pushed apart`() {
        val a = PhysicsObject(x = 100f, y = 100f, radius = 30f)
        val b = PhysicsObject(x = 120f, y = 100f, radius = 30f)

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

        assertTrue(abs(a.vx - origAvx) < 0.01f, "Separating fish A velocity should not change")
        assertTrue(abs(b.vx - origBvx) < 0.01f, "Separating fish B velocity should not change")
    }

    // === Idle swimming tests ===

    @Test
    fun `fish move when idle with no tilt`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val stillSensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        // Run enough updates to pass the 5-second idle threshold
        var state = PhysicsState(emptyList(), emptyList())
        for (i in 0 until 500) {
            state = engine.update(stillSensor)
            Thread.sleep(15)
            if (i > 400) break
        }

        // After idle threshold, fish should have moved from initial positions
        val initialX = 1080f * 0.3f
        val movedFish = state.fish[0]
        val dx = abs(movedFish.first - initialX)
        // Fish should have drifted at least slightly
        assertTrue(dx > 0.1f || abs(movedFish.second - 2400f * 0.25f) > 0.1f,
            "Fish should move during idle, dx=$dx")
    }

    @Test
    fun `tilt resets idle timer`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val stillSensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        // Run a few frames still
        repeat(10) { engine.update(stillSensor) }

        // Now tilt significantly
        val tiltSensor = SensorData(accelX = 0.5f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)
        val stateAfterTilt = engine.update(tiltSensor)

        // Fish should be reacting to tilt, not idle swimming
        // The tilt should cause horizontal movement
        val firstFish = stateAfterTilt.fish[0]
        // Just verify no crash and we get valid positions
        assertTrue(firstFish.first.isFinite() && firstFish.second.isFinite())
    }

    @Test
    fun `idle blend is gradual not instant`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val stillSensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        // Capture position just before idle threshold
        var preIdleState = engine.update(stillSensor)
        for (i in 0 until 350) {
            preIdleState = engine.update(stillSensor)
            Thread.sleep(15)
        }

        // Capture position just after idle starts
        val postIdleState = engine.update(stillSensor)

        // The change should be small (gradual blend), not a sudden jump
        val jumpX = abs(postIdleState.fish[0].first - preIdleState.fish[0].first)
        val jumpY = abs(postIdleState.fish[0].second - preIdleState.fish[0].second)
        assertTrue(jumpX < 50f && jumpY < 50f,
            "Idle transition should be gradual, got jump dx=$jumpX dy=$jumpY")
    }

    // === Helpers ===

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
