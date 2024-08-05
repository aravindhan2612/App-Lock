package com.pg.lockapp.presentation

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LockAppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}