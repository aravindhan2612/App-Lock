package com.pg.lockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pg.lockapp.domain.models.enitites.AppInformation
import kotlinx.coroutines.flow.Flow


@Dao
interface AppInformationDao {

    @Query("Select * from app_info")
    suspend fun getAppInfos(): List<AppInformation>

    @Query("Select * from app_info")
    fun getAllAppInformationListAsFlow(): Flow<List<AppInformation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appInformationList: List<AppInformation>)

    @Query("UPDATE app_info SET is_locked=:isLocked WHERE package_name = :packageName AND app_name = :appName")
    suspend fun update(isLocked: Boolean, packageName: String, appName: String)
}