package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TradeMyOrdersView : BaseMvpView {
    abstract fun afterSuccessMyOrders(data: ArrayList<MyOrderItem>)

}
