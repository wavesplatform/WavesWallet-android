package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order

import com.google.gson.internal.LinkedTreeMap
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TradeOrderView : BaseMvpView {
    fun successLoadPairBalance(pairBalance: LinkedTreeMap<String, Long>)
}
