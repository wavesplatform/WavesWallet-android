package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

@StateStrategyType(SkipStrategy::class)
interface TradeMyOrdersView : BaseMvpView {
    fun afterSuccessMyOrders(data: kotlin.collections.List<com.wavesplatform.wallet.v2.data.model.remote.response.OrderResponse>)
    fun afterFailedMyOrders()
    fun afterSuccessCancelOrder()
}
