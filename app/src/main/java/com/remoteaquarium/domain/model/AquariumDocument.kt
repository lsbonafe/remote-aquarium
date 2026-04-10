package com.remoteaquarium.domain.model

data class AquariumDocument(
    val documentBytes: ByteArray,
    val sensorVariableNames: SensorVariableNames,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AquariumDocument) return false
        return documentBytes.contentEquals(other.documentBytes) &&
            sensorVariableNames == other.sensorVariableNames
    }

    override fun hashCode(): Int {
        var result = documentBytes.contentHashCode()
        result = 31 * result + sensorVariableNames.hashCode()
        return result
    }
}
