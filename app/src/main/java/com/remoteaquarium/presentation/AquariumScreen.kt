package com.remoteaquarium.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.remote.player.core.RemoteDocument
import androidx.compose.remote.player.view.RemoteComposePlayer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.remoteaquarium.data.document.SensorVariableRegistry
import com.remoteaquarium.domain.model.AquariumDocument
import com.remoteaquarium.domain.model.SensorData
import com.remoteaquarium.presentation.physics.PhysicsState

@Composable
fun AquariumScreen(
    viewModel: AquariumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sensorData by viewModel.sensorData.collectAsStateWithLifecycle(
        initialValue = SensorData()
    )
    val physicsState by viewModel.physicsState.collectAsStateWithLifecycle(
        initialValue = PhysicsState(
            fish = List(SensorVariableRegistry.FISH_COUNT) { 540f to 1200f },
            fishAngles = List(SensorVariableRegistry.FISH_COUNT) { 1f to 0f },
            fishMouthOpen = List(SensorVariableRegistry.FISH_COUNT) { 0f },
            fishScale = List(SensorVariableRegistry.FISH_COUNT) { 1f },
            bubbles = List(SensorVariableRegistry.BUBBLE_COUNT) { 540f to 1920f },
            food = emptyList(),
        )
    )

    DisposableEffect(Unit) {
        viewModel.startSensors()
        onDispose { viewModel.stopSensors() }
    }

    when (val state = uiState) {
        is AquariumUiState.Loading -> AquariumLoadingScreen()
        is AquariumUiState.Error -> AquariumErrorScreen(state.message)
        is AquariumUiState.Ready -> AquariumPlayer(
            document = state.aquariumDocument,
            sensorData = sensorData,
            physicsState = physicsState,
            onFeed = { x, y -> viewModel.feed(x, y) },
        )
    }
}

@Composable
private fun AquariumLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44)),
        contentAlignment = Alignment.Center,
    ) {
        Text("Loading aquarium...", color = Color.White)
    }
}

@Composable
private fun AquariumErrorScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44)),
        contentAlignment = Alignment.Center,
    ) {
        Text("Error: $message", color = Color.Red)
    }
}

@Composable
private fun AquariumPlayer(
    document: AquariumDocument,
    sensorData: SensorData,
    physicsState: PhysicsState,
    onFeed: (Float, Float) -> Unit,
) {
    val remoteDoc = remember(document.documentBytes) {
        RemoteDocument(document.documentBytes)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                RemoteComposePlayer(context).apply {
                    setBackgroundColor(android.graphics.Color.parseColor("#0A0A2E"))
                    setDocument(remoteDoc)
                }
            },
            update = { player ->
                player.setUserLocalFloat(SensorVariableRegistry.ACCEL_X, sensorData.accelX)

                for (i in physicsState.fish.indices) {
                    val (x, y) = physicsState.fish[i]
                    player.setUserLocalFloat(SensorVariableRegistry.fishVar(i, "X"), x)
                    player.setUserLocalFloat(SensorVariableRegistry.fishVar(i, "Y"), y)
                }

                for (i in physicsState.fishAngles.indices) {
                    val (cosA, sinA) = physicsState.fishAngles[i]
                    player.setUserLocalFloat(SensorVariableRegistry.fishAngleVar(i, "C"), cosA)
                    player.setUserLocalFloat(SensorVariableRegistry.fishAngleVar(i, "S"), sinA)
                }

                for (i in physicsState.fishMouthOpen.indices) {
                    player.setUserLocalFloat(SensorVariableRegistry.fishMouthVar(i), physicsState.fishMouthOpen[i])
                }

                for (i in physicsState.fishScale.indices) {
                    player.setUserLocalFloat(SensorVariableRegistry.fishScaleVar(i), physicsState.fishScale[i])
                }

                for (i in physicsState.bubbles.indices) {
                    val (x, y) = physicsState.bubbles[i]
                    player.setUserLocalFloat(SensorVariableRegistry.bubbleVar(i, "X"), x)
                    player.setUserLocalFloat(SensorVariableRegistry.bubbleVar(i, "Y"), y)
                }

                for (i in 0 until SensorVariableRegistry.FOOD_COUNT) {
                    if (i < physicsState.food.size) {
                        val (x, y) = physicsState.food[i]
                        player.setUserLocalFloat(SensorVariableRegistry.foodVar(i, "X"), x)
                        player.setUserLocalFloat(SensorVariableRegistry.foodVar(i, "Y"), y)
                    } else {
                        player.setUserLocalFloat(SensorVariableRegistry.foodVar(i, "X"), -100f)
                        player.setUserLocalFloat(SensorVariableRegistry.foodVar(i, "Y"), -100f)
                    }
                }

                // Crown
                player.setUserLocalFloat(SensorVariableRegistry.CROWN_X, physicsState.crownX)
                player.setUserLocalFloat(SensorVariableRegistry.CROWN_Y, physicsState.crownY)
                player.setUserLocalFloat(SensorVariableRegistry.CROWN_COS, physicsState.crownCos)
                player.setUserLocalFloat(SensorVariableRegistry.CROWN_SIN, physicsState.crownSin)
                player.setUserLocalFloat(SensorVariableRegistry.CROWN_SCALE, physicsState.crownScale)

                player.invalidate()
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Transparent tap interceptor on top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onFeed(offset.x, offset.y)
                    }
                }
        )
    }
}
