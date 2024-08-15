package com.pg.lockapp.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class MyLifecycleObserver(
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        //forground
    }

    override fun onPause(owner: LifecycleOwner) {
        //background
    }
}

