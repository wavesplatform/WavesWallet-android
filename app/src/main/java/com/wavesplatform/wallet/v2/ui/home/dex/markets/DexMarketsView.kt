package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface DexMarketsView : BaseMvpView {
    fun afterSuccessGetMarkets(markets: MutableList<MarketResponse>)
    fun afterFailGetMarkets()
}
