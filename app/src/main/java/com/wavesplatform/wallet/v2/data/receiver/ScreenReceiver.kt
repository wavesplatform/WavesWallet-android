/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wavesplatform.wallet.App

class ScreenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            App.getAccessManager().resetWallet()
        }
    }
}