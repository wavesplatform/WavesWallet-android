/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import android.os.Handler

class AppLifecycleObserver : LifecycleObserver {

    private var handler: Handler = Handler()
    private var resetWalletTask = Runnable {
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