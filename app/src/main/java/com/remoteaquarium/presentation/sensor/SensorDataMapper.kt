package com.remoteaquarium.presentation.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import com.remoteaquarium.domain.model.SensorData
import javax.inject.Inject

class SensorDataMapper @Inject constructor() {

    fun map(event: SensorEvent, current: SensorData): SensorData {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return current
        return mapAccelerometer(event.values[0], event.values[1], current)
    }

    fun mapAccelerometer(rawX: Float, rawY: Float, current: SensorData): SensorData {
        val normalizedX = (rawX / GRAVITY).coerceIn(-1f, 1f)
        val normalizedY = (rawY / GRAVITY).coerceIn(-1f, 1f)

        // Low-pass filter for smooth movement (EMA with alpha=0.1)
        val smoothX = current.accelX + SMOOTHING * (normalizedX - current.accelX)
        val smoothY = current.accelY + SMOOTHING * (normalizedY - current.accelY)

        return SensorData(accelX = smoothX, accelY = smoothY)
    }

    companion object {
        const val GRAVITY = 9.81f
        const val SMOOTHING = 0.08f
    }
}
