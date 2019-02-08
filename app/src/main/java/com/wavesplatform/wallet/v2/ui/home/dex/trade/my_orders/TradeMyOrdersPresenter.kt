package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.WatchMarket
import com.wavesplatform.sdk.model.request.CancelOrderRequest
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class TradeMyOrdersPresenter @Inject constructor() : BasePresenter<TradeMyOrdersView>() {
    var watchMarket: WatchMarket? = null
    var cancelOrderRequest = CancelOrderRequest()

    fun loadMyOrders() {
        addSubscription(matcherDataManager.loadMyOrders(watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    val sortedByTimestamp = it
                            .sortedByDescending { it.timestamp }
                            .toMutableList()

                    viewState.afterSuccessLoadMyOrders(sortedByTimestamp)
                }, {
                    viewState.afterFailedLoadMyOrders()
                }))
    }

    fun cancelOrder(orderId: String?) {
        viewState.showProgressBar(true)
        addSubscription(matcherDataManager.cancelOrder(orderId, watchMarket, cancelOrderRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.showProgressBar(false)
                    viewState.afterSuccessCancelOrder()
                }, {
                    viewState.showProgressBar(false)
                    it.printStackTrace()
                }))
    }
}
