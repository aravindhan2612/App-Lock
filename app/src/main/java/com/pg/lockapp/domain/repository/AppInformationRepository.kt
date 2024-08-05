package com.pg.lockapp.domain.repository

import com.pg.lockapp.domain.models.enitites.AppInformation
import kotlinx.coroutines.flow.Flow

interface AppInformationRepository {
    suspend fun getAppInfosFromRoom(): List<AppInformation>
    fun getAllAppInformationListAsFlow(): Flow<List<AppInformation>>
    suspend fun addAllAppInfoToRoom(appInformationList: List<AppInformation>)
    suspend fun updateAppInfoToRoom(isLocked: Boolean, packageName: String)
}