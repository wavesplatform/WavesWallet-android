/*
 * Created by Eduard Zaydel on 26/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

import com.wavesplatform.sdk.model.response.data.PairResponse

data class MarketWidgetActiveMarket(
        var assetInfo: MarketWidgetActiveAsset,
        var data: PairResponse.DataResponse
)