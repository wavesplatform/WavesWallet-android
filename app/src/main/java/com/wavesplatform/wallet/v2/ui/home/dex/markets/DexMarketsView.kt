/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.wavesplatform.sdk.model.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface DexMarketsView : BaseMvpView {
    fun afterSuccessGetMarkets(markets: MutableList<MarketResponse>)
    fun afterFailGetMarkets()
}
