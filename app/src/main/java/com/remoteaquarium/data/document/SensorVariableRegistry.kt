package com.remoteaquarium.data.document

import com.remoteaquarium.domain.model.SensorVariableNames

object SensorVariableRegistry {
    private const val USER_PREFIX = "USER:"

    const val ACCEL_X = "accelX"
    const val ACCEL_Y = "accelY"

    const val DOC_ACCEL_X = "${USER_PREFIX}${ACCEL_X}"

    // Fish positions (6 fish x 2 coords)
    fun fishVar(index: Int, axis: String) = "fish${index}$axis"
    fun docFishVar(index: Int, axis: String) = "${USER_PREFIX}fish${index}$axis"

    // Bubble positions (6 bubbles x 2 coords)
    fun bubbleVar(index: Int, axis: String) = "bubble${index}$axis"
    fun docBubbleVar(index: Int, axis: String) = "${USER_PREFIX}bubble${index}$axis"

    // Fish rotation (18 fish x 2 values: cos, sin)
    fun fishAngleVar(index: Int, component: String) = "fish${index}A$component"
    fun docFishAngleVar(index: Int, component: String) = "${USER_PREFIX}fish${index}A$component"

    // Fish mouth (18 fish x 1 value: 0.0 closed to 1.0 open)
    fun fishMouthVar(index: Int) = "fish${index}Mouth"
    fun docFishMouthVar(index: Int) = "${USER_PREFIX}fish${index}Mouth"

    // Food positions (50 food x 2 coords)
    fun foodVar(index: Int, axis: String) = "food${index}$axis"
    fun docFoodVar(index: Int, axis: String) = "${USER_PREFIX}food${index}$axis"

    const val FISH_COUNT = 18
    const val BUBBLE_COUNT = 6
    const val FOOD_COUNT = 50

    val NAMES = SensorVariableNames(
        accelX = ACCEL_X,
        accelY = ACCEL_Y,
    )
}
