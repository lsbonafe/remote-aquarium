package com.remoteaquarium.presentation

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import app.cash.turbine.test
import com.remoteaquarium.domain.model.AquariumDocument
import com.remoteaquarium.domain.model.SensorData
import com.remoteaquarium.domain.model.SensorVariableNames
import com.remoteaquarium.domain.usecase.GetAquariumSceneUseCase
import com.remoteaquarium.presentation.sensor.SensorDataProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AquariumViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getAquariumScene = mockk<GetAquariumSceneUseCase>()
    private val sensorFlow = MutableStateFlow(SensorData())
    private val sensorDataProvider = mockk<SensorDataProvider>(relaxed = true) {
        every { sensorData } returns sensorFlow
    }
    private val displayMetrics = DisplayMetrics().apply {
        widthPixels = 1080
        heightPixels = 2400
    }
    private val resources = mockk<Resources> {
        every { this@mockk.displayMetrics } returns this@AquariumViewModelTest.displayMetrics
    }
    private val context = mockk<Context> {
        every { this@mockk.resources } returns this@AquariumViewModelTest.resources
    }

    private val fakeDocument = AquariumDocument(
        documentBytes = byteArrayOf(1, 2, 3),
        sensorVariableNames = SensorVariableNames(accelX = "accelX", accelY = "accelY"),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { getAquariumScene(any(), any()) } returns fakeDocument
        val viewModel = AquariumViewModel(getAquariumScene, sensorDataProvider, context)

        viewModel.uiState.test {
            assertEquals(AquariumUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state transitions to Ready after successful load`() = runTest {
        coEvery { getAquariumScene(any(), any()) } returns fakeDocument
        val viewModel = AquariumViewModel(getAquariumScene, sensorDataProvider, context)

        viewModel.uiState.test {
            assertEquals(AquariumUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(AquariumUiState.Ready(fakeDocument), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state transitions to Error on failure`() = runTest {
        coEvery { getAquariumScene(any(), any()) } throws RuntimeException("Test error")
        val viewModel = AquariumViewModel(getAquariumScene, sensorDataProvider, context)

        viewModel.uiState.test {
            assertEquals(AquariumUiState.Loading, awaitItem())
            advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is AquariumUiState.Error)
            assertEquals("Test error", (errorState as AquariumUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startSensors delegates to sensor provider`() = runTest {
        coEvery { getAquariumScene(any(), any()) } returns fakeDocument
        val viewModel = AquariumViewModel(getAquariumScene, sensorDataProvider, context)

        viewModel.startSensors()
        verify { sensorDataProvider.start() }
    }

    @Test
    fun `stopSensors delegates to sensor provider`() = runTest {
        coEvery { getAquariumScene(any(), any()) } returns fakeDocument
        val viewModel = AquariumViewModel(getAquariumScene, sensorDataProvider, context)

        viewModel.stopSensors()
        verify { sensorDataProvider.stop() }
    }
}
