package com.remoteaquarium.data.datasource

import com.remoteaquarium.data.document.AquariumDocumentBuilder
import com.remoteaquarium.data.document.SensorVariableRegistry
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MockAquariumDataSourceTest {

    private val builder = AquariumDocumentBuilder()
    private val dataSource = MockAquariumDataSource(builder)

    @Test
    fun `fetchAquariumDocument returns document from builder`() = runTest {
        val document = dataSource.fetchAquariumDocument()

        assertNotNull(document)
        assertNotNull(document.documentBytes)
    }

    @Test
    fun `fetchAquariumDocument returns correct sensor variable names`() = runTest {
        val document = dataSource.fetchAquariumDocument()

        assertEquals(SensorVariableRegistry.NAMES, document.sensorVariableNames)
    }
}
