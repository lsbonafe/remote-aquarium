package com.remoteaquarium.data.repository

import com.remoteaquarium.data.datasource.AquariumDataSource
import com.remoteaquarium.domain.model.AquariumDocument
import com.remoteaquarium.domain.repository.AquariumRepository
import javax.inject.Inject

class AquariumRepositoryImpl @Inject constructor(
    private val dataSource: AquariumDataSource,
) : AquariumRepository {
    override suspend fun getAquariumDocument(screenWidth: Float, screenHeight: Float): AquariumDocument =
        dataSource.fetchAquariumDocument(screenWidth, screenHeight)
}
