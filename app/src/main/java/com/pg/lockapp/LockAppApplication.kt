package com.pg.lockapp

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.pg.lockapp.presentation.MyLifecycleObserver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LockAppApplication : Application() {
    companion object {
        val CHANNEL_ID: String = "autoStartServiceChannel"
        val CHANNEL_NAME: String = "Auto Start Service Channel"

    }


    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.Companion.get().lifecycle.addObserver(
            MyLifecycleObserver(
            )
        )
    }
}