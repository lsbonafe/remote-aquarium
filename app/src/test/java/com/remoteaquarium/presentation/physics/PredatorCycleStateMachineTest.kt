package com.remoteaquarium.presentation.physics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PredatorCycleStateMachineTest {

    @Test
    fun `starts in NORMAL state`() {
        val sm = PredatorCycleStateMachine(fishCount = 18)

        assertEquals(CycleState.NORMAL, sm.currentState)
    }

    @Test
    fun `enters PREDATOR when biggest fish is 1_5x the 2nd biggest`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)

        sm.update(scales, 0f)

        assertEquals(CycleState.PREDATOR, sm.currentState)
        assertEquals(0, sm.predatorIndex)
    }

    @Test
    fun `does NOT enter PREDATOR at 1_4x`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.4f, 1.0f, 1.0f)

        sm.update(scales, 0f)

        assertEquals(CycleState.NORMAL, sm.currentState)
    }

    @Test
    fun `predator index is the fish with highest scale`() {
        val sm = PredatorCycleStateMachine(fishCount = 4)
        val scales = floatArrayOf(1.0f, 1.0f, 5.0f, 1.0f)

        sm.update(scales, 0f)

        assertEquals(CycleState.PREDATOR, sm.currentState)
        assertEquals(2, sm.predatorIndex)
    }

    @Test
    fun `enters CROWN when only 1 fish alive`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f) // enter PREDATOR

        // Kill fish 1 and 2
        sm.markDead(1)
        sm.markDead(2)
        sm.update(scales, 1f)

        assertEquals(CycleState.CROWN, sm.currentState)
    }

    @Test
    fun `CROWN state records start time`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)
        sm.markDead(1)
        sm.markDead(2)
        sm.update(scales, 5.5f)

        assertEquals(5.5f, sm.crownStartTime)
    }

    @Test
    fun `enters RESET after 15 seconds in CROWN`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)
        sm.markDead(1)
        sm.markDead(2)
        sm.update(scales, 10f) // enter CROWN at t=10

        sm.update(scales, 25.1f) // 15.1s later

        assertEquals(CycleState.RESET, sm.currentState)
    }

    @Test
    fun `does NOT enter RESET before 15 seconds`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f)
        sm.markDead(1)
        sm.markDead(2)
        sm.update(scales, 10f) // enter CROWN at t=10

        sm.update(scales, 24.9f) // only 14.9s

        assertEquals(CycleState.CROWN, sm.currentState)
    }

    @Test
    fun `transitions to NORMAL when reset completes`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)
        val scales = floatArrayOf(1.5f, 1.0f, 1.0f)
        sm.update(scales, 0f) // PREDATOR
        sm.markDead(1)
        sm.markDead(2)
        sm.update(scales, 10f) // CROWN
        sm.update(scales, 25.1f) // RESET

        sm.completeReset()

        assertEquals(CycleState.NORMAL, sm.currentState)
        assertTrue(sm.isAlive(0))
        assertTrue(sm.isAlive(1))
        assertTrue(sm.isAlive(2))
    }

    @Test
    fun `needs at least 2 alive fish for predator activation`() {
        val sm = PredatorCycleStateMachine(fishCount = 2)
        val scales = floatArrayOf(5.0f, 1.0f)
        sm.update(scales, 0f)
        assertEquals(CycleState.PREDATOR, sm.currentState)

        // Now with only 1 fish alive from the start (edge case)
        val sm2 = PredatorCycleStateMachine(fishCount = 1)
        val scales2 = floatArrayOf(5.0f)
        sm2.update(scales2, 0f)
        assertEquals(CycleState.NORMAL, sm2.currentState)
    }

    @Test
    fun `isAlive returns correct state`() {
        val sm = PredatorCycleStateMachine(fishCount = 3)

        assertTrue(sm.isAlive(0))
        assertTrue(sm.isAlive(1))
        assertTrue(sm.isAlive(2))

        sm.markDead(1)
        assertTrue(sm.isAlive(0))
        assertFalse(sm.isAlive(1))
        assertTrue(sm.isAlive(2))
    }
}
