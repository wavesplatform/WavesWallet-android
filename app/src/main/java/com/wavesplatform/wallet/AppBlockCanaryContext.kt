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

    override fun provideUid(): String {
        return "87224330"
    }

    override fun provideNetworkType(): String {
        return "wifi"
    }

    override fun provideMonitorDuration(): Int {
        return 9999
    }

    override fun provideBlockThreshold(): Int {
        return 500
    }

    override fun displayNotification(): Boolean {
        return BuildConfig.DEBUG
    }

    override fun concernPackages(): List<String> {
        val list = super.provideWhiteList()
        list.add("com.wavesplatform")
        return list
    }

    override fun provideWhiteList(): List<String> {
        val list = super.provideWhiteList()
        list.add("com.whitelist")
        return list
    }

    override fun stopWhenDebugging(): Boolean {
        return true
    }
}