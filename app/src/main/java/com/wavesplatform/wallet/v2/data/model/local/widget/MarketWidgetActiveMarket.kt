/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.widget

import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import java.math.BigDecimal

data class MarketWidgetActiveMarket(
        var assetInfo: MarketWidgetActiveAsset,
        var data: SearchPairResponse.Pair.Data,
        var assetPrice: Double
) {
    data class UI(
            var id: String,
            var name: String,
            var usdData: PriceData,
            var eurData: PriceData
    ) {
        data class PriceData(var price: BigDecimal,
                             var percent: BigDecimal) {
            fun isPriceIncrease() = percent > BigDecimal.ZERO
            fun isPriceDrop() = percent < BigDecimal.ZERO
            fun isPriceWithoutChange() = percent == BigDecimal.ZERO
        }
    }
}