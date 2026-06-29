package io.imito.woundgenius.sample

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import javax.inject.Inject

class AppLifecycleObserver @Inject constructor() : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        isEnterFromBackground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        // No-op: no cleanup required when the app enters the background
    }

    companion object {

        var isEnterFromBackground = false

    }

}