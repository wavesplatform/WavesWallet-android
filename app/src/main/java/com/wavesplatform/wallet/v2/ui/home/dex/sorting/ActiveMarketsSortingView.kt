package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import com.wavesplatform.sdk.net.model.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface ActiveMarketsSortingView : BaseMvpView {
    fun afterSuccessLoadMarkets(list: List<MarketResponse>)
}
