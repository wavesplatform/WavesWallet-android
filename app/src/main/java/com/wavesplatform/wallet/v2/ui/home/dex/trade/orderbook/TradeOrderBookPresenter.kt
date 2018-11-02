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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class TradeOrderBookPresenter @Inject constructor() : BasePresenter<TradeOrderBookView>() {
    var watchMarket: WatchMarket? = null

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
                        .onErrorResumeNext(Observable.empty())
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ pair ->
                            val result = mutableListOf<MultiItemEntity>()
                            result.addAll(pair.first.asks.asReversed())
                            pair.second.notNull {
                                val firstAsk = pair.first.asks.firstOrNull()?.price?.toDouble()
                                val firstBid = pair.first.bids.firstOrNull()?.price?.toDouble()
                                val percent = ((firstAsk?.minus(firstBid
                                        ?: 0.0))?.times(100)?.div(firstBid ?: 1.0))
                                result.add(LastPriceItem(percent, it))
                            }
                            result.addAll(pair.first.bids)
                            viewState.afterSuccessOrderbook(result)
                        }, {
                            it.printStackTrace()
                        }))

    }

}
