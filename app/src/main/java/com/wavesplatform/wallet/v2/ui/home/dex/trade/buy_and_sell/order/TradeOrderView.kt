package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order

import com.google.gson.internal.LinkedTreeMap
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TradeOrderView : BaseMvpView {
    fun successLoadPairBalance(pairBalance: LinkedTreeMap<String, Long>)
    fun successPlaceOrder()
    fun afterFailedPlaceOrder(message: String?)
    fun showCommissionLoading()
    fun showCommissionSuccess(unscaledAmount: Long)
    fun showCommissionError()
}
