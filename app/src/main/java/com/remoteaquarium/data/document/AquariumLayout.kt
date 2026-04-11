package com.remoteaquarium.data.document

object AquariumLayout {
    const val SAND_TOP_FRACTION = 0.82f

    fun sandTop(h: Float) = h * SAND_TOP_FRACTION
}
