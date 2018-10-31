package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.OrderbookItem
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import java.util.*
import javax.inject.Inject

@InjectViewState
class TradeOrderbookPresenter @Inject constructor() : BasePresenter<TradeOrderbookView>() {
    var watchMarket: WatchMarket? = null

    fun loadOrderbook() {
        var data = ArrayList<OrderbookItem>()
        data.add(OrderbookItem(OrderbookItem.PRICE_TYPE, TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 523.061350, Random().nextDouble())))
        data.add(OrderbookItem(OrderbookItem.PRICE_TYPE, TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 3.061350, Random().nextDouble())))
        data.add(OrderbookItem(OrderbookItem.PRICE_TYPE, TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 0.061350, Random().nextDouble())))
        data.add(OrderbookItem(OrderbookItem.LAST_PRICE_TYPE, TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 363.5061350, Random().nextDouble())))
        data.add(OrderbookItem(OrderbookItem.PRICE_TYPE, TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
        data.add(OrderbookItem(OrderbookItem.PRICE_TYPE, TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
        data.add(OrderbookItem(OrderbookItem.PRICE_TYPE, TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))

        viewState.afterSuccessOrderbook(data)
    }

}
