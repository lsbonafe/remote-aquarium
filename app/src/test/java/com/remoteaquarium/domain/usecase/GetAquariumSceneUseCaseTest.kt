package com.remoteaquarium.domain.usecase

import com.remoteaquarium.domain.model.AquariumDocument
import com.remoteaquarium.domain.model.SensorVariableNames
import com.remoteaquarium.domain.repository.AquariumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetAquariumSceneUseCaseTest {

    private val repository = mockk<AquariumRepository>()
    private val useCase = GetAquariumSceneUseCase(repository)

    private val fakeDocument = AquariumDocument(
        documentBytes = byteArrayOf(1, 2, 3),
        sensorVariableNames = SensorVariableNames(accelX = "accelX", accelY = "accelY"),
    )

    @Test
    fun `invoke delegates to repository`() = runTest {
        coEvery { repository.getAquariumDocument(any(), any()) } returns fakeDocument

        val result = useCase(1080f, 2400f)

        assertEquals(fakeDocument, result)
        coVerify(exactly = 1) { repository.getAquariumDocument(1080f, 2400f) }
    }

    @Test
    fun `invoke propagates repository exceptions`() = runTest {
        coEvery { repository.getAquariumDocument(any(), any()) } throws RuntimeException("Network error")

        assertThrows<RuntimeException> {
            useCase(1080f, 2400f)
        }
    }
}
