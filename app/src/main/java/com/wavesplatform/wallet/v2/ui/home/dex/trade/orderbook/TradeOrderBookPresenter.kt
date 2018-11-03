package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import com.arellomobile.mvp.InjectViewState
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.data.model.local.LastPriceItem
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderBook
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class TradeOrderBookPresenter @Inject constructor() : BasePresenter<TradeOrderBookView>() {
    var watchMarket: WatchMarket? = null
    var needFirstScrollToLastPrice = true

    fun loadOrderBook() {
        addSubscription(
                Observable.interval(0, 10, TimeUnit.SECONDS)
                        .retry(3)
                        .flatMap {
                            return@flatMap Observable.zip(matcherDataManager.loadOrderBook(watchMarket),
                                    dataFeedManager.getTradesByPair(watchMarket, 1)
                                            .map { it.firstOrNull() },
                                    BiFunction { orderBook: OrderBook, lastPrice: LastTrade? ->
                                        return@BiFunction Pair(orderBook, lastPrice)
                                    })
                        }
                        .doOnError {
                            runOnUiThread { viewState.afterFailedOrderbook() }
                        }
                        .onErrorResumeNext(Observable.empty())
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ pair ->
                            //                            val spreadPrice = pair.first.asks[0].price.plus(pair.first.bids[0].price).div(2)
//                            val delta = spreadPrice.times(1.5).div(2)
//                            val max = spreadPrice.plus(delta)
//                            val min = Math.max(0.0, spreadPrice.minus(delta))
//
//                            val croppedBids = pair.first.bids.filter { it.price > min && it.price < max }
//                            val croppedAsks = pair.first.asks.filter { it.price > min && it.price < max }
//
//                            val amountList = croppedAsks.asSequence()
//                                    .map { it.amount }
//                                    .plus(croppedBids
//                                            .map { it.amount })
//                                    .sorted()
//                                    .toList()
//
//                            val maxAmount = amountList[Math.floor(amountList.size * 0.9).toInt()] //percentile = 0.9;
//
//                            croppedAsks.forEach {
//                                it.width = it.amount.div(maxAmount.toFloat()).times(100)
//                            }
//
//                            croppedBids.forEach {
//                                it.width = it.amount.div(maxAmount.toFloat()).times(100)
//                            }

                            val result = mutableListOf<MultiItemEntity>()
                            result.addAll(pair.first.asks.asReversed())
                            pair.second.notNull {
                                val firstAsk = pair.first.asks.firstOrNull()?.price?.toDouble()
                                val firstBid = pair.first.bids.firstOrNull()?.price?.toDouble()
                                if (firstAsk == null && firstBid == null) {
                                    // do nothing, will show empty view
                                } else if (firstAsk == null || firstBid == null) {
                                    result.add(LastPriceItem(0.0, it))
                                } else {
                                    val percent = ((firstAsk.minus(firstBid))
                                            .times(100).div(firstBid))
                                    result.add(LastPriceItem(percent, it))
                                }
                            }
                            val lastPricePosition = result.size - 1
                            result.addAll(pair.first.bids)
                            viewState.afterSuccessOrderbook(result, lastPricePosition)
                        }, {
                            it.printStackTrace()
                            viewState.afterFailedOrderbook()
                        }))

    }

}
