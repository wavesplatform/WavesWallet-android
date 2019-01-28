package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

@StateStrategyType(SkipStrategy::class)
interface TradeMyOrdersView : BaseMvpView {
    fun afterSuccessLoadMyOrders(data: List<OrderResponse>)
    fun afterFailedLoadMyOrders()
    fun afterSuccessCancelOrder()
}
