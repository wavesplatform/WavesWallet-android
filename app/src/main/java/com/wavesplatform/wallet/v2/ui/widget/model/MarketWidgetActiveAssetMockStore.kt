/*
 * Created by Eduard Zaydel on 25/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

object MarketWidgetActiveAssetMockStore : MarketWidgetActiveAssetStore {
    private var activeAssets = mutableListOf<MarketWidgetActiveAsset>()

    override fun queryAll(): MutableList<MarketWidgetActiveAsset> {
        if (activeAssets.isEmpty()) {
            activeAssets.add(MarketWidgetActiveAsset("Waves", "WAVES",
                    "WAVES", "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS"))
            activeAssets.add(MarketWidgetActiveAsset("VST", "4LHHvYGNKJUg5hj65aGD5vgScvCBmLpdRFtjokvCjSL8",
                    "4LHHvYGNKJUg5hj65aGD5vgScvCBmLpdRFtjokvCjSL8", "WAVES"))
            activeAssets.add(MarketWidgetActiveAsset("ETH", "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu",
                    "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu", "WAVES"))
            activeAssets.add(MarketWidgetActiveAsset("LTC", "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk",
                    "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk", "WAVES"))
        }
        return activeAssets
    }

    override fun save(assetMarket: MarketWidgetActiveAsset) {
        activeAssets.add(assetMarket)
    }

    override fun saveAll(assetsMarkets: MutableList<MarketWidgetActiveAsset>) {
        activeAssets.addAll(assetsMarkets)
    }

    override fun remove(assetMarket: MarketWidgetActiveAsset) {
        activeAssets.remove(assetMarket)
    }
}