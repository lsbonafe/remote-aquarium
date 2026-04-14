package com.remoteaquarium.presentation.physics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResetAnimationTest {

    @Test
    fun `crown drops downward after reset starts`() {
        val reset = ResetAnimation()
        reset.start(
            startTime = 10f,
            predatorIndex = 0,
            predatorScale = 4f,
            deadFishIndices = listOf(1, 2),
            worldWidth = 1080f,
            worldHeight = 2400f,
        )

        val crownState = reset.crownState(11f) // 1 second into reset

        assertTrue(crownState.dropOffsetY > 0f, "Crown should drop downward, offsetY=${crownState.dropOffsetY}")
    }

    @Test
    fun `predator shrinks toward scale 1 over time`() {
        val reset = ResetAnimation()
        reset.start(10f, 0, 4f, listOf(1, 2), 1080f, 2400f)

        val scale = reset.predatorScale(12f) // 2 seconds in

        assertTrue(scale < 4f, "Predator should be shrinking, scale=$scale")
        assertTrue(scale >= 1f, "Should not shrink below 1.0, scale=$scale")
    }

    @Test
    fun `dead fish get edge respawn positions`() {
        val reset = ResetAnimation()
        reset.start(10f, 0, 4f, listOf(1, 2), 1080f, 2400f)

        val respawns = reset.respawnPositions

        assertEquals(2, respawns.size)
        for ((x, y) in respawns.values) {
            val onEdge = x <= 0f || x >= 1080f || y <= 0f || y >= 2400f
            assertTrue(onEdge, "Respawn should be on edge, pos=($x, $y)")
        }
    }

    @Test
    fun `update returns false while animations in progress`() {
        val reset = ResetAnimation()
        reset.start(10f, 0, 4f, listOf(1, 2), 1080f, 2400f)

        val done = reset.isComplete(10.5f)

        assertFalse(done, "Should not be complete after 0.5s")
    }

    @Test
    fun `update returns true when all animations complete`() {
        val reset = ResetAnimation()
        reset.start(10f, 0, 4f, listOf(1, 2), 1080f, 2400f)

        val done = reset.isComplete(13.5f) // 3.5s — well past all durations

        assertTrue(done, "Should be complete after 3.5s")
    }
}
