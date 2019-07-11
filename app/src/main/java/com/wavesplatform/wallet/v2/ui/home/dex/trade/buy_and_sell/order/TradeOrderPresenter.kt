/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v2.data.model.local.BuySellData
import com.wavesplatform.wallet.v2.data.model.local.OrderExpiration
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.remote.request.OrderRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.util.*
import java.math.RoundingMode
import javax.inject.Inject

@InjectViewState
class TradeOrderPresenter @Inject constructor() : BasePresenter<TradeOrderView>() {
    var data: BuySellData? = BuySellData()
    var orderRequest: OrderRequest = OrderRequest()
    var wavesBalance: AssetBalance = AssetBalance()

    var humanTotalTyping = false

    var currentAmountBalance: Long = 0L
    var currentPriceBalance: Long = 0L

    var selectedExpiration = 5
    var newSelectedExpiration = 5
    val expirationList = arrayOf(
            OrderExpiration.FIVE_MINUTES,
            OrderExpiration.THIRTY_MINUTES,
            OrderExpiration.ONE_HOUR,
            OrderExpiration.ONE_DAY,
            OrderExpiration.ONE_WEEK,
            OrderExpiration.ONE_MONTH)

    var orderType: Int = TradeBuyAndSellBottomSheetFragment.BUY_TYPE

    var priceValidation = false
    var totalPriceValidation = false
    var amountValidation = false

    var fee = 0L
    var feeAssetId = ""

    fun initBalances(){
        currentAmountBalance = queryFirst<AssetBalance> { equalTo("assetId",
                data?.watchMarket?.market?.amountAsset?.withWavesIdConvert()) }?.getAvailableBalance() ?: 0L
        currentPriceBalance = queryFirst<AssetBalance> { equalTo("assetId",
                data?.watchMarket?.market?.priceAsset?.withWavesIdConvert()) }?.getAvailableBalance() ?: 0L
    }

    fun isAllFieldsValid(): Boolean {
        return priceValidation && amountValidation && totalPriceValidation
    }

    fun getMatcherKey() {
        addSubscription(matcherDataManager.getMatcherKey()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    orderRequest.matcherPublicKey = it
                })
    }

    fun loadWavesBalance() {
        addSubscription(nodeDataManager.loadWavesBalance()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    wavesBalance = it
                })
    }

    fun loadPairBalancesAndCommission() {
        viewState.showCommissionLoading()
        fee = 0L
        addSubscription(matcherDataManager.getBalanceFromAssetPair(data?.watchMarket)
                .flatMap {
                    // save balance
                    currentAmountBalance = it[data?.watchMarket?.market?.amountAsset] ?: 0
                    currentPriceBalance = it[data?.watchMarket?.market?.priceAsset] ?: 0

                    return@flatMap nodeDataManager.getCommissionForPair(
                            data?.watchMarket?.market?.amountAsset,
                            data?.watchMarket?.market?.priceAsset)
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ calculatedFee ->
                    fee = calculatedFee
                    orderRequest.matcherFee = fee

                    viewState.showCommissionSuccess(fee)
                    viewState.successLoadPairBalance(currentAmountBalance, currentPriceBalance)
                }, {
                    it.printStackTrace()
                }))
    }

    fun createOrder(amount: String, price: String) {
        viewState.showProgressBar(true)

        orderRequest.amount = amount.clearBalance().toBigDecimal().setScale(data?.watchMarket?.market?.amountAssetDecimals
                ?: 0, RoundingMode.HALF_UP).unscaledValue().toLong()
        orderRequest.price = price.clearBalance().toBigDecimal().setScale((8.plus(data?.watchMarket?.market?.priceAssetDecimals
                ?: 0).minus(data?.watchMarket?.market?.amountAssetDecimals
                ?: 0)), RoundingMode.HALF_UP).unscaledValue().toLong()

        orderRequest.orderType = if (orderType == 0) OrderType.BUY else OrderType.SELL
        orderRequest.assetPair = createPair()
        orderRequest.timestamp = EnvironmentManager.getTime()
        orderRequest.expiration = orderRequest.timestamp + expirationList[selectedExpiration].timeServer

        orderRequest.matcherFeeAssetId = feeAssetId
        orderRequest.matcherFee = fee

        addSubscription(matcherDataManager.placeOrder(orderRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.showProgressBar(false)
                    viewState.successPlaceOrder()
                }, {
                    it.printStackTrace()
                    viewState.showProgressBar(false)
                    it.errorBody()?.let {
                        viewState.afterFailedPlaceOrder(it.message)
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



    class Commission(val assetId: String, var commission: Long)
}
