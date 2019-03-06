package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import com.arellomobile.mvp.InjectViewState
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.local.LastPriceItem
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTradesResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderBook
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.clearBalance
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.stripZeros
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class TradeOrderBookPresenter @Inject constructor() : BasePresenter<TradeOrderBookView>() {
    var watchMarket: WatchMarket? = null
    var needFirstScrollToLastPrice = true
    val subscriptions = CompositeDisposable()

    fun loadOrderBook() {
        addSubscription(
                Observable.interval(0, 10, TimeUnit.SECONDS)
                        .retry(3)
                        .flatMap {
                            return@flatMap Observable.zip(matcherDataManager.loadOrderBook(watchMarket),
                                    apiDataManager.getLastTradeByPair(watchMarket),
                                    BiFunction { orderBook: OrderBook, lastPrice: ArrayList<LastTradesResponse.Data.ExchangeTransaction> ->
                                        return@BiFunction Pair(orderBook, lastPrice)
                                    })
                        }
                        .doOnError {
                            runOnUiThread { viewState.afterFailedOrderbook() }
                        }
                        .onErrorResumeNext(Observable.empty())
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ pair ->
                            val result = mutableListOf<MultiItemEntity>()
                            result.addAll(getCalculatedAsks(pair.first.asks).asReversed())
                            pair.second.firstOrNull().notNull {
                                val firstAsk = pair.first.asks.firstOrNull()?.price?.toDouble()
                                val firstBid = pair.first.bids.firstOrNull()?.price?.toDouble()
                                if (firstAsk == null && firstBid == null) {
                                    // do nothing, will show empty view
                                } else if (firstAsk == null || firstBid == null) {
                                    result.add(LastPriceItem(0.0, it))
                                } else {
                                    var percent = ((firstAsk.minus(firstBid))
                                            .times(100).div(firstBid))
                                    if (percent >= 100) percent = 99.99
                                    result.add(LastPriceItem(percent, it))
                                }
                            }
                            val lastPricePosition = result.size - 1
                            result.addAll(getCalculatedBids(pair.first.bids))
                            viewState.afterSuccessOrderbook(result, lastPricePosition)
                        }, {
                            it.printStackTrace()
                            viewState.afterFailedOrderbook()
                        }))
    }

    private fun getCalculatedBids(list: List<OrderBook.Bid>): Collection<MultiItemEntity> {
        var sum = 0.0
        list.forEach {
            val amountUIValue = MoneyUtil.getScaledText(it.amount, watchMarket?.market?.amountAssetDecimals
                    ?: 0).stripZeros()
            val priceUIValue = MoneyUtil.getScaledPrice(it.price, watchMarket?.market?.amountAssetDecimals
                    ?: 0, watchMarket?.market?.priceAssetDecimals ?: 0).stripZeros()
            sum += amountUIValue.clearBalance().toDouble() * priceUIValue.clearBalance().toDouble()
            it.sum = sum
        }
        return list
    }

    private fun getCalculatedAsks(list: List<OrderBook.Ask>): List<OrderBook.Ask> {
        var sum = 0.0
        list.forEach {
            val amountUIValue = MoneyUtil.getScaledText(it.amount, watchMarket?.market?.amountAssetDecimals
                    ?: 0).stripZeros()
            val priceUIValue = MoneyUtil.getScaledPrice(it.price, watchMarket?.market?.amountAssetDecimals
                    ?: 0, watchMarket?.market?.priceAssetDecimals ?: 0).stripZeros()
            sum += amountUIValue.clearBalance().toDouble() * priceUIValue.clearBalance().toDouble()
            it.sum = sum
        }
        return list
    }

    override fun onDestroy() {
        super.onDestroy()
        clearSubscriptions()
    }

    override fun addSubscription(subscription: Disposable) {
        subscriptions.add(subscription)
    }

    fun clearSubscriptions() {
        subscriptions.clear()
    }
}
