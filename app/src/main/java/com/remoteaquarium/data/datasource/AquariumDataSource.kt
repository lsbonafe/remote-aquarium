package com.remoteaquarium.data.datasource

import com.remoteaquarium.domain.model.AquariumDocument

interface AquariumDataSource {
    suspend fun fetchAquariumDocument(screenWidth: Float, screenHeight: Float): AquariumDocument
}
