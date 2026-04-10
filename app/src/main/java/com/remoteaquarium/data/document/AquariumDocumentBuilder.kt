package com.remoteaquarium.data.document

import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.modifiers.RecordingModifier
import com.remoteaquarium.domain.model.AquariumDocument
import javax.inject.Inject

class AquariumDocumentBuilder @Inject constructor() {

    companion object {
        const val W = 1080f
        const val H = 2400f
    }

    fun build(): AquariumDocument {
        val ctx = RemoteComposeContext(
            W.toInt(), H.toInt(), "Interactive Aquarium",
            RcPlatformServices.None,
        ) {
            val accelXRef = addNamedFloat(SensorVariableRegistry.DOC_ACCEL_X, 0f)
            val accelYRef = addNamedFloat(SensorVariableRegistry.DOC_ACCEL_Y, 0f)
            val accelX = RFloat(writer, accelXRef)
            val accelY = RFloat(writer, accelYRef)

            root {
                canvas(RecordingModifier().fillMaxSize()) {
                    val t = ContinuousSec()

                    WaterLayerBuilder.draw(this, W, H, t, accelX)
                    SandFloorBuilder.draw(this, W, H)
                    SeaweedBuilder.draw(this, W, H, t, accelX)
                    FishBuilder.draw(this, W, H, t, accelX, accelY)
                    BubbleBuilder.draw(this, W, H, t)
                }
            }
        }

        val fullBuffer = ctx.buffer()
        val actualSize = ctx.bufferSize()

        return AquariumDocument(
            documentBytes = fullBuffer.copyOf(actualSize),
            sensorVariableNames = SensorVariableRegistry.NAMES,
        )
    }
}
