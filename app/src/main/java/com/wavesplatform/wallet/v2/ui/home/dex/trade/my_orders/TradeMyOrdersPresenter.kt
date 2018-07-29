package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryItem
import java.util.*
import javax.inject.Inject

@InjectViewState
class TradeMyOrdersPresenter @Inject constructor() : BasePresenter<TradeMyOrdersView>() {
    fun loadMyOrders() {
        var data = ArrayList<MyOrderItem>()
        data.add(MyOrderItem(true, "10.12.2017"))
        data.add(MyOrderItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 523.061350, Random().nextDouble())))
        data.add(MyOrderItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 3.061350, Random().nextDouble())))
        data.add(MyOrderItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 0.061350, Random().nextDouble())))
        data.add(MyOrderItem(true, "06.01.2018"))
        data.add(MyOrderItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 363.5061350, Random().nextDouble())))
        data.add(MyOrderItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
        data.add(MyOrderItem(true, "15.06.2018"))
        data.add(MyOrderItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))

        viewState.afterSuccessMyOrders(data)
    }
}
