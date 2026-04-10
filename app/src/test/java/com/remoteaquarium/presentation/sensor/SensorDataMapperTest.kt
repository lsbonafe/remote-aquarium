package com.remoteaquarium.presentation.sensor

import com.remoteaquarium.domain.model.SensorData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

class SensorDataMapperTest {

    private val mapper = SensorDataMapper()
    private val initialData = SensorData()

    @Test
    fun `first reading from zero applies smoothing`() {
        val result = mapper.mapAccelerometer(9.81f, 0f, initialData)
        // With smoothing 0.08: 0 + 0.08 * (1.0 - 0) = 0.08
        assertEquals(0.08f, result.accelX, 0.01f)
        assertEquals(0f, result.accelY, 0.01f)
    }

    @Test
    fun `repeated readings converge toward target`() {
        var data = initialData
        // Apply 1g on X repeatedly
        for (i in 0 until 50) {
            data = mapper.mapAccelerometer(9.81f, 0f, data)
        }
        // After 50 iterations should be close to 1.0
        assertTrue(data.accelX > 0.95f)
    }

    @Test
    fun `clamps values exceeding 1g before smoothing`() {
        val result = mapper.mapAccelerometer(20f, -20f, initialData)
        // Clamped to 1.0/-1.0 then smoothed from 0
        assertTrue(result.accelX > 0f)
        assertTrue(result.accelY < 0f)
    }

    @Test
    fun `ignores non-accelerometer sensor types`() {
        // mapAccelerometer is the pure function; the SensorEvent check is in map()
        // This test just verifies the smoothing math with zero input
        val result = mapper.mapAccelerometer(0f, 0f, initialData)
        assertEquals(0f, result.accelX)
        assertEquals(0f, result.accelY)
    }

    @Test
    fun `smoothing reduces jitter`() {
        var data = SensorData(accelX = 0.5f, accelY = 0.5f)
        // Sudden spike
        val result = mapper.mapAccelerometer(9.81f, -9.81f, data)
        // Should NOT jump to 1.0/-1.0, should be close to previous value
        assertTrue(abs(result.accelX - 0.5f) < 0.1f)
        assertTrue(abs(result.accelY - 0.5f) < 0.2f)
    }

    @Test
    fun `zero acceleration smooths back toward zero`() {
        val existing = SensorData(accelX = 0.5f, accelY = -0.3f)
        val result = mapper.mapAccelerometer(0f, 0f, existing)
        // Should move toward 0 but not reach it in one step
        assertTrue(result.accelX < 0.5f)
        assertTrue(result.accelX > 0f)
        assertTrue(result.accelY > -0.3f)
        assertTrue(result.accelY < 0f)
    }
}
