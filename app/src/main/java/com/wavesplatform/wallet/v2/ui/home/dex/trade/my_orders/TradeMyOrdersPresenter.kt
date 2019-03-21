package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.net.model.WatchMarket
import com.wavesplatform.sdk.net.model.request.CancelOrderRequest
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import javax.inject.Inject

@InjectViewState
class TradeMyOrdersPresenter @Inject constructor() : BasePresenter<TradeMyOrdersView>() {
    var watchMarket: WatchMarket? = null
    var fee: Long = 0

    fun loadMyOrders() {
        addSubscription(matcherDataManager.loadMyOrders(watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.afterSuccessLoadMyOrders(it)
                }, {
                    viewState.afterFailedLoadMyOrders()
                }))
    }

    fun loadCommission() {
        addSubscription(nodeDataManager.getCommissionForPair(watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    fee = it
                })
    }
}
