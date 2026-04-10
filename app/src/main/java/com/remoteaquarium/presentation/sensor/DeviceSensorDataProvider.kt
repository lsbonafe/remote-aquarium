package com.remoteaquarium.presentation.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.remoteaquarium.domain.model.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class DeviceSensorDataProvider @Inject constructor(
    private val sensorManager: SensorManager,
    private val mapper: SensorDataMapper,
) : SensorDataProvider {

    private val _sensorData = MutableStateFlow(SensorData())
    override val sensorData: Flow<SensorData> = _sensorData.asStateFlow()

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            _sensorData.value = mapper.map(event, _sensorData.value)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun start() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(listener)
    }
}
