/*
 * Created by Eduard Zaydel on 25/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

data class MarketWidgetActiveAsset(
        var name: String,
        var id: String,
        var amountAsset: String,
        var priceAsset: String
)