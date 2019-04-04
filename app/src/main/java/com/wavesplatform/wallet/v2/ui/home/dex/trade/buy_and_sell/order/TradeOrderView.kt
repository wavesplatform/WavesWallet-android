/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TradeOrderView : BaseMvpView {
    fun successLoadPairBalance(currentAmountBalance: Long?, currentPriceBalance: Long?)
    fun successPlaceOrder()
    fun afterFailedPlaceOrder(message: String?)
    fun showCommissionLoading()
    fun showCommissionSuccess(unscaledAmount: Long)
    fun showCommissionError()
}
