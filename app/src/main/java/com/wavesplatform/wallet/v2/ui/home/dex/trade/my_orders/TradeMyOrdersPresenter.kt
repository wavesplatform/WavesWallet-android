package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.R.id.result
import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.request.CancelOrderRequest
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.asDateString
import javax.inject.Inject

@InjectViewState
class TradeMyOrdersPresenter @Inject constructor() : BasePresenter<TradeMyOrdersView>() {
    var watchMarket: WatchMarket? = null
    var canclelOrderRequest = CancelOrderRequest()
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
                        it.t.sectionTimestamp = date
                        result.add(it)
                    }

                    viewState.afterSuccessMyOrders(result)
                }, {
                    viewState.afterFailedMyOrders()
                }))
    }

    fun cancelOrder(orderId: String?) {
        viewState.showProgressBar(true)
        addSubscription(matcherDataManager.cancelOrder(orderId, watchMarket, canclelOrderRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.showProgressBar(false)
                    viewState.afterSuccessCancelOrder()
                }, {
                    viewState.showProgressBar(false)
                    it.printStackTrace()
                }))
    }

    fun deleteOrder(orderId: String?, position: Int) {
        viewState.showProgressBar(true)
        addSubscription(matcherDataManager.deleteOrder(orderId, watchMarket, canclelOrderRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.showProgressBar(false)
                    viewState.afterSuccessDeleteOrder(position)
                }, {
                    viewState.showProgressBar(false)
                    it.printStackTrace()
                }))
    }
}
