package com.pg.lockapp.di

import android.content.Context
import androidx.room.Room
import com.pg.lockapp.common.Constants
import com.pg.lockapp.data.db.AppLockDataBase
import com.pg.lockapp.data.db.dao.AppInformationDao
import com.pg.lockapp.data.db.repository.AppInformationImpl
import com.pg.lockapp.domain.repository.AppInformationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    fun provideAppLockDataBase(@ApplicationContext context: Context) = Room.databaseBuilder(context, AppLockDataBase::class.java,Constants.APP_LOCK_DB_NAME).build()

    @Provides
    fun provideAppInformationDao(appLockDataBase: AppLockDataBase) = appLockDataBase.appInformationDao()

    @Provides
    fun provideAppInformationRepository(appInformationDao: AppInformationDao): AppInformationRepository = AppInformationImpl(appInformationDao)
}