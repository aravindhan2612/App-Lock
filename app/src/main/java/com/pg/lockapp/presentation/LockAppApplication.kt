package com.pg.lockapp.presentation

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LockAppApplication : Application() {
    companion object {
        val CHANNEL_ID: String = "autoStartServiceChannel"
        val CHANNEL_NAME: String = "Auto Start Service Channel"
    }


    override fun onCreate() {
        super.onCreate()
    }
}