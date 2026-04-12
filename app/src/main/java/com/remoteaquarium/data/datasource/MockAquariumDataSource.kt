package com.remoteaquarium.data.datasource

import com.remoteaquarium.data.document.AquariumDocumentBuilder
import com.remoteaquarium.domain.model.AquariumDocument
import javax.inject.Inject

class MockAquariumDataSource @Inject constructor(
    private val documentBuilder: AquariumDocumentBuilder,
) : AquariumDataSource {
    override suspend fun fetchAquariumDocument(screenWidth: Float, screenHeight: Float): AquariumDocument =
        documentBuilder.build(screenWidth, screenHeight)
}
