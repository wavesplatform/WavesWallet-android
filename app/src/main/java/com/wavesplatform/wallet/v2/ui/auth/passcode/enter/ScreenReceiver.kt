package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class ScreenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            // todo BlockchainApplication.getAccessManager().setCurrentAccount("")
        }
    }
}