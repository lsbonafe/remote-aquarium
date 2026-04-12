package com.remoteaquarium.data.document

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AquariumDocumentBuilderTest {

    private val builder = AquariumDocumentBuilder()

    @Test
    fun `build returns non-empty byte array`() {
        val document = builder.build(1080f, 2400f)
        assertTrue(document.documentBytes.isNotEmpty())
    }

    @Test
    fun `build returns correct sensor variable names`() {
        val document = builder.build(1080f, 2400f)
        assertNotNull(document.sensorVariableNames)
        assertTrue(document.sensorVariableNames.accelX == SensorVariableRegistry.ACCEL_X)
        assertTrue(document.sensorVariableNames.accelY == SensorVariableRegistry.ACCEL_Y)
    }

    @Test
    fun `build produces valid document bytes that can be loaded`() {
        val document = builder.build(1080f, 2400f)
        val remoteDoc = androidx.compose.remote.player.core.RemoteDocument(document.documentBytes)
        assertNotNull(remoteDoc.document)
    }

    @Test
    fun `build registers named floats for sensor variables`() {
        val document = builder.build(1080f, 2400f)
        val remoteDoc = androidx.compose.remote.player.core.RemoteDocument(document.documentBytes)
        val namedFloats = remoteDoc.getNamedVariables(0)
        assertNotNull(namedFloats)
    }

    @Test
    fun `document operation count is within limit`() {
        val document = builder.build(1080f, 2400f)
        val remoteDoc = androidx.compose.remote.player.core.RemoteDocument(document.documentBytes)
        val opCount = remoteDoc.document.operations.size
        assertTrue(opCount < 20000, "Operation count $opCount exceeds MAX_OP_COUNT of 20000")
    }
}
