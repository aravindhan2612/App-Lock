package com.pg.lockapp.domain.models.interfaces

sealed interface AppListUiState {
    data object Success : AppListUiState
    data object Error : AppListUiState
    data object Loading : AppListUiState
}