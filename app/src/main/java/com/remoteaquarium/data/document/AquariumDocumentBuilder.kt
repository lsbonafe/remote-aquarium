package com.remoteaquarium.data.document

import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.modifiers.RecordingModifier
import com.remoteaquarium.domain.model.AquariumDocument
import javax.inject.Inject

class AquariumDocumentBuilder @Inject constructor() {

    fun build(screenWidth: Float, screenHeight: Float): AquariumDocument {
        val w = screenWidth
        val h = screenHeight

        val ctx = RemoteComposeContext(
            w.toInt(), h.toInt(), "Interactive Aquarium",
            RcPlatformServices.None,
        ) {
            val accelX = RFloat(writer, addNamedFloat(SensorVariableRegistry.DOC_ACCEL_X, 0f))

            val fishPositions = (0 until SensorVariableRegistry.FISH_COUNT).map { i ->
                val fx = RFloat(writer, addNamedFloat(SensorVariableRegistry.docFishVar(i, "X"), w * 0.5f))
                val fy = RFloat(writer, addNamedFloat(SensorVariableRegistry.docFishVar(i, "Y"), h * 0.5f))
                fx to fy
            }

            val bubblePositions = (0 until SensorVariableRegistry.BUBBLE_COUNT).map { i ->
                val bx = RFloat(writer, addNamedFloat(SensorVariableRegistry.docBubbleVar(i, "X"), w * 0.5f))
                val by = RFloat(writer, addNamedFloat(SensorVariableRegistry.docBubbleVar(i, "Y"), h * 0.8f))
                bx to by
            }

            root {
                canvas(RecordingModifier().fillMaxSize()) {
                    val t = ContinuousSec()

                    WaterLayerBuilder.draw(this, w, h, t, accelX)
                    SandFloorBuilder.draw(this, w, h)
                    SeaweedBuilder.draw(this, w, h, t, accelX)
                    FishBuilder.draw(this, w, h, fishPositions)
                    BubbleBuilder.draw(this, w, h, bubblePositions)
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
