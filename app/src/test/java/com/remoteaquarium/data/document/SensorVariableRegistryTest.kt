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
    fun `DOC constants have USER prefix`() {
        assertTrue(SensorVariableRegistry.DOC_ACCEL_X.startsWith("USER:"))
        assertTrue(SensorVariableRegistry.DOC_ACCEL_Y.startsWith("USER:"))
        assertEquals("USER:accelX", SensorVariableRegistry.DOC_ACCEL_X)
        assertEquals("USER:accelY", SensorVariableRegistry.DOC_ACCEL_Y)
    }

    @Test
    fun `NAMES contains player-side names without prefix`() {
        val names = SensorVariableRegistry.NAMES
        assertEquals(SensorVariableRegistry.ACCEL_X, names.accelX)
        assertEquals(SensorVariableRegistry.ACCEL_Y, names.accelY)
    }
}
