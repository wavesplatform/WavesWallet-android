/*
 * Created by Eduard Zaydel on 3/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.helpers

import com.vicpin.krealmextensions.queryAll
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore

class ClearAssetsHelper {
    companion object {
        fun clearUnimportantAssets(prefsUtil: PrefsUtil, assets: MutableList<AssetBalance>, fromAPI: Boolean = false): MutableList<AssetBalance> {
            return if (!prefsUtil.getValue(PrefsUtil.KEY_IS_CLEARED_ASSETS, false)) {
                if (assets.size == EnvironmentManager.defaultAssets.size) {
                    // new account or empty account - let's go next
                    if (fromAPI) {
                        // don't need to clear any more if after load assets from API list is general
                        prefsUtil.setValue(PrefsUtil.KEY_IS_CLEARED_ASSETS, true)
                    }
                    assets
                } else {
                    // already exists account with unimportant assets: need clear
                    prefsUtil.setValue(PrefsUtil.KEY_IS_CLEARED_ASSETS, true)
                    checkAndClear(assets)
                }
            } else {
                assets
            }
        }

        private fun checkAndClear(assets: MutableList<AssetBalance>): MutableList<AssetBalance> {
            // load config for assets
            val savedAssetPrefs = queryAll<AssetBalanceStore>()
            val savedAssetPrefsMap = savedAssetPrefs.associateBy { it.assetId }

            // filter unimportant assets
            val allUnimportantAssets = assets.filter { asset ->
                !asset.isWaves() && !AssetBalance.isGateway(asset.assetId) && !asset.isFavorite && !asset.isMyWavesToken()
            }

            // filter general assets with zero balance
            val generalAssetsWithZeroBalance = assets.filter { asset ->
                AssetBalance.isGateway(asset.assetId) && !asset.isWaves() && !asset.isFavorite && asset.balance == 0L
            }

            // merge two list, clear and save
            val allAssetsToClear = allUnimportantAssets
                    .plus(generalAssetsWithZeroBalance)

            allAssetsToClear.forEach {
                it.isHidden = true
                savedAssetPrefsMap[it.assetId]?.isHidden = true
            }

            allAssetsToClear.saveAll()
            savedAssetPrefs.saveAll()

            return queryAll<AssetBalance>().toMutableList()
        }
    }
}