/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.analytics

import com.wavesplatform.sdk.net.model.response.AssetBalanceResponse
import com.wavesplatform.wallet.v2.util.PrefsUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticAssetManager @Inject constructor(private var prefUtil: PrefsUtil) {

    private var assetsIds: MutableSet<String> = mutableSetOf()
        get() {
            return prefUtil.getValue(PrefsUtil.KEY_ASSETS_ALL)
        }
        set(value) {
            field = value
            prefUtil.setValue(PrefsUtil.KEY_ASSETS_ALL, value)
        }


    private var zeroAssetsIds: MutableSet<String> = mutableSetOf()
        get() {
            return prefUtil.getValue(PrefsUtil.KEY_ASSETS_ZERO)
        }
        set(value) {
            field = value
            prefUtil.setValue(PrefsUtil.KEY_ASSETS_ZERO, value)
        }

    fun trackFromZeroBalances(assets: MutableList<AssetBalanceResponse>) {
        val zeroAssets = assets.filter { it.balance == 0L }

        saveZeroBalance(zeroAssets)

        val assetBalances = assets.filter { it.balance ?: 0 > 0L }

        val zeroBalances = zeroAssetsIds
        val allBalances = assetsIds

        assetBalances.forEach { asset ->
            if (zeroBalances.contains(asset.assetId) && !allBalances.contains(asset.assetId)) {
                allBalances.add(asset.assetId)
                analytics.trackEvent(AnalyticEvents.WalletStartBalanceFromZeroEvent(asset.getName()))
            }
        }

        assetsIds = allBalances
    }

    private fun saveZeroBalance(assetId: List<AssetBalanceResponse>) {
        val zeroBalances = zeroAssetsIds
        assetId.forEach { asset ->
            if (!zeroBalances.contains(asset.assetId)) {
                zeroBalances.add(asset.assetId)
            }
        }
        zeroAssetsIds = zeroBalances
    }
}
