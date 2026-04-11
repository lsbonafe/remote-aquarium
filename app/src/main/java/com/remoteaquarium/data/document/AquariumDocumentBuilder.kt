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
            val accelX = RFloat(writer, addNamedFloat(SensorVariableRegistry.DOC_ACCEL_X, 0f))
            val accelY = RFloat(writer, addNamedFloat(SensorVariableRegistry.DOC_ACCEL_Y, 0f))

            // Register fish position variables
            val fishPositions = (0 until SensorVariableRegistry.FISH_COUNT).map { i ->
                val fx = RFloat(writer, addNamedFloat(SensorVariableRegistry.docFishVar(i, "X"), W * 0.5f))
                val fy = RFloat(writer, addNamedFloat(SensorVariableRegistry.docFishVar(i, "Y"), H * 0.5f))
                fx to fy
            }

            // Register bubble position variables
            val bubblePositions = (0 until SensorVariableRegistry.BUBBLE_COUNT).map { i ->
                val bx = RFloat(writer, addNamedFloat(SensorVariableRegistry.docBubbleVar(i, "X"), W * 0.5f))
                val by = RFloat(writer, addNamedFloat(SensorVariableRegistry.docBubbleVar(i, "Y"), H * 0.8f))
                bx to by
            }

            root {
                canvas(RecordingModifier().fillMaxSize()) {
                    val t = ContinuousSec()

                    WaterLayerBuilder.draw(this, W, H, t, accelX)
                    SandFloorBuilder.draw(this, W, H)
                    SeaweedBuilder.draw(this, W, H, t, accelX)
                    FishBuilder.draw(this, W, H, fishPositions)
                    BubbleBuilder.draw(this, W, H, bubblePositions)
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
