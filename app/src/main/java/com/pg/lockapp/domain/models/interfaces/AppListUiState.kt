package com.pg.lockapp.domain.models.interfaces

sealed interface AppListUiState {
    data object Finished : AppListUiState
    data object Loading : AppListUiState
}