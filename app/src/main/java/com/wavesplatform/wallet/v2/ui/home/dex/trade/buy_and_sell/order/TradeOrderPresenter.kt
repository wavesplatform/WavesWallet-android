package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import com.wavesplatform.wallet.v2.data.model.local.BuySellData
import com.wavesplatform.wallet.v2.data.model.local.OrderExpiration
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.request.OrderRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.ErrorResponse
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
    var data: BuySellData? = BuySellData()
    var orderRequest: OrderRequest = OrderRequest()

    var humanTotalTyping = false

    var currentAmountBalance: Long? = 0L
    var currentPriceBalance: Long? = 0L

    var selectedExpiration = 5
    var newSelectedExpiration = 5
    val expirationList = arrayOf(OrderExpiration.FIVE_MINUTES, OrderExpiration.THIRTY_MINUTES,
            OrderExpiration.ONE_HOUR, OrderExpiration.ONE_DAY, OrderExpiration.ONE_WEEK, OrderExpiration.ONE_MONTH)

    var orderType: Int = TradeBuyAndSellBottomSheetFragment.BUY_TYPE

    var priceValidation = false
    var amountValidation = false


    fun isAllFieldsValid(): Boolean {
        return priceValidation && amountValidation
    }


    fun getMatcherKey() {
        addSubscription(matcherDataManager.getMatcherKey()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    orderRequest.matcherPublicKey = it
                })
    }

    fun getBalanceFromAssetPair() {
        addSubscription(matcherDataManager.getBalanceFromAssetPair(data?.watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    currentAmountBalance = it[data?.watchMarket?.market?.amountAsset]
                    currentPriceBalance = it[data?.watchMarket?.market?.priceAsset]
                    viewState.successLoadPairBalance(it)
                }, {
                    it.printStackTrace()
                }))
    }

    fun createOrder(amount: String, price: String) {
        viewState.showProgressBar(true)

        orderRequest.amount = amount.toBigDecimal().setScale(data?.watchMarket?.market?.amountAssetDecimals
                ?: 0, RoundingMode.HALF_UP).unscaledValue().toLong()
        orderRequest.price = price.toBigDecimal().setScale(data?.watchMarket?.market?.priceAssetDecimals
                ?: 0, RoundingMode.HALF_UP).unscaledValue().toLong()

        orderRequest.orderType = if (orderType == 0) OrderType.BUY else OrderType.SELL
        orderRequest.assetPair = createPair()
        orderRequest.timestamp = currentTimeMillis
        orderRequest.expiration = orderRequest.timestamp + expirationList[selectedExpiration].timeServer

        addSubscription(matcherDataManager.placeOrder(orderRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.showProgressBar(false)
                    viewState.successPlaceOrder()
                }, {
                    viewState.showProgressBar(false)
                    it.printStackTrace()
                    if (it is RetrofitException) {
                        val response = it.getErrorBodyAs(ErrorResponse::class.java)
                        viewState.afterFailedPlaceOrder(response?.message)
                    }
                }))
    }

    private fun createPair(): OrderBook.Pair {
        val amountAsset =
                if (data?.watchMarket?.market?.amountAsset?.isWaves() == true) ""
                else data?.watchMarket?.market?.amountAsset
        val priceAsset =
                if (data?.watchMarket?.market?.priceAsset?.isWaves() == true) ""
                else data?.watchMarket?.market?.priceAsset

        return OrderBook.Pair(amountAsset, priceAsset)
    }


}
