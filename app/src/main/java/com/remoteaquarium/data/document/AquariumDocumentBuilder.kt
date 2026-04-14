package com.remoteaquarium.data.document

import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.modifiers.RecordingModifier
import com.remoteaquarium.domain.model.AquariumDocument
import javax.inject.Inject

class AquariumDocumentBuilder @Inject constructor() {

    companion object {
        const val FEED_ACTION_ID = 1
    }

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

            val fishAngles = (0 until SensorVariableRegistry.FISH_COUNT).map { i ->
                val cosA = RFloat(writer, addNamedFloat(SensorVariableRegistry.docFishAngleVar(i, "C"), 1f))
                val sinA = RFloat(writer, addNamedFloat(SensorVariableRegistry.docFishAngleVar(i, "S"), 0f))
                cosA to sinA
            }

            val bubblePositions = (0 until SensorVariableRegistry.BUBBLE_COUNT).map { i ->
                val bx = RFloat(writer, addNamedFloat(SensorVariableRegistry.docBubbleVar(i, "X"), w * 0.5f))
                val by = RFloat(writer, addNamedFloat(SensorVariableRegistry.docBubbleVar(i, "Y"), h * 0.8f))
                bx to by
            }

            // Food positions (off-screen by default — spawned on tap)
            val foodPositions = (0 until SensorVariableRegistry.FOOD_COUNT).map { i ->
                val fx = RFloat(writer, addNamedFloat(SensorVariableRegistry.docFoodVar(i, "X"), -100f))
                val fy = RFloat(writer, addNamedFloat(SensorVariableRegistry.docFoodVar(i, "Y"), -100f))
                fx to fy
            }

            root {
                canvas(RecordingModifier().fillMaxSize()) {
                    val t = ContinuousSec()

                    WaterLayerBuilder.draw(this, w, h, t, accelX)
                    SandFloorBuilder.draw(this, w, h)
                    SeaweedBuilder.draw(this, w, h, t, accelX)
                    FishBuilder.draw(this, w, h, fishPositions, fishAngles)
                    BubbleBuilder.draw(this, w, h, bubblePositions)
                    FoodBuilder.draw(this, w, h, foodPositions)
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
