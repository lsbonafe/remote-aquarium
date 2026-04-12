package com.remoteaquarium.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remoteaquarium.domain.model.SensorData
import com.remoteaquarium.domain.usecase.GetAquariumSceneUseCase
import com.remoteaquarium.presentation.physics.AquariumPhysicsEngine
import com.remoteaquarium.presentation.physics.PhysicsState
import com.remoteaquarium.presentation.sensor.SensorDataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AquariumViewModel @Inject constructor(
    private val getAquariumScene: GetAquariumSceneUseCase,
    private val sensorDataProvider: SensorDataProvider,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AquariumUiState>(AquariumUiState.Loading)
    val uiState: StateFlow<AquariumUiState> = _uiState.asStateFlow()

    val sensorData: Flow<SensorData> = sensorDataProvider.sensorData

    private val displayMetrics = context.resources.displayMetrics
    private val screenWidth = displayMetrics.widthPixels.toFloat()
    private val screenHeight = displayMetrics.heightPixels.toFloat()

    private val physicsEngine = AquariumPhysicsEngine(
        width = screenWidth,
        height = screenHeight,
    )

    val physicsState: Flow<PhysicsState> = sensorDataProvider.sensorData.map { sensor ->
        physicsEngine.update(sensor)
    }

    init {
        loadScene()
    }

    private fun loadScene() {
        viewModelScope.launch {
            try {
                val document = getAquariumScene(screenWidth, screenHeight)
                _uiState.value = AquariumUiState.Ready(document)
            } catch (e: Exception) {
                _uiState.value = AquariumUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun feed(x: Float, y: Float) {
        physicsEngine.feed(x, y)
    }

    fun startSensors() {
        sensorDataProvider.start()
    }

    fun stopSensors() {
        sensorDataProvider.stop()
    }
}
