/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex

import com.wavesplatform.sdk.model.response.WatchMarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface DexView : BaseMvpView {
    fun afterSuccessLoadMarkets(list: ArrayList<WatchMarketResponse>)
    fun afterFailedLoadMarkets()
    fun afterSuccessLoadPairInfo(watchMarket: WatchMarketResponse, index: Int)
    fun afterFailedLoadPairInfo()
}
