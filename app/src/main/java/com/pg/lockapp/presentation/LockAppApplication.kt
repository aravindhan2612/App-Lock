package com.pg.lockapp.presentation

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LockAppApplication : Application() {
    companion object {
        val CHANNEL_ID: String = "autoStartServiceChannel"
        val CHANNEL_NAME: String = "Auto Start Service Channel"

    }


    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            MyLifecycleObserver(
            )
        )
    }
}