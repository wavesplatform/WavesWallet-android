package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.request.CancelOrderRequest
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
                    viewState.afterSuccessLoadMyOrders(it)
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
