package com.remoteaquarium.data.repository

import com.remoteaquarium.data.datasource.AquariumDataSource
import com.remoteaquarium.domain.model.AquariumDocument
import com.remoteaquarium.domain.model.SensorVariableNames
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AquariumRepositoryImplTest {

    private val dataSource = mockk<AquariumDataSource>()
    private val repository = AquariumRepositoryImpl(dataSource)

    private val fakeDocument = AquariumDocument(
        documentBytes = byteArrayOf(1, 2, 3),
        sensorVariableNames = SensorVariableNames(accelX = "accelX", accelY = "accelY"),
    )

    @Test
    fun `getAquariumDocument delegates to data source`() = runTest {
        coEvery { dataSource.fetchAquariumDocument(any(), any()) } returns fakeDocument

        val result = repository.getAquariumDocument(1080f, 2400f)

        assertEquals(fakeDocument, result)
        coVerify(exactly = 1) { dataSource.fetchAquariumDocument(1080f, 2400f) }
    }

    @Test
    fun `getAquariumDocument propagates data source exceptions`() = runTest {
        coEvery { dataSource.fetchAquariumDocument(any(), any()) } throws RuntimeException("Fetch failed")

        assertThrows<RuntimeException> {
            repository.getAquariumDocument(1080f, 2400f)
        }
    }
}
