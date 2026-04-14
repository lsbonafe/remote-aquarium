package com.remoteaquarium.data.document

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SensorVariableRegistryTest {

    @Test
    fun `ACCEL_X constant has expected value`() {
        assertEquals("accelX", SensorVariableRegistry.ACCEL_X)
    }

    @Test
    fun `ACCEL_Y constant has expected value`() {
        assertEquals("accelY", SensorVariableRegistry.ACCEL_Y)
    }

    @Test
    fun `DOC_ACCEL_X has USER prefix`() {
        assertEquals("USER:accelX", SensorVariableRegistry.DOC_ACCEL_X)
    }

    @Test
    fun `NAMES contains player-side names`() {
        val names = SensorVariableRegistry.NAMES
        assertEquals(SensorVariableRegistry.ACCEL_X, names.accelX)
        assertEquals(SensorVariableRegistry.ACCEL_Y, names.accelY)
    }

    @Test
    fun `fishAngleVar generates correct key format`() {
        assertEquals("fish5AC", SensorVariableRegistry.fishAngleVar(5, "C"))
        assertEquals("fish0AS", SensorVariableRegistry.fishAngleVar(0, "S"))
    }

    @Test
    fun `docFishAngleVar includes USER prefix`() {
        assertEquals("USER:fish0AS", SensorVariableRegistry.docFishAngleVar(0, "S"))
        assertEquals("USER:fish17AC", SensorVariableRegistry.docFishAngleVar(17, "C"))
    }
}
