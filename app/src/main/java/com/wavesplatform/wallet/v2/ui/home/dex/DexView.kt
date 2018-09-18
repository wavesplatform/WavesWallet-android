package com.wavesplatform.wallet.v2.ui.home.dex

import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface DexView :BaseMvpView{
    fun afterSuccessLoadMarkets(list: ArrayList<Market>)
    fun afterFailedLoadMarkets()
}
