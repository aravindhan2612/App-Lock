package com.pg.lockapp.presentation

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LockAppApplication : Application() {
    companion object{
        val CHANNEL_ID: String = "autoStartServiceChannel"
        val CHANNEL_NAME: String = "Auto Start Service Channel"
    }



    override fun onCreate() {
        super.onCreate()
        //createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}