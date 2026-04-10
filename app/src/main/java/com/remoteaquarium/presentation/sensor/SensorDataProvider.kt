package com.remoteaquarium.presentation.sensor

import com.remoteaquarium.domain.model.SensorData
import kotlinx.coroutines.flow.Flow

interface SensorDataProvider {
    val sensorData: Flow<SensorData>
    fun start()
    fun stop()
}
