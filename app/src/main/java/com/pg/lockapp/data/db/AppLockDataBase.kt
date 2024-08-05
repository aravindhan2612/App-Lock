package com.pg.lockapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pg.lockapp.data.db.dao.AppInformationDao
import com.pg.lockapp.domain.models.enitites.AppInformation

@Database(entities = [AppInformation::class], version = 1, exportSchema = false)
abstract class AppLockDataBase : RoomDatabase() {
    abstract fun  appInformationDao() : AppInformationDao
}