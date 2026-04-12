package com.remoteaquarium.domain.repository

import com.remoteaquarium.domain.model.AquariumDocument

interface AquariumRepository {
    suspend fun getAquariumDocument(screenWidth: Float, screenHeight: Float): AquariumDocument
}
