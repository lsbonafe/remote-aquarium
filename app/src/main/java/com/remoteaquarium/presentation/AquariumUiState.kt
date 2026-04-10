package com.remoteaquarium.presentation

import com.remoteaquarium.domain.model.AquariumDocument

sealed interface AquariumUiState {
    data object Loading : AquariumUiState
    data class Ready(val aquariumDocument: AquariumDocument) : AquariumUiState
    data class Error(val message: String) : AquariumUiState
}
