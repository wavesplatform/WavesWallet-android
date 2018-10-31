package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.asDateString
import javax.inject.Inject

@InjectViewState
class TradeMyOrdersPresenter @Inject constructor() : BasePresenter<TradeMyOrdersView>() {
    var watchMarket: WatchMarket? = null
    private var hashOfTimestamp = hashMapOf<Long, Long>()

    fun loadMyOrders() {
        addSubscription(matcherDataManager.loadMyOrders(watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    val sortedByTimestamp = it
                            .sortedByDescending { it.timestamp }
                            .mapTo(mutableListOf()) {
                                MyOrderItem(it)
                            }

                    val result = arrayListOf<MyOrderItem>()

                    sortedByTimestamp.forEach {
                        val date = (it.t.timestamp) / (1000 * 60 * 60 * 24)
                        if (hashOfTimestamp[date] == null) {
                            hashOfTimestamp[date] = date
                            result.add(MyOrderItem(true, it.t.timestamp.asDateString("dd.MM.yyyy")))
                        }
                        result.add(it)
                    }

                    viewState.afterSuccessMyOrders(result)
                }, {
                    viewState.afterFailedMyOrders()
                }))
    }
}
