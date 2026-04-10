package com.remoteaquarium.data.document

import com.remoteaquarium.domain.model.SensorVariableNames

object SensorVariableRegistry {
    private const val USER_PREFIX = "USER:"

    const val ACCEL_X = "accelX"
    const val ACCEL_Y = "accelY"

    // Used by the document creation side (addNamedFloat)
    const val DOC_ACCEL_X = "${USER_PREFIX}${ACCEL_X}"
    const val DOC_ACCEL_Y = "${USER_PREFIX}${ACCEL_Y}"

    // Used by the player side (setUserLocalFloat — which adds USER: prefix itself)
    val NAMES = SensorVariableNames(
        accelX = ACCEL_X,
        accelY = ACCEL_Y,
    )
}
