package com.remoteaquarium.presentation.physics

import com.remoteaquarium.domain.model.SensorData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

class AquariumPhysicsEngineTest {

    // === Collision tests (using CollisionResolver directly) ===

    @Test
    fun `overlapping objects get pushed apart`() {
        val a = PhysicsObject(x = 100f, y = 100f, radius = 30f)
        val b = PhysicsObject(x = 120f, y = 100f, radius = 30f)

        CollisionResolver.resolve(a, b)

        val dist = sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y))
        assertTrue(dist >= 59f, "Should be pushed to at least minDist apart, got $dist")
    }

    @Test
    fun `distant objects are not affected`() {
        val a = PhysicsObject(x = 100f, y = 100f, radius = 30f)
        val b = PhysicsObject(x = 300f, y = 300f, radius = 30f)
        val origAx = a.x

        CollisionResolver.resolve(a, b)

        assertEquals(origAx, a.x, "Distant objects should not move")
    }

    @Test
    fun `approaching objects exchange velocity`() {
        val a = PhysicsObject(x = 100f, y = 100f, vx = 50f, vy = 0f, radius = 30f)
        val b = PhysicsObject(x = 140f, y = 100f, vx = -50f, vy = 0f, radius = 30f)

        CollisionResolver.resolve(a, b)

        assertTrue(a.vx < 50f, "Object A should slow down, got vx=${a.vx}")
        assertTrue(b.vx > -50f, "Object B should slow down, got vx=${b.vx}")
    }

    @Test
    fun `collision respects different radii`() {
        val big = PhysicsObject(x = 100f, y = 100f, radius = 50f)
        val small = PhysicsObject(x = 140f, y = 100f, radius = 20f)

        CollisionResolver.resolve(big, small)

        val dist = sqrt((small.x - big.x) * (small.x - big.x) + (small.y - big.y) * (small.y - big.y))
        assertTrue(dist >= 69f, "Should be pushed to at least minDist, got $dist")
    }

    @Test
    fun `separating objects dont exchange velocity`() {
        val a = PhysicsObject(x = 100f, y = 100f, vx = -50f, vy = 0f, radius = 30f)
        val b = PhysicsObject(x = 140f, y = 100f, vx = 50f, vy = 0f, radius = 30f)
        val origAvx = a.vx

        CollisionResolver.resolve(a, b)

        assertTrue(abs(a.vx - origAvx) < 0.01f, "Separating velocity should not change")
    }

    // === IdleDetector tests ===

    @Test
    fun `idle blend is zero when tilt changes`() {
        val detector = IdleDetector()

        val blend = detector.update(0.5f, 0.1f, 1f)

        assertEquals(0f, blend)
    }

    @Test
    fun `idle blend ramps up after threshold`() {
        val detector = IdleDetector(idleThresholdSec = 2f, blendRampSec = 1f)

        detector.update(0f, 0f, 0f)
        val blend = detector.update(0f, 0f, 3.5f)

        assertTrue(blend > 0f, "Should be blending after threshold")
        assertTrue(blend <= 1f, "Blend should not exceed 1")
    }

    @Test
    fun `tilt change resets idle timer`() {
        val detector = IdleDetector(idleThresholdSec = 2f)

        detector.update(0f, 0f, 0f)
        detector.update(0f, 0f, 3f) // would be idle
        detector.update(0.5f, 0f, 3.1f) // tilt change resets
        val blend = detector.update(0.5f, 0f, 4f) // only 0.9s since reset

        assertEquals(0f, blend, "Should not be idle after tilt change")
    }

    // === PhysicsWorld tests ===

    @Test
    fun `clampAndBounce keeps object within bounds`() {
        val world = PhysicsWorld(1080f, 2400f, margin = 40f)
        val obj = PhysicsObject(x = -10f, y = 3000f, vx = -100f, vy = 100f, restitution = 0.3f)

        world.clampAndBounce(obj)

        assertEquals(40f, obj.x)
        assertEquals(2360f, obj.y)
        assertTrue(obj.vx > 0, "Velocity should be reflected")
        assertTrue(obj.vy < 0, "Velocity should be reflected")
    }

    @Test
    fun `clampAndBounce does nothing for objects inside bounds`() {
        val world = PhysicsWorld(1080f, 2400f, margin = 40f)
        val obj = PhysicsObject(x = 500f, y = 1200f, vx = 50f, vy = -30f)

        world.clampAndBounce(obj)

        assertEquals(500f, obj.x)
        assertEquals(1200f, obj.y)
    }

    // === FoodManager tests ===

    @Test
    fun `spawn adds food at position`() {
        val world = PhysicsWorld(1080f, 2400f)
        val manager = FoodManager(world)

        manager.spawn(500f, 800f, 0f)

        assertTrue(manager.hasFood)
        assertEquals(1, manager.positions.size)
        assertEquals(500f, manager.positions[0].first)
    }

    @Test
    fun `spawn respects max limit`() {
        val world = PhysicsWorld(1080f, 2400f)
        val manager = FoodManager(world, maxFood = 3)

        repeat(10) { manager.spawn(100f * it, 100f, 0f) }

        assertEquals(3, manager.positions.size)
    }

    @Test
    fun `checkEating removes food near fish`() {
        val world = PhysicsWorld(1080f, 2400f)
        val manager = FoodManager(world, eatDistance = 50f)

        manager.spawn(100f, 100f, 0f)
        val fish = listOf(PhysicsObject(x = 100f, y = 100f, radius = 30f))

        manager.checkEating(fish)

        assertTrue(!manager.hasFood, "Food should be eaten")
    }

    @Test
    fun `checkEating does not remove distant food`() {
        val world = PhysicsWorld(1080f, 2400f)
        val manager = FoodManager(world, eatDistance = 50f)

        manager.spawn(100f, 100f, 0f)
        val fish = listOf(PhysicsObject(x = 900f, y = 900f, radius = 30f))

        manager.checkEating(fish)

        assertTrue(manager.hasFood, "Distant food should not be eaten")
    }

    // === Integration tests ===

    @Test
    fun `feed spawns food and fish eat it`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val sensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        engine.feed(324f, 600f)

        var state = engine.update(sensor)
        for (i in 0 until 100) {
            state = engine.update(sensor)
            Thread.sleep(16)
            if (state.food.isEmpty()) break
        }

        assertTrue(state.food.isEmpty(), "Fish should eat nearby food")
    }

    @Test
    fun `no food before feed is called`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val sensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        val state = engine.update(sensor)
        assertTrue(state.food.isEmpty())
    }

    @Test
    fun `idle swimming activates after stillness`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val sensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        var state = PhysicsState(emptyList(), emptyList(), emptyList(), emptyList())
        for (i in 0 until 500) {
            state = engine.update(sensor)
            Thread.sleep(15)
            if (i > 400) break
        }

        val initialX = 1080f * 0.3f
        val dx = abs(state.fish[0].first - initialX)
        assertTrue(dx > 0.1f || abs(state.fish[0].second - 2400f * 0.25f) > 0.1f,
            "Fish should move during idle")
    }

    // === Angle smoothing tests ===

    @Test
    fun `lerpAngle wraps correctly across PI boundary`() {
        val result = lerpAngle(3.0f, -3.0f, 0.5f)

        // Short arc from +3.0 to -3.0 goes through PI (distance ~0.28),
        // not the long way around through 0 (distance ~6.0)
        assertTrue(abs(result) > 2.5f, "Should take the short path past PI, got $result")
    }

    @Test
    fun `lerpAngle with zero factor returns current`() {
        val result = lerpAngle(1.5f, -1.5f, 0f)

        assertEquals(1.5f, result)
    }

    @Test
    fun `lerpAngle with factor 1 returns target via short path`() {
        val result = lerpAngle(1.5f, -1.5f, 1f)

        assertEquals(-1.5f, result, 0.001f)
    }

    @Test
    fun `fish faces toward food when chasing`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val sensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        // Keep spawning food far to the left so it persists long enough
        var state = engine.update(sensor)
        for (i in 0 until 200) {
            if (state.food.isEmpty()) engine.feed(10f, 600f)
            state = engine.update(sensor)
            Thread.sleep(16)
        }

        // At least some fish should be facing left (cosA < 0)
        val anyFacingLeft = state.fishAngles.any { (cosA, _) -> cosA < 0f }
        assertTrue(anyFacingLeft,
            "Some fish should face left toward food, angles: ${state.fishAngles.map { it.first }}")
    }

    @Test
    fun `fish settles to face left after chasing food to the left`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val sensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        // Keep spawning food to the left until fish turn
        var state = engine.update(sensor)
        for (i in 0 until 200) {
            if (state.food.isEmpty()) engine.feed(10f, 600f)
            state = engine.update(sensor)
            Thread.sleep(16)
        }

        // Stop spawning food, keep tilt active so idle doesn't kick in
        val activeSensor = SensorData(accelX = 0.3f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)
        for (i in 0 until 100) {
            state = engine.update(activeSensor)
            Thread.sleep(16)
        }

        // Fish that were leaning left should settle to fully face left (cosA ≈ -1)
        // Fish that were leaning right should settle to fully face right (cosA ≈ 1)
        // All fish should be facing cleanly left or right (abs(cosA) near 1, sinA near 0)
        for ((cosA, sinA) in state.fishAngles) {
            assertTrue(abs(cosA) > 0.7f,
                "Fish should face left or right, not diagonal. cosA=$cosA")
            assertTrue(abs(sinA) < 0.75f,
                "Fish should not face up/down. sinA=$sinA")
        }
    }

    @Test
    fun `fish facing direction depends on lean after food`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val sensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        // Spawn food to the LEFT — fish will lean left
        var state = engine.update(sensor)
        for (i in 0 until 200) {
            if (state.food.isEmpty()) engine.feed(10f, 600f)
            state = engine.update(sensor)
            Thread.sleep(16)
        }

        // Let food be eaten, run with active tilt
        val activeSensor = SensorData(accelX = 0.3f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)
        for (i in 0 until 100) {
            state = engine.update(activeSensor)
            Thread.sleep(16)
        }

        // Some fish should be facing left (those that were leaning left)
        val facingLeft = state.fishAngles.count { (cosA, _) -> cosA < -0.5f }
        assertTrue(facingLeft > 0, "Some fish should settle facing left after chasing left food")
    }

    @Test
    fun `fish reverts to facing right during idle swimming`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val sensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        // Feed and let fish chase to build up angles
        var state = engine.update(sensor)
        for (i in 0 until 200) {
            if (state.food.isEmpty()) engine.feed(10f, 600f)
            state = engine.update(sensor)
            Thread.sleep(16)
        }

        // Hold still long enough for idle to kick in (5s threshold + 2s ramp)
        for (i in 0 until 500) {
            state = engine.update(sensor)
            Thread.sleep(16)
        }

        // After idle swimming activates, all fish should revert to facing right
        for ((cosA, sinA) in state.fishAngles) {
            assertTrue(cosA > 0.8f, "Fish should face right during idle, cosA=$cosA")
            assertTrue(abs(sinA) < 0.6f, "Fish sinA should be near 0 during idle, sinA=$sinA")
        }
    }

    @Test
    fun `fish angle transitions smoothly not instantly`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val sensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        // First frame to initialize
        engine.update(sensor)

        // Spawn food far to the left — all fish start facing right (angle=0)
        engine.feed(10f, 1200f)

        // One frame only
        val state = engine.update(sensor)

        // Fish should NOT have snapped fully to facing left yet
        // cosA should still be positive (not fully turned) for most fish
        val mostStillFacingRight = state.fishAngles.count { (cosA, _) -> cosA > 0f }
        assertTrue(mostStillFacingRight > state.fishAngles.size / 2,
            "Most fish should still be partly facing right after one frame")
    }

    @Test
    fun `PhysicsState fishAngles has correct count and unit values`() {
        val engine = AquariumPhysicsEngine(1080f, 2400f)
        val sensor = SensorData(accelX = 0f, accelY = AquariumPhysicsEngine.REST_ACCEL_Y)

        val state = engine.update(sensor)

        assertEquals(18, state.fishAngles.size)
        for ((cosA, sinA) in state.fishAngles) {
            val magnitude = cosA * cosA + sinA * sinA
            assertEquals(1f, magnitude, 0.01f, "cos²+sin² should be ~1, got $magnitude")
        }
    }
}
