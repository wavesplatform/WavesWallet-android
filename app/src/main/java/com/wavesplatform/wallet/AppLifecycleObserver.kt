/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Handler

class AppLifecycleObserver : LifecycleObserver {

    var handler: Handler = Handler()
    var resetWalletTask = Runnable {
        App.getAccessManager().resetWallet()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        handler.removeCallbacks(resetWalletTask)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        handler.postDelayed(resetWalletTask, SESSION_LIFE_AFTER_MINIMIZE)
    }

    companion object {
        const val SESSION_LIFE_AFTER_MINIMIZE = 5000L
    }
}