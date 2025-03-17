package com.pg.lockapp.domain.models.enitites

import kotlinx.serialization.Serializable

sealed class Screen {

    @Serializable
    object AppList : Screen()

    @Serializable
    object Settings : Screen()
}