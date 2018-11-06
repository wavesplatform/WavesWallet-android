package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.OrderExpiration
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.request.OrderRequest
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class TradeOrderPresenter @Inject constructor() : BasePresenter<TradeOrderView>() {
    lateinit var watchMarket: WatchMarket
    var orderRequest: OrderRequest = OrderRequest()

    var selectedExpiration = 5
    var newSelectedExpiration = 5
    val expirationList = arrayOf(OrderExpiration.FIVE_MINUTES, OrderExpiration.THIRTY_MINUTES,
            OrderExpiration.ONE_HOUR, OrderExpiration.ONE_DAY, OrderExpiration.ONE_WEEK, OrderExpiration.ONE_MONTH)

    var orderType: Int = TradeBuyAndSellBottomSheetFragment.BUY_TYPE

    fun getBalanceFromAssetPair() {
        addSubscription(matcherDataManager.getBalanceFromAssetPair(watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.successLoadPairBalance(it)
                }, {
                    it.printStackTrace()
                }))
    }


}
