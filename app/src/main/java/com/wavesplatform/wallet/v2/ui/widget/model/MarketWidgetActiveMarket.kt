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
            var usdData: USDData,
            var eurData: EURData
    ) {
        data class USDData(var price: Double,
                           var percent: Double)

        data class EURData(var price: Double,
                           var percent: Double)
    }
}