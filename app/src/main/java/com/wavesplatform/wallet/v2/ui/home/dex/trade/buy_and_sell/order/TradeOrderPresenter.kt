package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.OrderExpiration
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.request.OrderRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderBook
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.isWaves
import pers.victor.ext.currentTimeMillis
import java.math.RoundingMode
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

    fun getMatcherKey() {
        addSubscription(matcherDataManager.getMatcherKey()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    orderRequest.matcherPublicKey = it
                })
    }

    fun getBalanceFromAssetPair() {
        addSubscription(matcherDataManager.getBalanceFromAssetPair(watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.successLoadPairBalance(it)
                }, {
                    it.printStackTrace()
                }))
    }

    fun createOrder(amount: String, price: String) {
        orderRequest.amount = amount.toBigDecimal().setScale(watchMarket.market.amountAssetDecimals, RoundingMode.HALF_UP).unscaledValue().toLong()
        orderRequest.price = price.toBigDecimal().setScale(watchMarket.market.priceAssetDecimals, RoundingMode.HALF_UP).unscaledValue().toLong()

        orderRequest.orderType = if (orderType == 0) OrderType.BUY else OrderType.SELL
        orderRequest.assetPair = createPair()
        orderRequest.timestamp = currentTimeMillis
        orderRequest.expiration = orderRequest.timestamp + expirationList[selectedExpiration].timeServer

        addSubscription(matcherDataManager.placeOrder(orderRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.successPlaceOrder()
                }, {
                    it.printStackTrace()
                }))
    }

    private fun createPair(): OrderBook.Pair {
        val amountAsset =
                if (watchMarket.market.amountAsset.isWaves()) ""
                else watchMarket.market.amountAsset
        val priceAsset =
                if (watchMarket.market.priceAsset.isWaves()) ""
                else watchMarket.market.priceAsset

        return OrderBook.Pair(amountAsset, priceAsset)
    }


}
