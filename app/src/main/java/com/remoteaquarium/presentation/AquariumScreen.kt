package com.remoteaquarium.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.remote.player.core.RemoteDocument
import androidx.compose.remote.player.view.RemoteComposePlayer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.remoteaquarium.domain.model.AquariumDocument
import com.remoteaquarium.domain.model.SensorData

@Composable
fun AquariumScreen(
    viewModel: AquariumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sensorData by viewModel.sensorData.collectAsStateWithLifecycle(
        initialValue = SensorData()
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
) {
    val remoteDoc = remember(document.documentBytes) {
        RemoteDocument(document.documentBytes)
    }

    AndroidView(
        factory = { context ->
            RemoteComposePlayer(context).apply {
                setDocument(remoteDoc)
            }
        },
        update = { player ->
            player.setUserLocalFloat(document.sensorVariableNames.accelX, sensorData.accelX)
            player.setUserLocalFloat(document.sensorVariableNames.accelY, sensorData.accelY)
            player.invalidate()
        },
        modifier = Modifier.fillMaxSize(),
    )
}
