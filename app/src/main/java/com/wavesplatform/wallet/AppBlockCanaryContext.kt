/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet

import com.github.moduth.blockcanary.BlockCanaryContext

class AppBlockCanaryContext : BlockCanaryContext() {
    override fun provideQualifier(): String {
        return BuildConfig.VERSION_CODE.toString() + "_" + BuildConfig.VERSION_NAME + "_YYB"
    }

    override fun provideNetworkType(): String {
        return "wifi"
    }

    override fun displayNotification(): Boolean {
        return BuildConfig.DEBUG
    }

    override fun stopWhenDebugging(): Boolean {
        return true
    }
}