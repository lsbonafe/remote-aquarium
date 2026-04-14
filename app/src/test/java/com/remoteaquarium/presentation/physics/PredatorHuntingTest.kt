package com.remoteaquarium.presentation.physics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PredatorHuntingTest {

    @BeforeEach
    fun setup() {
        PredatorHunting.reset()
    }

    @Test
    fun `findNearestPrey returns closest alive non-predator fish`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f) // predator = 0

        val fish = listOf(
            PhysicsObject(x = 100f, y = 100f),
            PhysicsObject(x = 200f, y = 100f),
            PhysicsObject(x = 500f, y = 100f),
        )

        val preyIdx = PredatorHunting.findNearestPrey(fish[0], fish, sm)

        assertEquals(1, preyIdx, "Should find the closest prey")
    }

    @Test
    fun `findNearestPrey returns null when no prey alive`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)
        sm.markDead(1)
        sm.markDead(2)

        val fish = listOf(
            PhysicsObject(x = 100f, y = 100f),
            PhysicsObject(x = 200f, y = 100f),
            PhysicsObject(x = 500f, y = 100f),
        )

        val preyIdx = PredatorHunting.findNearestPrey(fish[0], fish, sm)

        assertNull(preyIdx)
    }

    @Test
    fun `findNearestPrey skips dead fish`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)
        sm.markDead(1) // closest prey is dead

        val fish = listOf(
            PhysicsObject(x = 100f, y = 100f),
            PhysicsObject(x = 200f, y = 100f), // dead
            PhysicsObject(x = 500f, y = 100f), // alive
        )

        val preyIdx = PredatorHunting.findNearestPrey(fish[0], fish, sm)

        assertEquals(2, preyIdx, "Should skip dead fish and find next closest")
    }

    @Test
    fun `applyAttraction pulls predator toward prey`() {
        val predator = PhysicsObject(x = 100f, y = 100f)
        val prey = PhysicsObject(x = 500f, y = 100f)

        PredatorHunting.applyAttraction(predator, prey, 0.016f)

        assertTrue(predator.vx > 0f, "Predator should be pulled toward prey, vx=${predator.vx}")
    }

    @Test
    fun `checkPreyEating returns prey index when within eat distance`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)

        val fish = listOf(
            PhysicsObject(x = 100f, y = 100f, radius = 52f),
            PhysicsObject(x = 130f, y = 100f, radius = 22f), // very close
            PhysicsObject(x = 500f, y = 100f, radius = 22f),
        )

        val swallowTimers = floatArrayOf(-1f, -1f, -1f)
        val eaten = PredatorHunting.checkPreyEating(fish, sm, swallowTimers)

        assertTrue(eaten.contains(1), "Fish 1 should be eaten")
    }

    @Test
    fun `checkPreyEating returns empty when prey is far`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)

        val fish = listOf(
            PhysicsObject(x = 100f, y = 100f, radius = 52f),
            PhysicsObject(x = 900f, y = 900f, radius = 22f),
            PhysicsObject(x = 500f, y = 500f, radius = 22f),
        )

        val swallowTimers = floatArrayOf(-1f, -1f, -1f)
        val eaten = PredatorHunting.checkPreyEating(fish, sm, swallowTimers)

        assertTrue(eaten.isEmpty(), "No fish should be eaten when far away")
    }

    @Test
    fun `checkPreyEating skips fish being swallowed`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)

        val fish = listOf(
            PhysicsObject(x = 100f, y = 100f, radius = 52f),
            PhysicsObject(x = 130f, y = 100f, radius = 22f), // close but swallowing
            PhysicsObject(x = 500f, y = 100f, radius = 22f),
        )

        val swallowTimers = floatArrayOf(-1f, 0.2f, -1f) // fish 1 is being swallowed
        val eaten = PredatorHunting.checkPreyEating(fish, sm, swallowTimers)

        assertFalse(eaten.contains(1), "Swallowing fish should be skipped")
    }

    @Test
    fun `cooldown blocks eating after a kill`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)

        val fish = listOf(
            PhysicsObject(x = 100f, y = 100f, radius = 52f),
            PhysicsObject(x = 130f, y = 100f, radius = 22f),
            PhysicsObject(x = 500f, y = 100f, radius = 22f),
        )

        // First eat triggers cooldown
        val swallowTimers = floatArrayOf(-1f, -1f, -1f)
        val firstEat = PredatorHunting.checkPreyEating(fish, sm, swallowTimers)
        assertTrue(firstEat.isNotEmpty(), "First eat should succeed")

        // Immediate second eat should be blocked by cooldown
        val secondEat = PredatorHunting.checkPreyEating(fish, sm, swallowTimers)
        assertTrue(secondEat.isEmpty(), "Cooldown should block immediate re-eating")
    }

    @Test
    fun `cooldown expires and allows eating again`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)

        val fish = listOf(
            PhysicsObject(x = 100f, y = 100f, radius = 52f),
            PhysicsObject(x = 130f, y = 100f, radius = 22f),
            PhysicsObject(x = 500f, y = 100f, radius = 22f),
        )

        val swallowTimers = floatArrayOf(-1f, -1f, -1f)
        PredatorHunting.checkPreyEating(fish, sm, swallowTimers) // triggers cooldown

        // Tick cooldown past duration
        PredatorHunting.updateCooldown(1f)

        val afterCooldown = PredatorHunting.checkPreyEating(fish, sm, swallowTimers)
        assertTrue(afterCooldown.isNotEmpty(), "Should eat again after cooldown expires")
    }
}
