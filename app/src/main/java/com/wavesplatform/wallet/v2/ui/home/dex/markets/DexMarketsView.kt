package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface DexMarketsView : BaseMvpView {
    fun afterSuccessGetMarkets(markets: ArrayList<Market>)
}
