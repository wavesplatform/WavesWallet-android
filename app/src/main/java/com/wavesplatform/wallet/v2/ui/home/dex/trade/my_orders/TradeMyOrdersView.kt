/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.wavesplatform.sdk.net.model.response.OrderResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

@StateStrategyType(SkipStrategy::class)
interface TradeMyOrdersView : BaseMvpView {
    fun afterSuccessLoadMyOrders(data: List<OrderResponse>)
    fun afterFailedLoadMyOrders()
    fun afterSuccessCancelOrder()
}
