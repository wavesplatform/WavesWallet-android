package com.wavesplatform.wallet

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.wavesplatform.wallet.v2.data.manager.AccessManager
import pers.victor.ext.toast
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import java.util.*
import javax.inject.Inject

class AppLifecycleObserver : LifecycleObserver {

    companion object {
        const val SESSION_LIFE_AFTER_MINIMIZE = 5000L
    }

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
}