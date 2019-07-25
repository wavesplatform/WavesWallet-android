/*
 * Created by Eduard Zaydel on 25/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

interface MarketWidgetActiveAssetStore {
    fun queryAll(): MutableList<MarketWidgetActiveAsset>
    fun save(assetMarket: MarketWidgetActiveAsset)
    fun saveAll(assetsMarkets: MutableList<MarketWidgetActiveAsset>)
    fun remove(assetMarket: MarketWidgetActiveAsset)
}