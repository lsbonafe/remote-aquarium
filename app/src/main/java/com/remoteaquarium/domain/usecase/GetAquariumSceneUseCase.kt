package com.remoteaquarium.domain.usecase

import com.remoteaquarium.domain.model.AquariumDocument
import com.remoteaquarium.domain.repository.AquariumRepository
import javax.inject.Inject

class GetAquariumSceneUseCase @Inject constructor(
    private val repository: AquariumRepository,
) {
    suspend operator fun invoke(): AquariumDocument = repository.getAquariumDocument()
}
