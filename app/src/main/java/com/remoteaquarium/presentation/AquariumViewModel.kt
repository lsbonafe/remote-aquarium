package com.remoteaquarium.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remoteaquarium.domain.model.SensorData
import com.remoteaquarium.domain.usecase.GetAquariumSceneUseCase
import com.remoteaquarium.presentation.sensor.SensorDataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AquariumViewModel @Inject constructor(
    private val getAquariumScene: GetAquariumSceneUseCase,
    private val sensorDataProvider: SensorDataProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AquariumUiState>(AquariumUiState.Loading)
    val uiState: StateFlow<AquariumUiState> = _uiState.asStateFlow()

    val sensorData: Flow<SensorData> = sensorDataProvider.sensorData

    init {
        loadScene()
    }

    private fun loadScene() {
        viewModelScope.launch {
            try {
                val document = getAquariumScene()
                _uiState.value = AquariumUiState.Ready(document)
            } catch (e: Exception) {
                _uiState.value = AquariumUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun startSensors() {
        sensorDataProvider.start()
    }

    fun stopSensors() {
        sensorDataProvider.stop()
    }
}
