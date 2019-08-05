/*
 * Created by Eduard Zaydel on 26/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

import com.wavesplatform.sdk.model.response.data.SearchPairResponse

data class MarketWidgetActiveMarket(
        var assetInfo: MarketWidgetActiveAsset,
        var data: SearchPairResponse.Pair.Data
) {
    data class UI(
            var id: String,
            var name: String,
            var usdData: PriceData,
            var eurData: PriceData
    ) {
        data class PriceData(var price: Double,
                             var percent: Double) {
            fun isPriceIncrease() = percent > 0
            fun isPriceDrop() = percent < 0
            fun isPriceWithoutChange() = percent == 0.0
        }
    }
}