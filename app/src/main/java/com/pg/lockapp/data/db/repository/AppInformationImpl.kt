package com.pg.lockapp.data.db.repository

import com.pg.lockapp.data.db.dao.AppInformationDao
import com.pg.lockapp.domain.models.enitites.AppInformation
import com.pg.lockapp.domain.repository.AppInformationRepository
import kotlinx.coroutines.flow.Flow

class AppInformationImpl(private val appInfoDao: AppInformationDao) : AppInformationRepository {
    override suspend fun getAppInfosFromRoom(): List<AppInformation>{
       return  appInfoDao.getAppInfos()
    }

    override fun getAllAppInformationListAsFlow(): Flow<List<AppInformation>> {
        return appInfoDao.getAllAppInformationListAsFlow()
    }

    override suspend fun addAllAppInfoToRoom(appInformationList: List<AppInformation>) {
        appInfoDao.insertAll(appInformationList)
    }

    override suspend fun updateAppInfoToRoom(isLocked: Boolean, packageName: String) {
        appInfoDao.update(isLocked, packageName)
    }
}